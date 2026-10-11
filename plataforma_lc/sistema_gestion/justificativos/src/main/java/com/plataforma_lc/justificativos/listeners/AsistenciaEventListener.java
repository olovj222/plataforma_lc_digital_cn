package com.plataforma_lc.justificativos.listeners;

import com.plataforma_lc.justificativos.dto.AsistenciaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AsistenciaEventListener {

    private static final Logger log = LoggerFactory.getLogger(AsistenciaEventListener.class);
    private static final String MARCADOR_FALLO = "SIMULAR_FALLO_JUSTIFICATIVOS";

    @Value("${rabbitmq.simulacion.fallos.habilitada:false}")
    private boolean simulacionFallosHabilitada;

    @RabbitListener(queues = "${rabbitmq.queue.justificativos}")
    public void procesarEventoAsistencia(AsistenciaDTO dto) {
        try {
            log.info("Evento de asistencia recibido en justificativos: {}", dto);

            if (simulacionFallosHabilitada
                    && dto.getMotivo() != null
                    && dto.getMotivo().contains(MARCADOR_FALLO)) {
                throw new IllegalStateException("Fallo simulado del consumer");
            }

            // Lógica de procesamiento...

        } catch (Exception e) {
            log.error("Error al procesar mensaje. Redirigiendo a la DLQ: {}", e.getMessage());
            // Al lanzar esta excepción, Spring AMQP envía el mensaje a la DLQ configurada
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}