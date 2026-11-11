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
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@EmbeddedKafka(partitions = 3)
@TestPropertySource(properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                // Los listeners deben leer lo publicado aunque se asignen tarde
                "spring.kafka.consumer.auto-offset-reset=earliest",
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

        @Autowired
        private KafkaListenerEndpointRegistry registry;

        // ------------------------------------------------------------------
        // Helpers
        // ------------------------------------------------------------------

        /**
         * Contenedores @KafkaListener que consumen el tópico indicado (sin depender de
         * nombres de grupo fijos).
         */
        private List<MessageListenerContainer> contenedoresDe(String topic) {
                return registry.getListenerContainers().stream()
                                .filter(c -> c.getContainerProperties().getTopics() != null
                                                && Arrays.asList(c.getContainerProperties().getTopics())
                                                                .contains(topic))
                                .toList();
        }

        /**
         * Falla con un mensaje claro si no hay listener, no corre, o no tiene
         * particiones asignadas.
         */
        private void esperarListenersActivos(String topic) {
                List<MessageListenerContainer> contenedores = contenedoresDe(topic);
                assertFalse(contenedores.isEmpty(),
                                "No hay ningún @KafkaListener registrado para '" + topic
                                                + "'. Listener ids en el registry: "
                                                + registry.getListenerContainerIds()
                                                + ". Revisa @Profile/@ConditionalOnProperty/autoStartup en el perfil de test.");

                await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
                        for (MessageListenerContainer c : contenedores) {
                                assertTrue(c.isRunning(),
                                                "Contenedor '" + c.getListenerId() + "' (grupo " + c.getGroupId()
                                                                + ") no está corriendo");
                                Collection<TopicPartition> asignadas = c.getAssignedPartitions();
                                assertTrue(asignadas != null && !asignadas.isEmpty(),
                                                "Contenedor '" + c.getListenerId() + "' (grupo " + c.getGroupId()
                                                                + ") corre pero no tiene particiones asignadas");
                        }
                });
        }

        /**
         * Suma de offsets confirmados (commit) por los grupos que consumen el tópico.
         */
        private long offsetsConfirmados(String topic) throws Exception {
                long total = 0;
                for (MessageListenerContainer c : contenedoresDe(topic)) {
                        Map<TopicPartition, OffsetAndMetadata> offsets = adminClient
                                        .listConsumerGroupOffsets(c.getGroupId())
                                        .partitionsToOffsetAndMetadata()
                                        .get(10, TimeUnit.SECONDS);
                        total += offsets.entrySet().stream()
                                        .filter(e -> e.getKey().topic().equals(topic) && e.getValue() != null)
                                        .mapToLong(e -> e.getValue().offset())
                                        .sum();
                }
                return total;
        }

        private HttpEntity<Void> sinCuerpo() {
                return null;
        }

        private void crearTopicoPorApi(String topicName) {
                TopicRequestDTO createReq = TopicRequestDTO.builder()
                                .nombre(topicName)
                                .particiones(3)
                                .factorReplicacion(1) // EmbeddedKafka tiene 1 broker
                                .retentionMs(86400000L)
                                .cleanupPolicy("delete")
                                .build();
                ResponseEntity<MensajeResponse> resp = restTemplate.postForEntity(
                                "/api/kafka/admin/topics", createReq, MensajeResponse.class);
                assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        }

        // ------------------------------------------------------------------
        // Tests
        // ------------------------------------------------------------------

        @Test
        @DisplayName("Escenario 1: Al arrancar se crean los 3 tópicos principales y sus DLT con particiones y configuraciones correctas")
        void escenario1_topicosInicialesCreados() throws Exception {
                Set<String> topicos = adminClient.listTopics().names().get(10, TimeUnit.SECONDS);

                assertTrue(topicos.contains(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS));
                assertTrue(topicos.contains(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS_DLT));
                assertTrue(topicos.contains(KafkaTopics.ASISTENCIA_ANALITICA_STREAM));
                assertTrue(topicos.contains(KafkaTopics.ASISTENCIA_ANALITICA_STREAM_DLT));
                assertTrue(topicos.contains(KafkaTopics.PLATAFORMA_ALERTAS_CRITICAS));
                assertTrue(topicos.contains(KafkaTopics.PLATAFORMA_ALERTAS_CRITICAS_DLT));

                ConfigResource crAuditoria = new ConfigResource(ConfigResource.Type.TOPIC,
                                KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS);
                Config configAuditoria = adminClient.describeConfigs(List.of(crAuditoria)).all().get().get(crAuditoria);
                assertEquals("604800000", configAuditoria.get("retention.ms").value());
                assertEquals("delete", configAuditoria.get("cleanup.policy").value());

                ConfigResource crAnalitica = new ConfigResource(ConfigResource.Type.TOPIC,
                                KafkaTopics.ASISTENCIA_ANALITICA_STREAM);
                Config configAnalitica = adminClient.describeConfigs(List.of(crAnalitica)).all().get().get(crAnalitica);
                assertEquals("2592000000", configAnalitica.get("retention.ms").value());
                assertEquals("delete", configAnalitica.get("cleanup.policy").value());
        }

        @Test
        @DisplayName("Escenario 2: Publicar eventos y verificar que los consumer groups los consumen (offsets confirmados avanzan)")
        void escenario2_publicarYConsumirEventos() throws Exception {
                esperarListenersActivos(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS);
                esperarListenersActivos(KafkaTopics.ASISTENCIA_ANALITICA_STREAM);

                long auditoriaAntes = offsetsConfirmados(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS);
                long analiticaAntes = offsetsConfirmados(KafkaTopics.ASISTENCIA_ANALITICA_STREAM);

                auditoriaProducer.publicar(AuditoriaEventDTO.builder()
                                .servicio("curso")
                                .accion("UPDATE")
                                .usuario("profesor@test.edu")
                                .detalle("Actualización de syllabus")
                                .build());

                analiticaProducer.publicar(AnaliticaStreamDTO.builder()
                                .cursoId(1L)
                                .estudianteId(10L)
                                .fecha(LocalDate.now())
                                .estado("PRESENTE")
                                .build());

                await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
                        assertTrue(offsetsConfirmados(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS) > auditoriaAntes,
                                        "El grupo de auditoría no confirmó offsets: el evento no fue consumido");
                        assertTrue(offsetsConfirmados(KafkaTopics.ASISTENCIA_ANALITICA_STREAM) > analiticaAntes,
                                        "El grupo de analítica no confirmó offsets: el evento no fue consumido");
                });
        }

        @Test
        @DisplayName("Escenario 3: Mensaje defectuoso va al DLT y consumidor no se detiene para mensajes válidos posteriores")
        void escenario3_mensajeDefectuosoVaADlt_yConsumidorSigueVivo() throws Exception {
                esperarListenersActivos(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS);

                String claveCorrupta = "corrupto-" + UUID.randomUUID();

                Map<String, Object> prodProps = new HashMap<>();
                prodProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
                prodProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                prodProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                DefaultKafkaProducerFactory<String, String> rawFactory = new DefaultKafkaProducerFactory<>(prodProps);
                KafkaTemplate<String, String> rawTemplate = new KafkaTemplate<>(rawFactory);

                Map<String, Object> consProps = new HashMap<>();
                consProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
                consProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-dlt-verifier-group-" + UUID.randomUUID());
                consProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                consProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                consProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

                try (Consumer<String, byte[]> dltConsumer = new DefaultKafkaConsumerFactory<String, byte[]>(consProps)
                                .createConsumer()) {

                        dltConsumer.subscribe(List.of(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS_DLT));

                        // Mensaje corrupto al tópico de auditoría
                        rawTemplate.send(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS, claveCorrupta,
                                        "{esto no es json valido!}")
                                        .get(5, TimeUnit.SECONDS);

                        // Se busca por KEY (no por contenido): si la plantilla del DLT usa
                        // JsonSerializer,
                        // el byte[] original llega en base64 y buscar el texto fallaría.
                        await().atMost(20, TimeUnit.SECONDS).pollInSameThread().until(() -> {
                                ConsumerRecords<String, byte[]> records = dltConsumer.poll(Duration.ofMillis(500));
                                for (ConsumerRecord<String, byte[]> record : records) {
                                        if (claveCorrupta.equals(record.key())) {
                                                return true;
                                        }
                                }
                                return false;
                        });
                } finally {
                        rawFactory.destroy();
                }

                // Mensaje válido posterior: el consumidor debe seguir vivo y confirmar offsets
                long antes = offsetsConfirmados(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS);

                AuditoriaEventDTO validoPosterior = AuditoriaEventDTO.builder()
                                .servicio("asistencia")
                                .accion("RESILIENCIA_OK")
                                .usuario("admin")
                                .detalle("Mensaje posterior al corrupto")
                                .build();
                assertDoesNotThrow(() -> auditoriaProducer.publicar(validoPosterior));

                await().atMost(20, TimeUnit.SECONDS)
                                .untilAsserted(() -> assertTrue(
                                                offsetsConfirmados(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS) > antes,
                                                "El consumidor no procesó el mensaje válido posterior al corrupto"));
        }

        @Test
        @DisplayName("Escenario 4: API REST: crear, 409 duplicado, inspeccionar, modificar config y aumentar particiones")
        void escenario4_apiRestCrearInspeccionarModificar() {
                String topicName = "e2e.test.topic." + System.currentTimeMillis();

                // 1. Crear (201)
                TopicRequestDTO createReq = TopicRequestDTO.builder()
                                .nombre(topicName)
                                .particiones(3)
                                .factorReplicacion(1)
                                .retentionMs(86400000L)
                                .cleanupPolicy("delete")
                                .build();
                ResponseEntity<MensajeResponse> respCrear = restTemplate.postForEntity(
                                "/api/kafka/admin/topics", createReq, MensajeResponse.class);
                assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());

                // 2. Duplicado (409)
                ResponseEntity<ErrorResponse> respDuplicado = restTemplate.postForEntity(
                                "/api/kafka/admin/topics", createReq, ErrorResponse.class);
                assertEquals(HttpStatus.CONFLICT, respDuplicado.getStatusCode());

                // 3. Inspeccionar (200)
                ResponseEntity<TopicoDetalleResponse> respInsp = restTemplate.getForEntity(
                                "/api/kafka/admin/topics/" + topicName, TopicoDetalleResponse.class);
                assertEquals(HttpStatus.OK, respInsp.getStatusCode());
                assertEquals(3, respInsp.getBody().totalParticiones());

                // 4. Modificar retention.ms (200)
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, String>> configReq = new HttpEntity<>(Map.of("retention.ms", "172800000"),
                                headers);
                ResponseEntity<MensajeResponse> respMod = restTemplate.exchange(
                                "/api/kafka/admin/topics/" + topicName + "/config", HttpMethod.PUT, configReq,
                                MensajeResponse.class);
                assertEquals(HttpStatus.OK, respMod.getStatusCode());

                // 5. Aumentar particiones 3 -> 5 (200)
                HttpEntity<Map<String, Integer>> partReq = new HttpEntity<>(Map.of("particiones", 5), headers);
                ResponseEntity<MensajeResponse> respPart = restTemplate.exchange(
                                "/api/kafka/admin/topics/" + topicName + "/particiones", HttpMethod.PUT, partReq,
                                MensajeResponse.class);
                assertEquals(HttpStatus.OK, respPart.getStatusCode());
        }

        /**
         * El borrado de tópicos en el broker embebido falla en Windows: Kafka no puede
         * renombrar el
         * directorio del log (archivos .index mapeados en memoria ->
         * AccessDeniedException), el log dir
         * se marca como fallido y el broker se apaga, dejando sin servicio a los demás
         * tests.
         * Se aísla aquí; en Linux/CI corre normal. Alternativa: Testcontainers con
         * imagen de Kafka.
         */
        @Test
        @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Kafka embebido no puede borrar tópicos en Windows (AccessDeniedException)")
        @DisplayName("Escenario 4b: Eliminar tópico (200) y luego 404 al inspeccionarlo")
        void escenario4b_eliminarTopicoYNotFound() {
                String topicName = "e2e.delete.topic." + System.currentTimeMillis();
                crearTopicoPorApi(topicName);

                ResponseEntity<MensajeResponse> respDel = restTemplate.exchange(
                                "/api/kafka/admin/topics/" + topicName, HttpMethod.DELETE, sinCuerpo(),
                                MensajeResponse.class);
                assertEquals(HttpStatus.OK, respDel.getStatusCode());

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

                assertTrue(node.get("timestamp").isTextual(), "El timestamp debe ser textual en formato ISO-8601");
                assertEquals("2026-11-11T15:30:00", node.get("timestamp").asText());
        }
}