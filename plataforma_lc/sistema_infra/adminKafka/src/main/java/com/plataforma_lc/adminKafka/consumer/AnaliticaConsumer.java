package com.plataforma_lc.adminKafka.consumer;

import com.plataforma_lc.adminKafka.config.KafkaTopics;
import com.plataforma_lc.adminKafka.dto.AnaliticaStreamDTO;
import com.plataforma_lc.adminKafka.service.AnaliticaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumidor Kafka para el flujo de analítica de asistencia.
 * Escucha el tópico {@link KafkaTopics#ASISTENCIA_ANALITICA_STREAM} con el
 * grupo
 * "plataforma-analitica-group".
 * <p>
 * Escalabilidad horizontal:
 * Con 3 particiones en el clúster, el grupo soporta hasta 3 hilos/instancias
 * concurrentes
 * distribuyendo las cargas de forma balanceada.
 * Las excepciones se propagan directamente hacia DefaultErrorHandler.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnaliticaConsumer {

    private final AnaliticaService analiticaService;

    @KafkaListener(topics = KafkaTopics.ASISTENCIA_ANALITICA_STREAM, groupId = "plataforma-analitica-group", concurrency = "${app.kafka.consumer.concurrency:1}")
    public void consumir(ConsumerRecord<String, AnaliticaStreamDTO> record) {
        log.info("[ANALITICA-CONSUMER] Mensaje recibido -> topic={}, partition={}, offset={}, key={}",
                record.topic(), record.partition(), record.offset(), record.key());

        analiticaService.procesar(record.value());

        log.info("[ANALITICA-CONSUMER] ✅ Registro de analítica procesado exitosamente -> key={}", record.key());
    }
}
