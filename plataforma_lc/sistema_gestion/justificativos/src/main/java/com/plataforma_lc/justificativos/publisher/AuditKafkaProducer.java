package com.plataforma_lc.justificativos.publisher;

import com.plataforma_lc.justificativos.dto.DetallesAuditDto;
import com.plataforma_lc.justificativos.dto.EventoAuditoriaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AuditKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(AuditKafkaProducer.class);

    private final KafkaTemplate<String, EventoAuditoriaDto> kafkaTemplate;
    private final String auditTopic;
    private final String servicioOrigen;

    public AuditKafkaProducer(
            KafkaTemplate<String, EventoAuditoriaDto> kafkaTemplate,
            @Value("${app.kafka.topic-audit}") String auditTopic,
            @Value("${app.kafka.servicio-origen}") String servicioOrigen) {
        this.kafkaTemplate = kafkaTemplate;
        this.auditTopic = auditTopic;
        this.servicioOrigen = servicioOrigen;
    }

    public void emitirEventoAuditoria(String tipoAccion, String usuarioId, DetallesAuditDto detalles) {
        String eventId = "evt-" + UUID.randomUUID();
        String timestamp = Instant.now().toString(); // Formato ISO-8601 en UTC (ej: 2026-11-02T10:15:00Z)

        EventoAuditoriaDto evento = new EventoAuditoriaDto(
            eventId,
            servicioOrigen,
            tipoAccion,
            usuarioId,
            timestamp,
            detalles
        );

        // Se usa estudianteId o justificativoId como Key para asegurar el orden por entidad en la partición
        String partitionKey = detalles.justificativoId() != null 
                ? String.valueOf(detalles.justificativoId()) 
                : eventId;

        kafkaTemplate.send(auditTopic, partitionKey, evento)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Evento de auditoria emitido a Kafka correctamente. ID: {}, Topic: {}, Particion: {}",
                            eventId, auditTopic, result.getRecordMetadata().partition());
                } else {
                    log.error("Fallo al emitir evento de auditoria a Kafka. ID: {}", eventId, ex);
                }
            });
    }
}