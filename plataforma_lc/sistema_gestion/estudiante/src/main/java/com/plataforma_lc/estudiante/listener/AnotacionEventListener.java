package com.plataforma_lc.estudiante.listener;

import com.plataforma_lc.estudiante.dto.AnotacionEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer del evento de anotación NEGATIVA publicado por el MS anotaciones.
 * Escucha anotaciones.queue y registra la incidencia en el dominio estudiante.
 */
@Component
public class AnotacionEventListener {

    private static final Logger log = LoggerFactory.getLogger(AnotacionEventListener.class);

    @RabbitListener(queues = "${rabbitmq.queue.anotaciones}")
    public void procesarAnotacionNegativa(AnotacionEventDTO dto) {
        try {
            log.warn("Anotación NEGATIVA recibida — estudianteId: {}, cursoId: {}, descripcion: '{}', autorId: {}",
                    dto.getEstudianteId(), dto.getCursoId(), dto.getDescripcion(), dto.getAutorId());

            // Lógica de negocio: registrar incidencia, notificar al estudiante,
            // actualizar historial disciplinario, etc.

        } catch (Exception e) {
            log.error("Error al procesar anotación NEGATIVA: {}. Redirigiendo a DLQ.", e.getMessage());
            // Spring AMQP envía el mensaje a anotaciones.dlq
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
