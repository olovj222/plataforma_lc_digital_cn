package com.plataforma_lc.adminKafka.consumer;

import com.plataforma_lc.adminKafka.config.KafkaTopics;
import com.plataforma_lc.adminKafka.dto.AuditoriaEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumidor Kafka para eventos de auditoría.
 * Escucha el tópico {@link KafkaTopics#PLATAFORMA_AUDITORIA_EVENTOS} con el consumer group
 * "plataforma-auditoria-group". Incluye manejo explícito de excepciones
 * para prevenir la detención del consumidor ante mensajes defectuosos.
 */
@Slf4j
@Service
public class AuditoriaConsumer {

    @KafkaListener(
            topics = KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS,
            groupId = "plataforma-auditoria-group",
            properties = {
                    "spring.json.value.default.type=com.plataforma_lc.adminKafka.dto.AuditoriaEventDTO"
            }
    )
    public void consumir(ConsumerRecord<String, AuditoriaEventDTO> record) {
        try {
            AuditoriaEventDTO evento = record.value();
            log.info("[AUDITORIA-CONSUMER] ✅ Evento recibido -> topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), record.key());
            log.info("[AUDITORIA-CONSUMER] 📋 Detalle: servicio={}, accion={}, usuario={}, detalle={}, timestamp={}",
                    evento.getServicio(),
                    evento.getAccion(),
                    evento.getUsuario(),
                    evento.getDetalle(),
                    evento.getTimestamp());

            // Aquí se procesaría el evento: persistencia, indexación, etc.

        } catch (Exception e) {
            log.error("[AUDITORIA-CONSUMER] ❌ Error procesando mensaje -> topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e);
            // El consumidor continúa procesando el siguiente mensaje sin detenerse
        }
    }
}

