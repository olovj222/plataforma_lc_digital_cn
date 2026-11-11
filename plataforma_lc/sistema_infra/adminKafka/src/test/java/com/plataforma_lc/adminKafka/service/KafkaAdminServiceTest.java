package com.plataforma_lc.adminKafka.service;

import com.plataforma_lc.adminKafka.config.KafkaTopics;
import com.plataforma_lc.adminKafka.dto.KafkaResponses.*;
import com.plataforma_lc.adminKafka.dto.TopicRequestDTO;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.InvalidConfigurationException;
import org.apache.kafka.common.errors.InvalidPartitionsException;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.internals.KafkaFutureImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaAdminServiceTest {

    @Mock
    private AdminClient adminClient;

    @InjectMocks
    private KafkaAdminService adminService;

    private Node broker1;
    private Node broker2;
    private Node broker3;

    @BeforeEach
    void setUp() {
        broker1 = new Node(1, "kafka-1", 29092);
        broker2 = new Node(2, "kafka-2", 29092);
        broker3 = new Node(3, "kafka-3", 29092);
    }

    private <T> KafkaFuture<T> failedFuture(Throwable cause) {
        KafkaFutureImpl<T> future = new KafkaFutureImpl<>();
        future.completeExceptionally(cause);
        return future;
    }

    @Test
    @DisplayName("listarTopicos: retorna lista ordenada sin tópicos internos")
    void listarTopicos_exito() {
        ListTopicsResult listTopicsResult = mock(ListTopicsResult.class);
        when(adminClient.listTopics(any(ListTopicsOptions.class))).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(KafkaFuture.completedFuture(Set.of("b.topic", "a.topic")));

        ListadoTopicosResponse response = adminService.listarTopicos();

        assertEquals(2, response.totalTopicos());
        assertEquals(List.of("a.topic", "b.topic"), response.topicos());
    }

    @Test
    @DisplayName("crearTopico: crea tópico exitosamente con minIsr seguro")
    void crearTopico_exito() {
        DescribeClusterResult clusterResult = mock(DescribeClusterResult.class);
        when(adminClient.describeCluster()).thenReturn(clusterResult);
        when(clusterResult.nodes()).thenReturn(KafkaFuture.completedFuture(List.of(broker1, broker2, broker3)));

        CreateTopicsResult createTopicsResult = mock(CreateTopicsResult.class);
        when(adminClient.createTopics(any())).thenReturn(createTopicsResult);
        when(createTopicsResult.all()).thenReturn(KafkaFuture.completedFuture(null));

        TopicRequestDTO dto = TopicRequestDTO.builder()
                .nombre("test.nuevo")
                .particiones(3)
                .factorReplicacion(3)
                .retentionMs(86400000L)
                .cleanupPolicy("delete")
                .build();

        MensajeResponse resp = adminService.crearTopico(dto);

        assertEquals("Tópico creado exitosamente", resp.mensaje());
        assertEquals("test.nuevo", resp.recurso());
        verify(adminClient).createTopics(any());
    }

    @Test
    @DisplayName("crearTopico: falla cuando el factor de replicación supera los brokers disponibles")
    void crearTopico_rfMayorQueBrokers_lanzaExcepcion() {
        DescribeClusterResult clusterResult = mock(DescribeClusterResult.class);
        when(adminClient.describeCluster()).thenReturn(clusterResult);
        when(clusterResult.nodes()).thenReturn(KafkaFuture.completedFuture(List.of(broker1, broker2)));

        TopicRequestDTO dto = TopicRequestDTO.builder()
                .nombre("test.fallo")
                .particiones(3)
                .factorReplicacion(3) // 3 > 2 brokers
                .build();

        assertThrows(InvalidReplicationFactorException.class, () -> adminService.crearTopico(dto));
        verify(adminClient, never()).createTopics(any());
    }

    @Test
    @DisplayName("inspeccionarTopico: retorna información detallada del tópico")
    void inspeccionarTopico_exito() {
        String topic = "test.topic";
        DescribeTopicsResult descResult = mock(DescribeTopicsResult.class);
        when(adminClient.describeTopics(Collections.singletonList(topic))).thenReturn(descResult);

        TopicPartitionInfo part0 = new TopicPartitionInfo(0, broker1, List.of(broker1, broker2), List.of(broker1, broker2));
        TopicDescription desc = new TopicDescription(topic, false, List.of(part0));
        when(descResult.topicNameValues()).thenReturn(Map.of(topic, KafkaFuture.completedFuture(desc)));

        DescribeConfigsResult confResult = mock(DescribeConfigsResult.class);
        when(adminClient.describeConfigs(any())).thenReturn(confResult);
        ConfigResource cr = new ConfigResource(ConfigResource.Type.TOPIC, topic);
        ConfigEntry ce = new ConfigEntry("retention.ms", "604800000", ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG, false, false, Collections.emptyList(), ConfigEntry.ConfigType.LONG, null);
        Config config = new Config(List.of(ce));
        when(confResult.all()).thenReturn(KafkaFuture.completedFuture(Map.of(cr, config)));

        TopicoDetalleResponse response = adminService.inspeccionarTopico(topic);

        assertEquals(topic, response.nombre());
        assertEquals(1, response.totalParticiones());
        assertEquals("604800000", response.configuracionesPersonalizadas().get("retention.ms"));
    }

    @Test
    @DisplayName("inspeccionarTopico: lanza UnknownTopicOrPartitionException cuando no existe")
    void inspeccionarTopico_inexistente_lanzaExcepcion() {
        String topic = "no.existe";
        DescribeTopicsResult descResult = mock(DescribeTopicsResult.class);
        when(adminClient.describeTopics(Collections.singletonList(topic))).thenReturn(descResult);
        when(descResult.topicNameValues()).thenReturn(Map.of(topic, failedFuture(new UnknownTopicOrPartitionException("No existe"))));

        assertThrows(UnknownTopicOrPartitionException.class, () -> adminService.inspeccionarTopico(topic));
    }

    @Test
    @DisplayName("modificarConfiguracion: clave inválida lanza InvalidConfigurationException")
    void modificarConfiguracion_claveInvalida_lanzaExcepcion() {
        String topic = "test.topic";
        DescribeTopicsResult descResult = mock(DescribeTopicsResult.class);
        when(adminClient.describeTopics(Collections.singletonList(topic))).thenReturn(descResult);
        TopicDescription desc = new TopicDescription(topic, false, List.of());
        when(descResult.topicNameValues()).thenReturn(Map.of(topic, KafkaFuture.completedFuture(desc)));
        DescribeConfigsResult confResult = mock(DescribeConfigsResult.class);
        when(adminClient.describeConfigs(any())).thenReturn(confResult);
        when(confResult.all()).thenReturn(KafkaFuture.completedFuture(Map.of(new ConfigResource(ConfigResource.Type.TOPIC, topic), new Config(List.of()))));

        Map<String, String> configs = Map.of("clave.no.permitida", "123");

        assertThrows(InvalidConfigurationException.class, () -> adminService.modificarConfiguracion(topic, configs));
        verify(adminClient, never()).incrementalAlterConfigs(any());
    }

    @Test
    @DisplayName("aumentarParticiones: rechaza si el nuevo total es menor o igual al actual")
    void aumentarParticiones_reducir_rechaza() {
        String topic = "test.topic";
        DescribeTopicsResult descResult = mock(DescribeTopicsResult.class);
        when(adminClient.describeTopics(Collections.singletonList(topic))).thenReturn(descResult);
        TopicPartitionInfo part0 = new TopicPartitionInfo(0, broker1, List.of(broker1), List.of(broker1));
        TopicPartitionInfo part1 = new TopicPartitionInfo(1, broker1, List.of(broker1), List.of(broker1));
        TopicDescription desc = new TopicDescription(topic, false, List.of(part0, part1)); // 2 particiones
        when(descResult.topicNameValues()).thenReturn(Map.of(topic, KafkaFuture.completedFuture(desc)));
        DescribeConfigsResult confResult = mock(DescribeConfigsResult.class);
        when(adminClient.describeConfigs(any())).thenReturn(confResult);
        when(confResult.all()).thenReturn(KafkaFuture.completedFuture(Map.of(new ConfigResource(ConfigResource.Type.TOPIC, topic), new Config(List.of()))));

        assertThrows(InvalidPartitionsException.class, () -> adminService.aumentarParticiones(topic, 2));
        assertThrows(InvalidPartitionsException.class, () -> adminService.aumentarParticiones(topic, 1));
        verify(adminClient, never()).createPartitions(any());
    }

    @Test
    @DisplayName("eliminarTopico: tópico protegido sin force=true es rechazado")
    void eliminarTopico_protegidoSinForce_lanzaExcepcion() {
        String topicProtegido = KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminService.eliminarTopico(topicProtegido, false));

        assertTrue(ex.getMessage().contains("protegido"));
        verify(adminClient, never()).deleteTopics(any(Collection.class));
    }

    @Test
    @DisplayName("eliminarTopico: tópico protegido con force=true se elimina exitosamente")
    void eliminarTopico_protegidoConForce_exito() {
        String topicProtegido = KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS;
        DeleteTopicsResult deleteResult = mock(DeleteTopicsResult.class);
        Collection<String> topicsToDelete = Collections.singletonList(topicProtegido);
        when(adminClient.deleteTopics(topicsToDelete)).thenReturn(deleteResult);
        when(deleteResult.all()).thenReturn(KafkaFuture.completedFuture(null));

        MensajeResponse resp = adminService.eliminarTopico(topicProtegido, true);

        assertEquals("Tópico eliminado exitosamente", resp.mensaje());
        assertEquals(topicProtegido, resp.recurso());
    }

    @Test
    @DisplayName("describirConsumerGroup: calcula lag correctamente")
    void describirConsumerGroup_calculaLag() {
        String groupId = "test-group";
        DescribeConsumerGroupsResult descResult = mock(DescribeConsumerGroupsResult.class);
        when(adminClient.describeConsumerGroups(Collections.singletonList(groupId))).thenReturn(descResult);

        ConsumerGroupDescription desc = new ConsumerGroupDescription(
                groupId, true, Collections.emptyList(), "range", ConsumerGroupState.STABLE, broker1
        );
        when(descResult.describedGroups()).thenReturn(Map.of(groupId, KafkaFuture.completedFuture(desc)));

        ListConsumerGroupOffsetsResult offsetsResult = mock(ListConsumerGroupOffsetsResult.class);
        when(adminClient.listConsumerGroupOffsets(groupId)).thenReturn(offsetsResult);

        TopicPartition tp = new TopicPartition("test.topic", 0);
        when(offsetsResult.partitionsToOffsetAndMetadata()).thenReturn(
                KafkaFuture.completedFuture(Map.of(tp, new OffsetAndMetadata(100L)))
        );

        ListOffsetsResult listOffsetsResult = mock(ListOffsetsResult.class);
        when(adminClient.listOffsets(any())).thenReturn(listOffsetsResult);
        when(listOffsetsResult.all()).thenReturn(KafkaFuture.completedFuture(
                Map.of(tp, new ListOffsetsResult.ListOffsetsResultInfo(150L, System.currentTimeMillis(), Optional.empty()))
        ));

        ConsumerGroupDetalleResponse response = adminService.describirConsumerGroup(groupId);

        assertEquals(groupId, response.groupId());
        assertEquals(1, response.particionesOffsetLag().size());
        OffsetLagResponse offsetLag = response.particionesOffsetLag().get(0);
        assertEquals(100L, offsetLag.currentOffset());
        assertEquals(150L, offsetLag.logEndOffset());
        assertEquals(50L, offsetLag.lag()); // 150 - 100 = 50
        assertEquals(50L, response.lagTotal());
    }
}
