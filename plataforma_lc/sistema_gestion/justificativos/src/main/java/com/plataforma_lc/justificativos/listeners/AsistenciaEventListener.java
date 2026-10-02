package com.plataforma_lc.justificativos.listeners;

import com.plataforma_lc.justificativos.dto.AsistenciaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Component;

@Component
public class AsistenciaEventListener {

    private static final Logger log = LoggerFactory.getLogger(AsistenciaEventListener.class);

    @RabbitListener(queues = "${rabbitmq.queue.asistencia}")
    public void procesarEventoAsistencia(AsistenciaDTO dto) {
        try {
            log.info("Evento de asistencia recibido en justificativos: {}", dto);
            // Lógica de procesamiento...
        } catch (Exception e) {
            log.error("Error al procesar mensaje. Redirigiendo a la DLQ: {}", e.getMessage());
            // Al lanzar esta excepción, Spring AMQP envía el mensaje a la DLQ configurada
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
