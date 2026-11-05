package com.plataforma_lc.adminKafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declaración programática de los 3 tópicos principales de la plataforma.
 * Se crean al arrancar la aplicación utilizando TopicBuilder.
 * Si los tópicos ya existen, Spring Kafka los ignora sin error.
 */
@Configuration
public class KafkaTopicConfig {

    /**
     * Tópico de auditoría para eventos del sistema.
     * Retención: 7 días (604800000 ms)
     */
    @Bean
    public NewTopic auditoriaEventosTopic() {
        return TopicBuilder.name("plataforma.auditoria.eventos")
                .partitions(3)
                .replicas(3)
                .config("retention.ms", "604800000")        // 7 días
                .config("min.insync.replicas", "2")
                .build();
    }

    /**
     * Tópico de analítica de asistencia (stream de datos).
     * Retención: 30 días (2592000000 ms)
     */
    @Bean
    public NewTopic analiticaStreamTopic() {
        return TopicBuilder.name("asistencia.analitica.stream")
                .partitions(3)
                .replicas(3)
                .config("retention.ms", "2592000000")       // 30 días
                .config("min.insync.replicas", "2")
                .build();
    }

    /**
     * Tópico de alertas críticas de la plataforma.
     * Retención: 14 días (1209600000 ms)
     */
    @Bean
    public NewTopic alertasCriticasTopic() {
        return TopicBuilder.name("plataforma.alertas.criticas")
                .partitions(3)
                .replicas(3)
                .config("retention.ms", "1209600000")       // 14 días
                .config("min.insync.replicas", "2")
                .build();
    }
}

