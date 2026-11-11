package com.plataforma_lc.adminKafka.dto;

import java.util.List;
import java.util.Map;

/**
 * Registros inmutables (records) para respuestas uniformes de la API administrativa de Kafka.
 */
public final class KafkaResponses {

    private KafkaResponses() {}

    /**
     * Respuesta simple con mensaje descriptivo y detalles adicionales opcionales.
     */
    public record MensajeResponse(
            String mensaje,
            String recurso
    ) {}

    /**
     * Respuesta al listar tópicos del clúster.
     */
    public record ListadoTopicosResponse(
            int totalTopicos,
            List<String> topicos
    ) {}

    /**
     * Respuesta detallada de la inspección de un tópico.
     */
    public record TopicoDetalleResponse(
            String nombre,
            String topicId,
            boolean esInterno,
            int totalParticiones,
            List<ParticionDetalleResponse> particiones,
            Map<String, String> configuracionesPersonalizadas
    ) {}

    /**
     * Información de una partición de un tópico.
     */
    public record ParticionDetalleResponse(
            int particion,
            String lider,
            List<String> replicas,
            List<String> isr,
            int replicasEnSync
    ) {}

    /**
     * Respuesta resumen al listar consumer groups.
     */
    public record ConsumerGroupItemResponse(
            String groupId,
            String estado,
            boolean esSimple
    ) {}

    /**
     * Detalle completo de un consumer group incluyendo miembros, asignaciones y lag por partición.
     */
    public record ConsumerGroupDetalleResponse(
            String groupId,
            String estado,
            String coordinador,
            String tipoParticionador,
            List<MiembroGroupResponse> miembros,
            List<OffsetLagResponse> particionesOffsetLag,
            long lagTotal
    ) {}

    /**
     * Información de un miembro activo en el consumer group.
     */
    public record MiembroGroupResponse(
            String memberId,
            String clientId,
            String host,
            List<String> particionesAsignadas
    ) {}

    /**
     * Detalle de offset, log-end-offset y lag calculado para una partición.
     */
    public record OffsetLagResponse(
            String topic,
            int particion,
            long currentOffset,
            long logEndOffset,
            long lag
    ) {}

    /**
     * Información del clúster de Kafka (brokers, controlador, clusterId).
     */
    public record ClusterInfoResponse(
            String clusterId,
            String controller,
            int totalBrokers,
            List<BrokerInfoResponse> brokers
    ) {}

    /**
     * Datos individuales de un broker en el clúster.
     */
    public record BrokerInfoResponse(
            int id,
            String host,
            int port,
            String rack
    ) {}
}
