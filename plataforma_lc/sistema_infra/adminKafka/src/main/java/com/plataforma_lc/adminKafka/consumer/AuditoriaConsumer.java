package com.plataforma_lc.adminKafka.consumer;

import com.plataforma_lc.adminKafka.config.KafkaTopics;
import com.plataforma_lc.adminKafka.dto.AuditoriaEventDTO;
import com.plataforma_lc.adminKafka.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumidor Kafka para eventos de auditoría.
 * Escucha el tópico {@link KafkaTopics#PLATAFORMA_AUDITORIA_EVENTOS} con el
 * grupo
 * "plataforma-auditoria-group".
 * <p>
 * Escalabilidad horizontal:
 * El tópico cuenta con 3 particiones. Se pueden desplegar hasta 3 instancias
 * activas
 * de este microservicio (o concurrencia = 3) asignando exactamente 1 partición
 * por consumidor.
 * Instancias adicionales quedarán inactivas en reserva (failover).
 * Las excepciones no se capturan aquí para permitir que DefaultErrorHandler
 * reintente o desvíe al DLT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaConsumer {

    private final AuditoriaService auditoriaService;

    @KafkaListener(topics = KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS, groupId = "plataforma-auditoria-group", concurrency = "${app.kafka.consumer.concurrency:1}")
    public void consumir(ConsumerRecord<String, AuditoriaEventDTO> record) {
        log.info("[AUDITORIA-CONSUMER] Mensaje recibido -> topic={}, partition={}, offset={}, key={}",
                record.topic(), record.partition(), record.offset(), record.key());

        auditoriaService.procesar(record.value());

        log.info("[AUDITORIA-CONSUMER] ✅ Evento de auditoría procesado exitosamente -> key={}", record.key());
    }
}
