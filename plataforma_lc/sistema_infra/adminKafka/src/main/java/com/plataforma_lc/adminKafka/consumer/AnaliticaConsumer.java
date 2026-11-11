package com.plataforma_lc.adminKafka.consumer;

import com.plataforma_lc.adminKafka.config.KafkaTopics;
import com.plataforma_lc.adminKafka.dto.AnaliticaStreamDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumidor Kafka para el stream de analítica de asistencia.
 * Escucha el tópico {@link KafkaTopics#ASISTENCIA_ANALITICA_STREAM} con el consumer group
 * "plataforma-analitica-group". Incluye manejo explícito de excepciones
 * para prevenir la detención del consumidor ante mensajes defectuosos.
 */
@Slf4j
@Service
public class AnaliticaConsumer {

    @KafkaListener(
            topics = KafkaTopics.ASISTENCIA_ANALITICA_STREAM,
            groupId = "plataforma-analitica-group",
            properties = {
                    "spring.json.value.default.type=com.plataforma_lc.adminKafka.dto.AnaliticaStreamDTO"
            }
    )
    public void consumir(ConsumerRecord<String, AnaliticaStreamDTO> record) {
        try {
            AnaliticaStreamDTO dto = record.value();
            log.info("[ANALITICA-CONSUMER] ✅ Stream recibido -> topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), record.key());
            log.info("[ANALITICA-CONSUMER] 📊 Datos: cursoId={}, estudianteId={}, fecha={}, estado={}, timestamp={}",
                    dto.getCursoId(),
                    dto.getEstudianteId(),
                    dto.getFecha(),
                    dto.getEstado(),
                    dto.getTimestamp());

            // Aquí se procesaría el registro: agregaciones, métricas, dashboards, etc.

        } catch (Exception e) {
            log.error("[ANALITICA-CONSUMER] ❌ Error procesando stream -> topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e);
            // El consumidor continúa procesando el siguiente mensaje sin detenerse
        }
    }
}

