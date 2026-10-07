package com.plataforma_lc.asistencia.listener;

import com.plataforma_lc.asistencia.dto.EvaluacionEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consumer del evento de evaluación publicado por el MS evaluaciones.
 * Escucha evaluaciones.queue y procesa las calificaciones registradas.
 */
@Component
public class EvaluacionEventListener {

    private static final Logger log = LoggerFactory.getLogger(EvaluacionEventListener.class);

    /** Texto que, presente en el nombre de la evaluación, provoca el fallo simulado. */
    static final String MARCADOR_FALLO = "SIMULAR_FALLO";

    @Value("${rabbitmq.simulacion.fallos.habilitada:false}")
    private boolean simulacionFallosHabilitada;

    @RabbitListener(queues = "${rabbitmq.queue.evaluaciones}")
    public void procesarEvaluacion(EvaluacionEventDTO dto) {
        try {
            log.info("Calificación registrada — estudianteId: {}, cursoId: {}, evaluacion: '{}', calificacion: {}",
                    dto.getEstudianteId(), dto.getCursoId(), dto.getNombre(), dto.getCalificacion());

            if (simulacionFallosHabilitada
                    && dto.getNombre() != null
                    && dto.getNombre().contains(MARCADOR_FALLO)) {
                throw new IllegalStateException(
                        "Fallo simulado del consumer (marcador " + MARCADOR_FALLO + ")");
            }

            // Lógica de negocio: registrar en historial/auditoría del MS asistencia
            // Por ejemplo: marcar que el estudiante tiene evaluación en esa fecha,
            // o simplemente registrar el evento para trazabilidad.

        } catch (Exception e) {
            log.error("Error al procesar evento de evaluación: {}. Redirigiendo a DLQ.", e.getMessage());
            // Al lanzar esta excepción, Spring AMQP envía el mensaje a evaluaciones.dlq
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}