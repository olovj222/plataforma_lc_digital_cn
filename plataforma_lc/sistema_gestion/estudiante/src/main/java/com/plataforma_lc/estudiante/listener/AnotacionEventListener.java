package com.plataforma_lc.estudiante.listener;

import com.plataforma_lc.estudiante.dto.AnotacionEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consumer del evento de anotación NEGATIVA publicado por el MS anotaciones.
 * Escucha anotaciones.queue y registra la incidencia en el dominio estudiante.
 */
@Component
public class AnotacionEventListener {

    private static final Logger log = LoggerFactory.getLogger(AnotacionEventListener.class);

    /** Texto que, presente en la descripción, provoca el fallo simulado. */
    static final String MARCADOR_FALLO = "SIMULAR_FALLO";

    @Value("${rabbitmq.simulacion.fallos.habilitada:false}")
    private boolean simulacionFallosHabilitada;

    @RabbitListener(queues = "${rabbitmq.queue.anotaciones}")
    public void procesarAnotacionNegativa(AnotacionEventDTO dto) {
        try {
            log.warn("Anotación NEGATIVA recibida — estudianteId: {}, cursoId: {}, descripcion: '{}', autorId: {}",
                    dto.getEstudianteId(), dto.getCursoId(), dto.getDescripcion(), dto.getAutorId());

            if (simulacionFallosHabilitada
                    && dto.getDescripcion() != null
                    && dto.getDescripcion().contains(MARCADOR_FALLO)) {
                throw new IllegalStateException(
                        "Fallo simulado del consumer (marcador " + MARCADOR_FALLO + ")");
            }

            // Lógica de negocio: registrar incidencia, notificar al estudiante,
            // actualizar historial disciplinario, etc.

        } catch (Exception e) {
            log.error("Error al procesar anotación NEGATIVA: {}. Redirigiendo a DLQ.", e.getMessage());
            // Spring AMQP envía el mensaje a anotaciones.dlq
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}