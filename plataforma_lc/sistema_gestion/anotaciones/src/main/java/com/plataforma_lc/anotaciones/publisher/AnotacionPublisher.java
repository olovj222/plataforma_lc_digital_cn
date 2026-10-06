package com.plataforma_lc.anotaciones.publisher;

import com.plataforma_lc.anotaciones.entities.Anotacion;
import com.plataforma_lc.anotaciones.entities.TipoAnotacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Publisher del dominio anotaciones.
 * Solo publica eventos cuando el tipo de anotación es NEGATIVA.
 * Usa la entidad Anotacion directamente (no requiere DTO propio).
 */
@Component
public class AnotacionPublisher {

    private static final Logger log = LoggerFactory.getLogger(AnotacionPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.anotaciones}")
    private String exchange;

    @Value("${rabbitmq.routingkey.anotaciones}")
    private String routingKey;

    public AnotacionPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica el evento solo si la anotación es de tipo NEGATIVA.
     * El MS estudiante (consumer) recibirá el evento para registrar la incidencia.
     */
    public void publicarAnotacionNegativa(Anotacion anotacion) {
        if (anotacion.getTipo() != TipoAnotacion.NEGATIVA) {
            return; // Solo publicamos anotaciones negativas
        }
        try {
            // Construimos payload simple para serialización limpia
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", anotacion.getId());
            payload.put("estudianteId", anotacion.getEstudianteId());
            payload.put("cursoId", anotacion.getCursoId());
            payload.put("tipo", anotacion.getTipo().name());         // "NEGATIVA"
            payload.put("descripcion", anotacion.getDescripcion());
            payload.put("fecha", anotacion.getFecha().toString());   // ISO "YYYY-MM-DD"
            payload.put("autorId", anotacion.getAutorId());

            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
            log.info(">>> Evento anotación NEGATIVA enviado a RabbitMQ: estudianteId={}", anotacion.getEstudianteId());
        } catch (Exception e) {
            log.error(">>> ERROR publicando evento de anotación en RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
