package com.plataforma_lc.justificativos.publisher;

import com.plataforma_lc.justificativos.entities.Justificativo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JustificativoPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.asistencia}")
    private String exchange;

    @Value("${rabbitmq.routingkey.asistencia}")
    private String routingKey;

    public JustificativoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarEventoJustificativo(Justificativo justificativo) {
        try {
            // Construimos un payload simple para garantizar serialización limpia
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", justificativo.getId());
            payload.put("estudianteId", justificativo.getEstudianteId());
            payload.put("cursoId", justificativo.getCursoId());
            payload.put("fecha", justificativo.getFechaInasistencia().toString()); // ISO "YYYY-MM-DD"
            payload.put("estado", justificativo.getEstado().name()); // "PENDIENTE", "APROBADO", "RECHAZADO"

            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
            System.out.println(">>> Evento enviado exitosamente a RabbitMQ: " + payload);
        } catch (Exception e) {
            System.err.println(">>> ERROR publicando evento en RabbitMQ: " + e.getMessage());
            e.printStackTrace();
        }
    }
}