package com.plataforma_lc.adminKafka.service;

import com.plataforma_lc.adminKafka.dto.AlertaEventDTO;
import com.plataforma_lc.adminKafka.exception.EventoInvalidoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio de lógica de dominio para procesamiento de alertas críticas.
 * Separa la regla de negocio del consumo de mensajería Kafka.
 */
@Slf4j
@Service
public class AlertaService {

    /**
     * Procesa un evento de alerta validando su nivel y contenido.
     *
     * @param evento DTO de alerta a procesar
     * @throws EventoInvalidoException si faltan campos obligatorios
     */
    public void procesar(AlertaEventDTO evento) {
        if (evento == null) {
            throw new EventoInvalidoException("El evento de alerta no puede ser nulo");
        }
        if (evento.getTipoAlerta() == null || evento.getTipoAlerta().isBlank()) {
            throw new EventoInvalidoException("El campo 'tipoAlerta' es obligatorio en el evento de alerta");
        }
        if (evento.getNivel() == null || evento.getNivel().isBlank()) {
            throw new EventoInvalidoException("El campo 'nivel' es obligatorio en el evento de alerta");
        }
        if (evento.getMensaje() == null || evento.getMensaje().isBlank()) {
            throw new EventoInvalidoException("El campo 'mensaje' es obligatorio en el evento de alerta");
        }

        switch (evento.getNivel().toUpperCase()) {
            case "CRITICAL" -> log.error("[ALERTA-SERVICIO] 🚨 ALERTA CRÍTICA recibida: alertaId={}, tipo={}, mensaje={}, estudianteId={}, cursoId={}",
                    evento.getAlertaId(), evento.getTipoAlerta(), evento.getMensaje(), evento.getEstudianteId(), evento.getCursoId());
            case "WARNING" -> log.warn("[ALERTA-SERVICIO] ⚠️ ALERTA WARNING recibida: alertaId={}, tipo={}, mensaje={}",
                    evento.getAlertaId(), evento.getTipoAlerta(), evento.getMensaje());
            default -> log.info("[ALERTA-SERVICIO] ℹ️ ALERTA INFO recibida: alertaId={}, tipo={}, mensaje={}",
                    evento.getAlertaId(), evento.getTipoAlerta(), evento.getMensaje());
        }

        // Punto de extensión: envío de emails, notificaciones push, webhooks, etc.
    }
}

