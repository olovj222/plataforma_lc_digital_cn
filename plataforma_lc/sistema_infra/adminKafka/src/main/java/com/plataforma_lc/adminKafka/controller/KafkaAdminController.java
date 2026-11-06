package com.plataforma_lc.adminKafka.controller;

import com.plataforma_lc.adminKafka.dto.TopicRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 *  * Proporciona endpoints para listar, crear, inspeccionar y eliminar tópicos,
 * así como consultar consumer groups activos y sus offsets.
 */
@Slf4j
@RestController
@RequestMapping("/api/kafka/admin/topics")
@RequiredArgsConstructor
public class KafkaAdminController {

    private final AdminClient adminClient;

    // ========================================================================================
    // LISTAR TÓPICOS
    // ========================================================================================

    /**
     * Lista todos los topics del clúster Kafka (excluyendo los internos).
     * GET /api/kafka/admin/topics
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarTopicos() {
        try {
            Set<String> topicos = adminClient.listTopics(new ListTopicsOptions().listInternal(false))
                    .names()
                    .get();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("totalTopicos", topicos.size());
            response.put("topicos", topicos.stream().sorted().collect(Collectors.toList()));

            log.info("[KAFKA-ADMIN] Topic listados exitosamente: {} tópicos encontrados", topicos.size());
            return ResponseEntity.ok(response);

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("[KAFKA-ADMIN] Error al listar topics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al listar topic: " + e.getMessage()));
        }
    }

    // ========================================================================================
    // CREAR TopicO
    // ========================================================================================

    /**
     * Crea un nuevo topic con parámetros dinámicos.
     * POST /api/kafka/admin/topics
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearTopico(@Valid @RequestBody TopicRequestDTO request) {
        try {
            NewTopic nuevoTopico = new NewTopic(
                    request.getNombre(),
                    request.getParticiones(),
                    (short) request.getFactorReplicacion()
            );

            // Configuraciones adicionales del tópico
            Map<String, String> configs = new HashMap<>();
            configs.put("retention.ms", String.valueOf(request.getRetentionMs()));
            configs.put("cleanup.policy", request.getCleanupPolicy());
            configs.put("min.insync.replicas", "2");
            nuevoTopico.configs(configs);

            adminClient.createTopics(Collections.singletonList(nuevoTopico)).all().get();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("mensaje", "Topic creado exitosamente");
            response.put("nombre", request.getNombre());
            response.put("particiones", request.getParticiones());
            response.put("factorReplicacion", request.getFactorReplicacion());
            response.put("retentionMs", request.getRetentionMs());
            response.put("cleanupPolicy", request.getCleanupPolicy());

            log.info("[KAFKA-ADMIN] ✅ Topic creado: nombre={}, particiones={}, réplicas={}",
                    request.getNombre(), request.getParticiones(), request.getFactorReplicacion());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("[KAFKA-ADMIN] ❌ Error al crear topic '{}': {}", request.getNombre(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear topic: " + e.getMessage()));
        }
    }

    // ========================================================================================
    // INSPECCIONAR Topic (detalles: particiones, réplicas, ISR, configuraciones)
    // ========================================================================================

    /**
     * Obtiene los detalles completos de un topic: particiones, réplicas, ISR y configuraciones.
     * GET /api/kafka/admin/topics/{nombreTopico}
     */
    @GetMapping("/{nombreTopico}")
    public ResponseEntity<Map<String, Object>> inspeccionarTopico(@PathVariable String nombreTopico) {
        try {
            // Obtener descripción del topic
            TopicDescription descripcion = adminClient
                    .describeTopics(Collections.singletonList(nombreTopico))
                    .topicNameValues()
                    .get(nombreTopico)
                    .get();

            // Obtener configuraciones del topico
            ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombreTopico);
            Config config = adminClient
                    .describeConfigs(Collections.singletonList(recurso))
                    .all()
                    .get()
                    .get(recurso);

            // Construir respuesta detallada
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("nombre", descripcion.name());
            response.put("topicId", descripcion.topicId().toString());
            response.put("esInterno", descripcion.isInternal());
            response.put("totalParticiones", descripcion.partitions().size());

            // Detalle por partición
            List<Map<String, Object>> particiones = new ArrayList<>();
            for (TopicPartitionInfo partInfo : descripcion.partitions()) {
                Map<String, Object> partData = new LinkedHashMap<>();
                partData.put("particion", partInfo.partition());
                partData.put("lider", formatNode(partInfo.leader()));
                partData.put("replicas", partInfo.replicas().stream()
                        .map(this::formatNode)
                        .collect(Collectors.toList()));
                partData.put("isr", partInfo.isr().stream()
                        .map(this::formatNode)
                        .collect(Collectors.toList()));
                partData.put("replicasEnSync", partInfo.isr().size());
                particiones.add(partData);
            }
            response.put("particiones", particiones);

            // Configuraciones del topic
            Map<String, String> configuraciones = new LinkedHashMap<>();
            for (ConfigEntry entry : config.entries()) {
                if (!entry.isDefault()) {
                    configuraciones.put(entry.name(), entry.value());
                }
            }
            response.put("configuracionesPersonalizadas", configuraciones);

            log.info("[KAFKA-ADMIN] Topic inspeccionado: {}", nombreTopico);
            return ResponseEntity.ok(response);

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("[KAFKA-ADMIN] Error al inspeccionar topic '{}': {}", nombreTopico, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Topic no encontrado o error al inspeccionar: " + e.getMessage()));
        }
    }

    // ========================================================================================
    // ELIMINAR ToPIC
    // ========================================================================================

    /**
     * Elimina un topic del clúster Kafka.
     * DELETE /api/kafka/admin/topics/{nombreTopico}
     */
    @DeleteMapping("/{nombreTopico}")
    public ResponseEntity<Map<String, Object>> eliminarTopico(@PathVariable String nombreTopico) {
        try {
            adminClient.deleteTopics(Collections.singletonList(nombreTopico)).all().get();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("mensaje", "Topic eliminado exitosamente");
            response.put("nombre", nombreTopico);

            log.info("[KAFKA-ADMIN] ✅ Topic eliminado: {}", nombreTopico);
            return ResponseEntity.ok(response);

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("[KAFKA-ADMIN] ❌ Error al eliminar topic '{}': {}", nombreTopico, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar topic: " + e.getMessage()));
        }
    }

    // ========================================================================================
    // LISTAR CONSUMER GROUPS ACTIVOS Y SUS OFFSETS
    // ========================================================================================

    /**
     * Lista todos los consumer groups activos del clúster con sus offsets por partición.
     * GET /api/kafka/admin/topics/consumer-groups
     */
    @GetMapping("/consumer-groups")
    public ResponseEntity<Map<String, Object>> listarConsumerGroups() {
        try {
            // Obtener todos los consumer groups
            Collection<ConsumerGroupListing> grupos = adminClient
                    .listConsumerGroups()
                    .all()
                    .get();

            List<Map<String, Object>> gruposDetalle = new ArrayList<>();

            for (ConsumerGroupListing grupo : grupos) {
                Map<String, Object> grupoData = new LinkedHashMap<>();
                grupoData.put("groupId", grupo.groupId());
                grupoData.put("estado", grupo.state().isPresent() ? grupo.state().get().toString() : "UNKNOWN");
                grupoData.put("esSimple", grupo.isSimpleConsumerGroup());

                // Obtener offsets del consumer group
                try {
                    Map<TopicPartition, OffsetAndMetadata> offsets = adminClient
                            .listConsumerGroupOffsets(grupo.groupId())
                            .partitionsToOffsetAndMetadata()
                            .get();

                    List<Map<String, Object>> offsetsDetalle = new ArrayList<>();
                    for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                        Map<String, Object> offsetData = new LinkedHashMap<>();
                        offsetData.put("topic", entry.getKey().topic());
                        offsetData.put("particion", entry.getKey().partition());
                        offsetData.put("offset", entry.getValue().offset());
                        if (entry.getValue().metadata() != null && !entry.getValue().metadata().isEmpty()) {
                            offsetData.put("metadata", entry.getValue().metadata());
                        }
                        offsetsDetalle.add(offsetData);
                    }
                    grupoData.put("offsets", offsetsDetalle);
                } catch (Exception e) {
                    grupoData.put("offsets", "Error al obtener offsets: " + e.getMessage());
                }

                gruposDetalle.add(grupoData);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("totalConsumerGroups", gruposDetalle.size());
            response.put("consumerGroups", gruposDetalle);

            log.info("[KAFKA-ADMIN] Consumer groups listados: {} grupos encontrados", gruposDetalle.size());
            return ResponseEntity.ok(response);

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("[KAFKA-ADMIN] Error al listar consumer groups: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al listar consumer groups: " + e.getMessage()));
        }
    }

    // ========================================================================================
    // UTILIDADES PRIVADAS
    // ========================================================================================

    /**
     * Formatea la información de un nodo Kafka para la respuesta JSON.
     */
    private String formatNode(Node node) {
        if (node == null) return "N/A";
        return String.format("broker-%d (%s:%d)", node.id(), node.host(), node.port());
    }
}

