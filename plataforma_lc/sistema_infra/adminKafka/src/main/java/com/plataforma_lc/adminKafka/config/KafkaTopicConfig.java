package com.plataforma_lc.adminKafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declaración programática de los 3 tópicos principales y sus respectivos DLT (Dead Letter Topics).
 * <p>
 * Justificación de políticas y tiempos de retención:
 * <ul>
 *   <li><b>plataforma.auditoria.eventos:</b> Retención de 7 días (604800000 ms) con política {@code cleanup.policy=delete}.
 *       Los eventos de auditoría se consumen rápidamente para indexación y trazabilidad operativa;
 *       una ventana de 7 días permite recuperar eventos en caso de caídas temporales de servicios downstream.</li>
 *   <li><b>asistencia.analitica.stream:</b> Retención de 30 días (2592000000 ms) con política {@code cleanup.policy=delete}.
 *       Corresponde al ciclo mensual académico para consolidación de métricas, reportes de asistencia y recálculo
 *       de resúmenes analíticos periódicos.</li>
 *   <li><b>plataforma.alertas.criticas:</b> Retención de 14 días (1209600000 ms) con política {@code cleanup.policy=delete}.
 *       Ventana de dos semanas suficiente para gestionar incidentes, alertas activas no atendidas y seguimiento
 *       de escalamientos de soporte o casos de estudiantes en riesgo.</li>
 *   <li><b>Tópicos DLT (.DLT):</b> Retención de 30 días (2592000000 ms) y {@code cleanup.policy=delete}.
 *       Mantiene los mensajes defectuosos o no procesables durante un mes para diagnóstico forense,
 *       inspección de causas raíz y reprocesamiento manual sin pérdida de información.</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final KafkaTopicProperties topicProperties;

    // ========================================================================================
    // TÓPICOS PRINCIPALES
    // ========================================================================================

    /**
     * Tópico de auditoría para eventos del sistema.
     * Retención: 7 días (604800000 ms), cleanup.policy=delete.
     */
    @Bean
    public NewTopic auditoriaEventosTopic() {
        return TopicBuilder.name(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS)
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .config("retention.ms", "604800000")        // 7 días
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", String.valueOf(topicProperties.getMinInsyncReplicas()))
                .build();
    }

    /**
     * Tópico de analítica de asistencia (stream continuo de registros).
     * Retención: 30 días (2592000000 ms), cleanup.policy=delete.
     */
    @Bean
    public NewTopic analiticaStreamTopic() {
        return TopicBuilder.name(KafkaTopics.ASISTENCIA_ANALITICA_STREAM)
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .config("retention.ms", "2592000000")       // 30 días
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", String.valueOf(topicProperties.getMinInsyncReplicas()))
                .build();
    }

    /**
     * Tópico de alertas críticas de la plataforma.
     * Retención: 14 días (1209600000 ms), cleanup.policy=delete.
     */
    @Bean
    public NewTopic alertasCriticasTopic() {
        return TopicBuilder.name(KafkaTopics.PLATAFORMA_ALERTAS_CRITICAS)
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .config("retention.ms", "1209600000")       // 14 días
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", String.valueOf(topicProperties.getMinInsyncReplicas()))
                .build();
    }

    // ========================================================================================
    // TÓPICOS DEAD LETTER (DLT)
    // ========================================================================================

    /**
     * Tópico DLT para eventos fallidos de auditoría.
     * Retención: 30 días (2592000000 ms), cleanup.policy=delete.
     */
    @Bean
    public NewTopic auditoriaEventosDltTopic() {
        return TopicBuilder.name(KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS_DLT)
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .config("retention.ms", "2592000000")       // 30 días
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", String.valueOf(topicProperties.getMinInsyncReplicas()))
                .build();
    }

    /**
     * Tópico DLT para stream fallido de analítica de asistencia.
     * Retención: 30 días (2592000000 ms), cleanup.policy=delete.
     */
    @Bean
    public NewTopic analiticaStreamDltTopic() {
        return TopicBuilder.name(KafkaTopics.ASISTENCIA_ANALITICA_STREAM_DLT)
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .config("retention.ms", "2592000000")       // 30 días
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", String.valueOf(topicProperties.getMinInsyncReplicas()))
                .build();
    }

    /**
     * Tópico DLT para alertas críticas fallidas.
     * Retención: 30 días (2592000000 ms), cleanup.policy=delete.
     */
    @Bean
    public NewTopic alertasCriticasDltTopic() {
        return TopicBuilder.name(KafkaTopics.PLATAFORMA_ALERTAS_CRITICAS_DLT)
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .config("retention.ms", "2592000000")       // 30 días
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", String.valueOf(topicProperties.getMinInsyncReplicas()))
                .build();
    }
}
