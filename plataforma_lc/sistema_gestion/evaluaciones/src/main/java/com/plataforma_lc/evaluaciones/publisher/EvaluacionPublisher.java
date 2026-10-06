package com.plataforma_lc.evaluaciones.publisher;

import com.plataforma_lc.evaluaciones.entities.Evaluaciones;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EvaluacionPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.evaluaciones}")
    private String exchange;

    @Value("${rabbitmq.routingkey.evaluaciones}")
    private String routingKey;

    public EvaluacionPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarEventoEvaluacion(Evaluaciones evaluacion) {
        try {
            // Construimos un payload simple para garantizar serialización limpia
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", evaluacion.getId());
            payload.put("estudianteId", evaluacion.getEstudianteId());
            payload.put("cursoId", evaluacion.getCursoId());
            payload.put("calificacion", evaluacion.getCalificacion());
            payload.put("nombre", evaluacion.getNombre());

            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
            System.out.println(">>> Evento evaluación enviado a RabbitMQ: " + payload);
        } catch (Exception e) {
            System.err.println(">>> ERROR publicando evento de evaluación en RabbitMQ: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
