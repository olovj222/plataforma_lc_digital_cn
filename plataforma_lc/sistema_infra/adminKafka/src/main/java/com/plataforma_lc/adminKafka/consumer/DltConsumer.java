package com.plataforma_lc.adminKafka.consumer;

import com.plataforma_lc.adminKafka.config.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Consumidor observable para los tópicos Dead Letter (DLT) de la plataforma.
 * Escucha los mensajes derivados por fallas de validación o deserialización,
 * registrando en nivel WARN todos los metadatos forenses y el payload crudo truncado.
 */
@Slf4j
@Service
public class DltConsumer {

    @KafkaListener(
            topics = {
                    KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS_DLT,
                    KafkaTopics.ASISTENCIA_ANALITICA_STREAM_DLT,
                    KafkaTopics.PLATAFORMA_ALERTAS_CRITICAS_DLT
            },
            groupId = "plataforma-dlt-group",
            containerFactory = "dltKafkaListenerContainerFactory"
    )
    public void consumirDlt(
            ConsumerRecord<String, byte[]> record,
            @Header(name = KafkaHeaders.DLT_ORIGINAL_TOPIC, required = false) String originalTopic,
            @Header(name = KafkaHeaders.DLT_ORIGINAL_PARTITION, required = false) Integer originalPartition,
            @Header(name = KafkaHeaders.DLT_ORIGINAL_OFFSET, required = false) Long originalOffset,
            @Header(name = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exceptionMessage,
            @Header(name = KafkaHeaders.DLT_EXCEPTION_FQCN, required = false) String exceptionFqcn) {

        byte[] payloadBytes = record.value();
        String payloadStr = (payloadBytes != null) ? new String(payloadBytes, StandardCharsets.UTF_8) : "<null>";

        // Truncar payload a 1000 caracteres
        String payloadTruncado = payloadStr.length() > 1000 ? payloadStr.substring(0, 1000) + "... [TRUNCADO]" : payloadStr;

        log.warn("[KAFKA-DLT-OBSERVABLE] ⚠️ Mensaje en Dead Letter Topic! " +
                        "dltTopic={}, dltPart={}, dltOffset={}, " +
                        "origTopic={}, origPart={}, origOffset={}, " +
                        "exceptionFqcn={}, exceptionMsg={}, " +
                        "payload={}",
                record.topic(), record.partition(), record.offset(),
                originalTopic, originalPartition, originalOffset,
                exceptionFqcn, exceptionMessage,
                payloadTruncado);
    }

    /**
     * Configuración del contenedor listener para DLT con deserialización cruda (byte[]).
     */
    @Configuration
    static class DltContainerConfig {

        @Value("${spring.kafka.bootstrap-servers}")
        private String bootstrapServers;

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, byte[]> dltKafkaListenerContainerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

            DefaultKafkaConsumerFactory<String, byte[]> consumerFactory = new DefaultKafkaConsumerFactory<>(props);
            ConcurrentKafkaListenerContainerFactory<String, byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            return factory;
        }
    }
}
