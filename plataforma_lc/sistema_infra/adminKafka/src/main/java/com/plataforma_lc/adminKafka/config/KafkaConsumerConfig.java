package com.plataforma_lc.adminKafka.config;

import com.plataforma_lc.adminKafka.exception.EventoInvalidoException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.converter.MessageConversionException;

import java.util.Map;

/**
 * Configuración de resiliencia y manejo de errores para consumidores Kafka.
 * Define el {@link DefaultErrorHandler} con {@link DeadLetterPublishingRecoverer} hacia tópicos .DLT
 * y política de reintentos con backoff exponencial.
 */
@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Value("${app.kafka.consumer.concurrency:1}")
    private int concurrency;

    /**
     * Handler de errores por defecto para los contenedores de escucha Kafka.
     * Utiliza un recuperador que reenvía a <topico>.DLT y un backoff exponencial de 3 reintentos.
     * Excepciones fatales (deserialización, conversión o evento inválido) se omiten del reintento
     * y se dirigen directamente al Dead Letter Topic.
     */
    @Bean
    public DefaultErrorHandler errorHandler(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTemplate<byte[], byte[]> bytesKafkaTemplate) {

        // DeadLetterPublishingRecoverer que publica los bytes originales para deserializaciones fallidas
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                Map.of(
                        Object.class, (KafkaOperations<?, ?>) kafkaTemplate,
                        byte[].class, (KafkaOperations<?, ?>) bytesKafkaTemplate
                ),
                (record, ex) -> {
                    String dltTopic = record.topic() + ".DLT";
                    log.warn("[KAFKA-DLT-RECOVERER] Reenviando mensaje fallido de topic={} part={} offset={} hacia DLT={}. Razón: {}",
                            record.topic(), record.partition(), record.offset(), dltTopic, ex.getMessage());
                    return new TopicPartition(dltTopic, record.partition());
                }
        );

        // Política de reintentos: 3 reintentos, inicial 1s, multiplicador 2.0 (1s, 2s, 4s)
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // Excepciones no reintentables: van directo al DLT sin consumir reintentos
        errorHandler.addNotRetryableExceptions(
                DeserializationException.class,
                MessageConversionException.class,
                EventoInvalidoException.class,
                IllegalArgumentException.class
        );

        return errorHandler;
    }

    public int getConcurrency() {
        return concurrency;
    }
}

