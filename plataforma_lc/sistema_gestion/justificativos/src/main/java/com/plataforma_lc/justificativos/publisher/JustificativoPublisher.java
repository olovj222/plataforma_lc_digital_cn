package com.plataforma_lc.justificativos.publisher;

import com.plataforma_lc.justificativos.entities.Justificativo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
            rabbitTemplate.convertAndSend(exchange, routingKey, justificativo);
        } catch (Exception e) {
            // Log o manejo silencioso de la cola sin interrumpir la transacción principal
        }
    }
}