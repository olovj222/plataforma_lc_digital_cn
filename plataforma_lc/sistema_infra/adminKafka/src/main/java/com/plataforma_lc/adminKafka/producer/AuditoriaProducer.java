package com.plataforma_lc.adminKafka.producer;

import com.plataforma_lc.adminKafka.config.KafkaTopics;
import com.plataforma_lc.adminKafka.dto.AuditoriaEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Productor Kafka para eventos de auditoría.
 * Publica mensajes al tópico {@link KafkaTopics#PLATAFORMA_AUDITORIA_EVENTOS} con acks=all.
 * Utiliza callbacks asíncronos para registrar éxito (partición/offset) o errores.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaProducer {

    private static final String TOPIC = KafkaTopics.PLATAFORMA_AUDITORIA_EVENTOS;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publica un evento de auditoría al tópico correspondiente.
     * La clave de partición se basa en el nombre del servicio para agrupar
     * eventos del mismo microservicio en la misma partición.
     *
     * @param evento DTO con los datos del evento de auditoría
     */
    public void publicar(AuditoriaEventDTO evento) {
        String key = evento.getServicio();
        log.info("[AUDITORIA-PRODUCER] Publicando evento: servicio={}, accion={}, usuario={}",
                evento.getServicio(), evento.getAccion(), evento.getUsuario());

        kafkaTemplate.send(TOPIC, key, evento)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[AUDITORIA-PRODUCER] ✅ Evento publicado exitosamente -> topic={}, partition={}, offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("[AUDITORIA-PRODUCER] ❌ Error al publicar evento de auditoría: servicio={}, accion={} -> {}",
                                evento.getServicio(), evento.getAccion(), ex.getMessage(), ex);
                    }
                });
    }
}

