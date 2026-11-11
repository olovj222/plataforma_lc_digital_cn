package com.plataforma_lc.adminKafka.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración del AdminClient de Kafka y plantillas auxiliares.
 * Instancia el bean AdminClient conectado al clúster de brokers
 * y la plantilla de bytes para el recuperador DeadLetterPublishingRecoverer.
 */
@Configuration
public class KafkaAdminConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public AdminClient adminClient() {
        return AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000",
                AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "10000"
        ));
    }

    /**
     * Factoría y KafkaTemplate de byte[] para publicar los payloads originales sin re-serializar
     * en caso de mensajes corruptos dirigidos a tópicos DLT.
     */
    @Bean
    public ProducerFactory<byte[], byte[]> bytesProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<byte[], byte[]> bytesKafkaTemplate() {
        return new KafkaTemplate<>(bytesProducerFactory());
    }
}
