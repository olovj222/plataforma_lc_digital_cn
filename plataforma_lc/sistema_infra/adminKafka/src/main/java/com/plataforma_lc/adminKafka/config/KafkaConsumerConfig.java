package com.plataforma_lc.adminKafka.config;

import com.plataforma_lc.adminKafka.exception.EventoInvalidoException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.converter.MessageConversionException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuración de resiliencia y manejo de errores para consumidores Kafka.
 * Define el {@link DefaultErrorHandler} con
 * {@link DeadLetterPublishingRecoverer} hacia tópicos .DLT
 * y política de reintentos con backoff exponencial.
 */
@Slf4j
@Configuration
public class KafkaConsumerConfig {

        @Value("${app.kafka.consumer.concurrency:1}")
        private int concurrency;

        /**
         * Template para publicar al DLT los bytes originales de mensajes que no se
         * pudieron deserializar.
         * La KEY debe serializarse como String (los consumidores usan
         * StringDeserializer para la key);
         * solo el VALUE se envía como byte[]. Con ByteArraySerializer en la key, el
         * envío al DLT
         * fallaba con ClassCastException (String -> byte[]) y el mensaje venenoso se
         * reintentaba sin fin.
         */
        @Bean
        public KafkaTemplate<String, byte[]> dltBytesKafkaTemplate(KafkaTemplate<String, Object> kafkaTemplate) {
                Map<String, Object> props = new HashMap<>(
                                kafkaTemplate.getProducerFactory().getConfigurationProperties());
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
                return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
        }

        /**
         * Handler de errores por defecto para los contenedores de escucha Kafka.
         * Utiliza un recuperador que reenvía a <topico>.DLT y un backoff exponencial de
         * 3 reintentos.
         * Excepciones fatales (deserialización, conversión o evento inválido) se omiten
         * del reintento
         * y se dirigen directamente al Dead Letter Topic.
         */
        @Bean
        public DefaultErrorHandler errorHandler(
                        KafkaTemplate<String, Object> kafkaTemplate,
                        @Qualifier("dltBytesKafkaTemplate") KafkaTemplate<String, byte[]> dltBytesKafkaTemplate) {

                // El ORDEN importa: el recoverer usa la primera plantilla cuyo tipo sea
                // compatible con el valor.
                // byte[] debe ir antes que Object (Object coincide con todo).
                Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
                templates.put(byte[].class, dltBytesKafkaTemplate);
                templates.put(Object.class, kafkaTemplate);

                DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                                templates,
                                (record, ex) -> {
                                        String dltTopic = record.topic() + ".DLT";
                                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                                        log.warn("[KAFKA-DLT-RECOVERER] Reenviando mensaje fallido de topic={} part={} offset={} hacia DLT={}. Razón: {}",
                                                        record.topic(), record.partition(), record.offset(), dltTopic,
                                                        causa.getMessage());
                                        return new TopicPartition(dltTopic, record.partition());
                                });

                // Política de reintentos: 3 reintentos, inicial 1s, multiplicador 2.0 (1s, 2s,
                // 4s)
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
                                IllegalArgumentException.class);

                return errorHandler;
        }

        public int getConcurrency() {
                return concurrency;
        }
}