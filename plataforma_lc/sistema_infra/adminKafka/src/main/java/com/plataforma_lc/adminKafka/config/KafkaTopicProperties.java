package com.plataforma_lc.adminKafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración para la creación de tópicos en el clúster Kafka.
 * Permite parametrizar particiones, factor de replicación y min.insync.replicas
 * según el entorno (por ejemplo, RF=3 en producción con 3 brokers, o RF=1 en tests embebidos).
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.kafka.topics")
public class KafkaTopicProperties {

    /**
     * Número de particiones por defecto para tópicos creados automáticamente (por defecto 3).
     */
    private int partitions = 3;

    /**
     * Factor de replicación por defecto (por defecto 3 en clúster multi-broker).
     */
    private int replicas = 3;

    /**
     * Mínimo de réplicas en sincronía (min.insync.replicas) requerido para confirmar escrituras acks=all (por defecto 2).
     */
    private int minInsyncReplicas = 2;
}

