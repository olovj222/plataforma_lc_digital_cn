package com.plataforma_lc.adminKafka.service;

import com.plataforma_lc.adminKafka.config.KafkaTopics;
import com.plataforma_lc.adminKafka.dto.KafkaResponses.*;
import com.plataforma_lc.adminKafka.dto.TopicRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.InvalidConfigurationException;
import org.apache.kafka.common.errors.InvalidPartitionsException;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Servicio de administración centralizado para tópicos, consumer groups y clúster Kafka.
 * Desenvuelve ExecutionException para exponer excepciones nativas de Kafka y maneja
 * correctamente InterruptedException restaurando el flag de interrupción del hilo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaAdminService {

    private final AdminClient adminClient;

    /**
     * Conjunto cerrado de claves de configuración permitidas para modificación en tiempo de ejecución.
     */
    private static final Set<String> CLAVES_CONFIG_PERMITIDAS = Set.of(
            "retention.ms",
            "retention.bytes",
            "cleanup.policy",
            "min.insync.replicas",
            "segment.ms",
            "segment.bytes",
            "max.message.bytes"
    );

    // ========================================================================================
    // TÓPICOS
    // ========================================================================================

    /**
     * Lista todos los tópicos del clúster (excluyendo los internos).
     */
    public ListadoTopicosResponse listarTopicos() {
        try {
            Set<String> topicos = adminClient.listTopics(new ListTopicsOptions().listInternal(false))
                    .names()
                    .get();

            List<String> ordenados = topicos.stream().sorted().collect(Collectors.toList());
            log.info("[KAFKA-ADMIN] Tópicos listados exitosamente: {} tópicos encontrados", ordenados.size());
            return new ListadoTopicosResponse(ordenados.size(), ordenados);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operación interrumpida al listar tópicos", e);
        } catch (ExecutionException e) {
            throw desenvuelve(e);
        }
    }

    /**
     * Crea un nuevo tópico validando que el factor de replicación no supere los brokers disponibles
     * y calculando min.insync.replicas de forma segura: min(2, factorReplicacion).
     */
    public MensajeResponse crearTopico(TopicRequestDTO request) {
        try {
            // Validar número de brokers activos
            int totalBrokers = adminClient.describeCluster().nodes().get().size();
            if (request.getFactorReplicacion() > totalBrokers) {
                throw new InvalidReplicationFactorException(String.format(
                        "El factor de replicación (%d) no puede ser mayor que el número de brokers disponibles en el clúster (%d)",
                        request.getFactorReplicacion(), totalBrokers));
            }

            NewTopic nuevoTopico = new NewTopic(
                    request.getNombre(),
                    request.getParticiones(),
                    (short) request.getFactorReplicacion()
            );

            // min.insync.replicas = min(2, factorReplicacion)
            int minIsr = Math.min(2, request.getFactorReplicacion());

            Map<String, String> configs = new HashMap<>();
            configs.put("retention.ms", String.valueOf(request.getRetentionMs()));
            configs.put("cleanup.policy", request.getCleanupPolicy());
            configs.put("min.insync.replicas", String.valueOf(minIsr));
            nuevoTopico.configs(configs);

            adminClient.createTopics(Collections.singletonList(nuevoTopico)).all().get();

            log.info("[KAFKA-ADMIN] ✅ Tópico creado: nombre={}, particiones={}, réplicas={}, minIsr={}",
                    request.getNombre(), request.getParticiones(), request.getFactorReplicacion(), minIsr);
            return new MensajeResponse("Tópico creado exitosamente", request.getNombre());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operación interrumpida al crear tópico", e);
        } catch (ExecutionException e) {
            throw desenvuelve(e);
        }
    }

    /**
     * Inspecciona a fondo un tópico: particiones, líderes, réplicas, ISR y configuraciones personalizadas.
     */
    public TopicoDetalleResponse inspeccionarTopico(String nombreTopico) {
        try {
            TopicDescription descripcion = adminClient
                    .describeTopics(Collections.singletonList(nombreTopico))
                    .topicNameValues()
                    .get(nombreTopico)
                    .get();

            ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombreTopico);
            Config config = adminClient
                    .describeConfigs(Collections.singletonList(recurso))
                    .all()
                    .get()
                    .get(recurso);

            List<ParticionDetalleResponse> particiones = new ArrayList<>();
            for (TopicPartitionInfo p : descripcion.partitions()) {
                particiones.add(new ParticionDetalleResponse(
                        p.partition(),
                        formatNode(p.leader()),
                        p.replicas().stream().map(this::formatNode).collect(Collectors.toList()),
                        p.isr().stream().map(this::formatNode).collect(Collectors.toList()),
                        p.isr().size()
                ));
            }

            Map<String, String> configsPersonalizadas = new LinkedHashMap<>();
            for (ConfigEntry entry : config.entries()) {
                if (!entry.isDefault()) {
                    configsPersonalizadas.put(entry.name(), entry.value());
                }
            }

            log.info("[KAFKA-ADMIN] Tópico inspeccionado: {}", nombreTopico);
            return new TopicoDetalleResponse(
                    descripcion.name(),
                    descripcion.topicId().toString(),
                    descripcion.isInternal(),
                    descripcion.partitions().size(),
                    particiones,
                    configsPersonalizadas
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operación interrumpida al inspeccionar tópico", e);
        } catch (ExecutionException e) {
            throw desenvuelve(e);
        }
    }

    /**
     * Modifica las configuraciones dinámicas de un tópico usando incrementalAlterConfigs.
     * Solo acepta claves permitidas y valida sus valores.
     */
    public MensajeResponse modificarConfiguracion(String nombreTopico, Map<String, String> configs) {
        if (configs == null || configs.isEmpty()) {
            throw new IllegalArgumentException("El mapa de configuraciones no puede estar vacío");
        }

        // Validar que el tópico exista
        inspeccionarTopico(nombreTopico);

        List<AlterConfigOp> ops = new ArrayList<>();
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String clave = entry.getKey();
            String valor = entry.getValue();

            if (!CLAVES_CONFIG_PERMITIDAS.contains(clave)) {
                throw new InvalidConfigurationException(String.format(
                        "La clave de configuración '%s' no está permitida. Permitidas: %s",
                        clave, CLAVES_CONFIG_PERMITIDAS));
            }

            validarValorConfig(clave, valor);
            ops.add(new AlterConfigOp(new ConfigEntry(clave, valor), AlterConfigOp.OpType.SET));
        }

        try {
            ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombreTopico);
            adminClient.incrementalAlterConfigs(Map.of(recurso, ops)).all().get();

            log.info("[KAFKA-ADMIN] ✅ Configuraciones modificadas para tópico {}: {}", nombreTopico, configs);
            return new MensajeResponse("Configuración del tópico actualizada exitosamente", nombreTopico);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operación interrumpida al modificar configuración de tópico", e);
        } catch (ExecutionException e) {
            throw desenvuelve(e);
        }
    }

    /**
     * Incrementa el número de particiones de un tópico existente. Nunca permite reducirlas.
     */
    public MensajeResponse aumentarParticiones(String nombreTopico, int nuevoTotalParticiones) {
        TopicoDetalleResponse actual = inspeccionarTopico(nombreTopico);
        if (nuevoTotalParticiones <= actual.totalParticiones()) {
            throw new InvalidPartitionsException(String.format(
                    "El nuevo número de particiones (%d) debe ser estrictamente mayor al actual (%d)",
                    nuevoTotalParticiones, actual.totalParticiones()));
        }

        try {
            Map<String, NewPartitions> nuevas = Map.of(
                    nombreTopico, NewPartitions.increaseTo(nuevoTotalParticiones)
            );
            adminClient.createPartitions(nuevas).all().get();

            log.info("[KAFKA-ADMIN] ✅ Particiones aumentadas para tópico {}: {} -> {}",
                    nombreTopico, actual.totalParticiones(), nuevoTotalParticiones);
            return new MensajeResponse(
                    String.format("Particiones aumentadas a %d exitosamente", nuevoTotalParticiones),
                    nombreTopico
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operación interrumpida al aumentar particiones", e);
        } catch (ExecutionException e) {
            throw desenvuelve(e);
        }
    }

    /**
     * Elimina un tópico. Si es un tópico del sistema protegido (KafkaTopics.TOPICOS_PROTEGIDOS),
     * requiere el parámetro force=true para evitar eliminaciones accidentales en producción.
     */
    public MensajeResponse eliminarTopico(String nombreTopico, boolean force) {
        if (KafkaTopics.TOPICOS_PROTEGIDOS.contains(nombreTopico) && !force) {
            throw new IllegalArgumentException(String.format(
                    "El tópico '%s' pertenece a la infraestructura central de la plataforma y está protegido. " +
                    "Use el parámetro ?force=true si realmente desea eliminarlo.", nombreTopico));
        }

        try {
            adminClient.deleteTopics(Collections.singletonList(nombreTopico)).all().get();
            log.info("[KAFKA-ADMIN] ✅ Tópico eliminado exitosamente: {}", nombreTopico);
            return new MensajeResponse("Tópico eliminado exitosamente", nombreTopico);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operación interrumpida al eliminar tópico", e);
        } catch (ExecutionException e) {
            throw desenvuelve(e);
        }
    }

    // ========================================================================================
    // CONSUMER GROUPS
    // ========================================================================================

    /**
     * Lista todos los consumer groups activos y su estado.
     */
    public List<ConsumerGroupItemResponse> listarConsumerGroups() {
        try {
            Collection<ConsumerGroupListing> listings = adminClient.listConsumerGroups().all().get();
            return listings.stream()
                    .map(g -> new ConsumerGroupItemResponse(
                            g.groupId(),
                            g.state().isPresent() ? g.state().get().toString() : "UNKNOWN",
                            g.isSimpleConsumerGroup()
                    ))
                    .sorted(Comparator.comparing(ConsumerGroupItemResponse::groupId))
                    .collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operación interrumpida al listar consumer groups", e);
        } catch (ExecutionException e) {
            throw desenvuelve(e);
        }
    }

    /**
     * Describe a fondo un consumer group: miembros, coordinador, particiones asignadas y lag por partición.
     */
    public ConsumerGroupDetalleResponse describirConsumerGroup(String groupId) {
        try {
            ConsumerGroupDescription desc = adminClient
                    .describeConsumerGroups(Collections.singletonList(groupId))
                    .describedGroups()
                    .get(groupId)
                    .get();

            // Offsets confirmados
            Map<TopicPartition, OffsetAndMetadata> offsets = adminClient
                    .listConsumerGroupOffsets(groupId)
                    .partitionsToOffsetAndMetadata()
                    .get();

            // Calcular Log-End-Offset para cada partición consultada
            List<OffsetLagResponse> offsetsLag = new ArrayList<>();
            long lagTotal = 0;

            if (offsets != null && !offsets.isEmpty()) {
                Map<TopicPartition, OffsetSpec> requestOffsets = new HashMap<>();
                for (TopicPartition tp : offsets.keySet()) {
                    requestOffsets.put(tp, OffsetSpec.latest());
                }

                Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> endOffsets =
                        adminClient.listOffsets(requestOffsets).all().get();

                for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                    TopicPartition tp = entry.getKey();
                    long current = entry.getValue() != null ? entry.getValue().offset() : 0;
                    long end = endOffsets.containsKey(tp) ? endOffsets.get(tp).offset() : current;
                    long lag = Math.max(0, end - current);
                    lagTotal += lag;

                    offsetsLag.add(new OffsetLagResponse(
                            tp.topic(),
                            tp.partition(),
                            current,
                            end,
                            lag
                    ));
                }
            }

            List<MiembroGroupResponse> miembros = new ArrayList<>();
            for (MemberDescription m : desc.members()) {
                List<String> partes = m.assignment().topicPartitions().stream()
                        .map(tp -> tp.topic() + "-" + tp.partition())
                        .collect(Collectors.toList());
                miembros.add(new MiembroGroupResponse(
                        m.consumerId(),
                        m.clientId(),
                        m.host(),
                        partes
                ));
            }

            return new ConsumerGroupDetalleResponse(
                    desc.groupId(),
                    desc.state().toString(),
                    formatNode(desc.coordinator()),
                    desc.partitionAssignor(),
                    miembros,
                    offsetsLag,
                    lagTotal
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operación interrumpida al describir consumer group", e);
        } catch (ExecutionException e) {
            throw desenvuelve(e);
        }
    }

    /**
     * Elimina un consumer group (debe estar vacío).
     */
    public MensajeResponse eliminarConsumerGroup(String groupId) {
        try {
            adminClient.deleteConsumerGroups(Collections.singletonList(groupId)).all().get();
            log.info("[KAFKA-ADMIN] ✅ Consumer group eliminado: {}", groupId);
            return new MensajeResponse("Consumer group eliminado exitosamente", groupId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operación interrumpida al eliminar consumer group", e);
        } catch (ExecutionException e) {
            throw desenvuelve(e);
        }
    }

    // ========================================================================================
    // CLÚSTER
    // ========================================================================================

    /**
     * Retorna información del clúster de Kafka: clusterId, controlador y lista de brokers.
     */
    public ClusterInfoResponse obtenerInfoCluster() {
        try {
            DescribeClusterResult cluster = adminClient.describeCluster();
            String clusterId = cluster.clusterId().get();
            Node controller = cluster.controller().get();
            Collection<Node> nodes = cluster.nodes().get();

            List<BrokerInfoResponse> brokers = nodes.stream()
                    .map(n -> new BrokerInfoResponse(n.id(), n.host(), n.port(), n.rack()))
                    .sorted(Comparator.comparingInt(BrokerInfoResponse::id))
                    .collect(Collectors.toList());

            return new ClusterInfoResponse(
                    clusterId,
                    formatNode(controller),
                    brokers.size(),
                    brokers
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operación interrumpida al obtener info del clúster", e);
        } catch (ExecutionException e) {
            throw desenvuelve(e);
        }
    }

    // ========================================================================================
    // UTILIDADES PRIVADAS
    // ========================================================================================

    private void validarValorConfig(String clave, String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(String.format("El valor para la configuración '%s' no puede estar vacío", clave));
        }

        switch (clave) {
            case "retention.ms", "retention.bytes", "segment.ms", "segment.bytes", "max.message.bytes" -> {
                try {
                    long v = Long.parseLong(valor);
                    if (v < -1) {
                        throw new IllegalArgumentException(String.format("El valor de '%s' debe ser >= -1", clave));
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(String.format("El valor de '%s' debe ser un número entero válido", clave));
                }
            }
            case "min.insync.replicas" -> {
                try {
                    int v = Integer.parseInt(valor);
                    if (v < 1) {
                        throw new IllegalArgumentException("min.insync.replicas debe ser >= 1");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("min.insync.replicas debe ser un entero positivo");
                }
            }
            case "cleanup.policy" -> {
                if (!valor.equals("delete") && !valor.equals("compact") && !valor.equals("compact,delete")) {
                    throw new IllegalArgumentException("cleanup.policy debe ser 'delete', 'compact' o 'compact,delete'");
                }
            }
        }
    }

    private RuntimeException desenvuelve(ExecutionException e) {
        Throwable causa = e.getCause();
        if (causa instanceof RuntimeException re) {
            return re;
        }
        return new RuntimeException(causa != null ? causa.getMessage() : e.getMessage(), causa);
    }

    private String formatNode(Node node) {
        if (node == null) return "N/A";
        return String.format("broker-%d (%s:%d)", node.id(), node.host(), node.port());
    }
}

