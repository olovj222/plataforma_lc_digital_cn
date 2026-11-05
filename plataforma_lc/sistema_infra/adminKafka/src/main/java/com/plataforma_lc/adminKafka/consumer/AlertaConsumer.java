package com.plataforma_lc.adminKafka.consumer;

import com.plataforma_lc.adminKafka.dto.AlertaEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumidor Kafka para alertas críticas de la plataforma.
 * Escucha el tópico "plataforma.alertas.criticas" con el consumer group
 * "plataforma-alertas-group". Incluye manejo explícito de excepciones
 * para prevenir la detención del consumidor ante mensajes defectuosos.
 */
@Slf4j
@Service
public class AlertaConsumer {

    @KafkaListener(
            topics = "plataforma.alertas.criticas",
            groupId = "plataforma-alertas-group",
            properties = {
                    "spring.json.value.default.type=com.plataforma_lc.adminKafka.dto.AlertaEventDTO"
            }
    )
    public void consumir(ConsumerRecord<String, AlertaEventDTO> record) {
        try {
            AlertaEventDTO evento = record.value();
            log.info("[ALERTA-CONSUMER] ✅ Alerta recibida -> topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), record.key());

            // Logging diferenciado por nivel de severidad
            switch (evento.getNivel()) {
                case "CRITICAL":
                    log.error("[ALERTA-CONSUMER] 🚨 ALERTA CRÍTICA: alertaId={}, tipo={}, mensaje={}, origen={}, estudianteId={}, cursoId={}",
                            evento.getAlertaId(), evento.getTipoAlerta(), evento.getMensaje(),
                            evento.getOrigenServicio(), evento.getEstudianteId(), evento.getCursoId());
                    break;
                case "WARNING":
                    log.warn("[ALERTA-CONSUMER] ⚠️ ADVERTENCIA: alertaId={}, tipo={}, mensaje={}, origen={}",
                            evento.getAlertaId(), evento.getTipoAlerta(), evento.getMensaje(),
                            evento.getOrigenServicio());
                    break;
                default: // INFO
                    log.info("[ALERTA-CONSUMER] ℹ️ INFO: alertaId={}, tipo={}, mensaje={}, origen={}",
                            evento.getAlertaId(), evento.getTipoAlerta(), evento.getMensaje(),
                            evento.getOrigenServicio());
                    break;
            }

            // Aquí se procesaría la alerta: notificaciones, escalado, etc.

        } catch (Exception e) {
            log.error("[ALERTA-CONSUMER] ❌ Error procesando alerta -> topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e);
            // El consumidor continúa procesando el siguiente mensaje sin detenerse
        }
    }
}

