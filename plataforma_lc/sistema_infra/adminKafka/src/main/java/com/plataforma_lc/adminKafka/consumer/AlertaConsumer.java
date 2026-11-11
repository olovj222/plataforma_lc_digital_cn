package com.plataforma_lc.adminKafka.consumer;

import com.plataforma_lc.adminKafka.config.KafkaTopics;
import com.plataforma_lc.adminKafka.dto.AlertaEventDTO;
import com.plataforma_lc.adminKafka.service.AlertaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumidor Kafka para alertas críticas de la plataforma.
 * Escucha el tópico {@link KafkaTopics#PLATAFORMA_ALERTAS_CRITICAS} con el
 * grupo
 * "plataforma-alertas-group".
 * <p>
 * Escalabilidad horizontal:
 * Permite hasta 3 consumidores en el grupo "plataforma-alertas-group" para
 * balancear
 * las 3 particiones asignadas al tópico.
 * Las excepciones se delegan al DefaultErrorHandler sin bloques try/catch que
 * las oculten.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaConsumer {

    private final AlertaService alertaService;

    @KafkaListener(topics = KafkaTopics.PLATAFORMA_ALERTAS_CRITICAS, groupId = "plataforma-alertas-group", concurrency = "${app.kafka.consumer.concurrency:1}")
    public void consumir(ConsumerRecord<String, AlertaEventDTO> record) {
        log.info("[ALERTA-CONSUMER] Mensaje recibido -> topic={}, partition={}, offset={}, key={}",
                record.topic(), record.partition(), record.offset(), record.key());

        alertaService.procesar(record.value());

        log.info("[ALERTA-CONSUMER] ✅ Alerta procesada exitosamente -> key={}", record.key());
    }
}
