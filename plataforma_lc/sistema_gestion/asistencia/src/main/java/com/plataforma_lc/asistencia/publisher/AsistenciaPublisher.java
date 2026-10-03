package com.plataforma_lc.asistencia.publisher;

import com.plataforma_lc.asistencia.entities.Asistencia;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AsistenciaPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.asistencia}")
    private String exchange;

    @Value("${rabbitmq.routingkey.asistencia}")
    private String routingKey;

    public AsistenciaPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarEventoAsistencia(Asistencia asistencia) {
        rabbitTemplate.convertAndSend(exchange, routingKey, asistencia);
    }
}