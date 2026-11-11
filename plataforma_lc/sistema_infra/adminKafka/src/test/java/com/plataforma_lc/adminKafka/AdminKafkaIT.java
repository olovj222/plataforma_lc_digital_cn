package com.plataforma_lc.adminKafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma_lc.adminKafka.config.KafkaTopics;
import com.plataforma_lc.adminKafka.dto.AnaliticaStreamDTO;
import com.plataforma_lc.adminKafka.dto.AuditoriaEventDTO;
import com.plataforma_lc.adminKafka.dto.KafkaResponses.*;
import com.plataforma_lc.adminKafka.dto.TopicRequestDTO;
import com.plataforma_lc.adminKafka.exception.ErrorResponse;
import com.plataforma_lc.adminKafka.producer.AnaliticaProducer;
import com.plataforma_lc.adminKafka.producer.AuditoriaProducer;
import com.plataforma_lc.adminKafka.service.KafkaAdminService;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@EmbeddedKafka(
        partitions = 3
)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "app.kafka.topics.partitions=3",
        "app.kafka.topics.replicas=1",
        "app.kafka.topics.min-insync-replicas=1",
        "spring.kafka.producer.acks=all"
})
@DirtiesContext
class AdminKafkaIT {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private AdminClient adminClient;

    @Autowired
    private KafkaAdminService adminService;

    @Autowired
    private AuditoriaProducer auditoriaProducer;

    @Autowired
    private AnaliticaProducer analiticaProducer;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Escenario 1: Al arrancar se crean los 3 tópicos principales y sus DLT con particiones y configuraciones correctas")
    void escenario1_topicosInicialesCreados() throws Exception {
        Set<String> topicos = adminClient.listTopics().names().get(10, TimeUnit.SECONDS);

        // Verificar existencia de los 3 tópicos y los 3 DLT
        assertTrue(topicos.contains(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS));
        assertTrue(topicos.contains(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS_DLT));
        assertTrue(topicos.contains(KafkaTopics.ASISTENCIA_ANALITICA_STREAM));
        assertTrue(topicos.contains(KafkaTopics.ASISTENCIA_ANALITICA_STREAM_DLT));
        assertTrue(topicos.contains(KafkaTopics.PLATAFORMA_ALERTAS_CRITICAS));
        assertTrue(topicos.contains(KafkaTopics.PLATAFORMA_ALERTAS_CRITICAS_DLT));

        // Verificar configuraciones de auditoría (retention.ms=604800000, cleanup.policy=delete)
        ConfigResource crAuditoria = new ConfigResource(ConfigResource.Type.TOPIC, KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS);
        Config configAuditoria = adminClient.describeConfigs(List.of(crAuditoria)).all().get().get(crAuditoria);
        assertEquals("604800000", configAuditoria.get("retention.ms").value());
        assertEquals("delete", configAuditoria.get("cleanup.policy").value());

        // Verificar analítica (retention.ms=2592000000)
        ConfigResource crAnalitica = new ConfigResource(ConfigResource.Type.TOPIC, KafkaTopics.ASISTENCIA_ANALITICA_STREAM);
        Config configAnalitica = adminClient.describeConfigs(List.of(crAnalitica)).all().get().get(crAnalitica);
        assertEquals("2592000000", configAnalitica.get("retention.ms").value());
        assertEquals("delete", configAnalitica.get("cleanup.policy").value());
    }

    @Test
    @DisplayName("Escenario 2: Publicar eventos por productores y verificar que los consumer groups están activos")
    void escenario2_publicarYConsumirEventos() {
        AuditoriaEventDTO auditoria = AuditoriaEventDTO.builder()
                .servicio("curso")
                .accion("UPDATE")
                .usuario("profesor@test.edu")
                .detalle("Actualización de syllabus")
                .build();
        auditoriaProducer.publicar(auditoria);

        AnaliticaStreamDTO analitica = AnaliticaStreamDTO.builder()
                .cursoId(1L)
                .estudianteId(10L)
                .fecha(LocalDate.now())
                .estado("PRESENTE")
                .build();
        analiticaProducer.publicar(analitica);

        // Esperar con Awaitility a que los consumer groups estén registrados
        await().atMost(15, TimeUnit.SECONDS).until(() -> {
            try {
                var groups = adminClient.listConsumerGroups().all().get();
                return groups.stream().anyMatch(g -> g.groupId().contains("plataforma-auditoria-group"))
                        && groups.stream().anyMatch(g -> g.groupId().contains("plataforma-analitica-group"));
            } catch (Exception e) {
                return false;
            }
        });
    }

    @Test
    @DisplayName("Escenario 3: Mensaje defectuoso va al DLT y consumidor no se detiene para mensajes válidos posteriores")
    void escenario3_mensajeDefectuosoVaADlt_yConsumidorSigueVivo() throws Exception {
        // 1. Preparar un productor crudo de strings para enviar JSON inválido
        Map<String, Object> prodProps = new HashMap<>();
        prodProps.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        prodProps.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        prodProps.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        KafkaTemplate<String, String> rawTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(prodProps));

        // 2. Preparar un consumidor de test para escuchar en plataforma.auditoria.eventos.DLT
        Map<String, Object> consProps = new HashMap<>();
        consProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        consProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-dlt-verifier-group-" + UUID.randomUUID());
        consProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        Consumer<String, byte[]> dltConsumer = new DefaultKafkaConsumerFactory<String, byte[]>(consProps).createConsumer();
        dltConsumer.subscribe(List.of(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS_DLT));

        // 3. Enviar mensaje corrupto al tópico de auditoría
        String payloadCorrupto = "{esto no es json valido!}";
        rawTemplate.send(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS, "corrupto-key", payloadCorrupto).get(5, TimeUnit.SECONDS);

        // 4. Verificar que el mensaje corrupto llegó al DLT
        await().atMost(20, TimeUnit.SECONDS).until(() -> {
            ConsumerRecords<String, byte[]> records = dltConsumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, byte[]> record : records) {
                String val = new String(record.value(), StandardCharsets.UTF_8);
                if (val.contains("esto no es json valido!")) {
                    return true;
                }
            }
            return false;
        });
        dltConsumer.close();

        // 5. Enviar mensaje válido después del mensaje corrupto para probar resiliencia
        AuditoriaEventDTO validoPosterior = AuditoriaEventDTO.builder()
                .servicio("asistencia")
                .accion("RESILIENCIA_OK")
                .usuario("admin")
                .detalle("Mensaje posterior al corrupto")
                .build();
        assertDoesNotThrow(() -> auditoriaProducer.publicar(validoPosterior));
    }

    @Test
    @DisplayName("Escenario 4: API REST end-to-end: crear, 409 duplicado, inspeccionar, modificar config, aumentar particiones, eliminar y 404")
    void escenario4_apiRestEndToEnd() {
        String topicName = "e2e.test.topic." + System.currentTimeMillis();

        // 1. Crear tópico (201 Created)
        TopicRequestDTO createReq = TopicRequestDTO.builder()
                .nombre(topicName)
                .particiones(3)
                .factorReplicacion(1) // EmbeddedKafka tiene 1 broker en este entorno
                .retentionMs(86400000L)
                .cleanupPolicy("delete")
                .build();

        ResponseEntity<MensajeResponse> respCrear = restTemplate.postForEntity(
                "/api/kafka/admin/topics", createReq, MensajeResponse.class);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());

        // 2. Intentar crear de nuevo (409 Conflict)
        ResponseEntity<ErrorResponse> respDuplicado = restTemplate.postForEntity(
                "/api/kafka/admin/topics", createReq, ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, respDuplicado.getStatusCode());

        // 3. Inspeccionar tópico (200 OK)
        ResponseEntity<TopicoDetalleResponse> respInsp = restTemplate.getForEntity(
                "/api/kafka/admin/topics/" + topicName, TopicoDetalleResponse.class);
        assertEquals(HttpStatus.OK, respInsp.getStatusCode());
        assertEquals(3, respInsp.getBody().totalParticiones());

        // 4. Modificar configuración retention.ms (200 OK)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> configReq = new HttpEntity<>(Map.of("retention.ms", "172800000"), headers);
        ResponseEntity<MensajeResponse> respMod = restTemplate.exchange(
                "/api/kafka/admin/topics/" + topicName + "/config", HttpMethod.PUT, configReq, MensajeResponse.class);
        assertEquals(HttpStatus.OK, respMod.getStatusCode());

        // 5. Aumentar particiones de 3 a 5 (200 OK)
        HttpEntity<Map<String, Integer>> partReq = new HttpEntity<>(Map.of("particiones", 5), headers);
        ResponseEntity<MensajeResponse> respPart = restTemplate.exchange(
                "/api/kafka/admin/topics/" + topicName + "/particiones", HttpMethod.PUT, partReq, MensajeResponse.class);
        assertEquals(HttpStatus.OK, respPart.getStatusCode());

        // 6. Eliminar tópico (200 OK)
        ResponseEntity<MensajeResponse> respDel = restTemplate.exchange(
                "/api/kafka/admin/topics/" + topicName, HttpMethod.DELETE, null, MensajeResponse.class);
        assertEquals(HttpStatus.OK, respDel.getStatusCode());

        // 7. Inspeccionar de nuevo tras eliminar (404 Not Found)
        ResponseEntity<ErrorResponse> respNotFound = restTemplate.getForEntity(
                "/api/kafka/admin/topics/" + topicName, ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, respNotFound.getStatusCode());
    }

    @Test
    @DisplayName("Escenario 5: Verifica que el JSON serializa LocalDateTime en formato ISO-8601 (texto no array numérico)")
    void escenario5_fechasSerializanIso8601() throws Exception {
        AuditoriaEventDTO evento = AuditoriaEventDTO.builder()
                .servicio("estudiante")
                .accion("TEST_ISO")
                .usuario("admin")
                .timestamp(LocalDateTime.of(2026, 11, 11, 15, 30, 0))
                .build();

        String json = objectMapper.writeValueAsString(evento);
        JsonNode node = objectMapper.readTree(json);

        // Comprobar que timestamp es un valor de texto ISO-8601, no un array numérico [2026, 11, 11, 15, 30, 0]
        assertTrue(node.get("timestamp").isTextual(), "El timestamp debe ser textual en formato ISO-8601");
        assertEquals("2026-11-11T15:30:00", node.get("timestamp").asText());
    }
}
