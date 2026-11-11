package com.plataforma_lc.adminKafka.service;

import com.plataforma_lc.adminKafka.dto.AuditoriaEventDTO;
import com.plataforma_lc.adminKafka.exception.EventoInvalidoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio de lógica de dominio para procesamiento de eventos de auditoría.
 * Separa la regla de negocio del consumo de mensajería Kafka.
 */
@Slf4j
@Service
public class AuditoriaService {

    /**
     * Procesa un evento de auditoría validando sus datos y aplicando la lógica del negocio.
     *
     * @param evento DTO de auditoría a procesar
     * @throws EventoInvalidoException si faltan campos obligatorios
     */
    public void procesar(AuditoriaEventDTO evento) {
        if (evento == null) {
            throw new EventoInvalidoException("El evento de auditoría no puede ser nulo");
        }
        if (evento.getServicio() == null || evento.getServicio().isBlank()) {
            throw new EventoInvalidoException("El campo 'servicio' es obligatorio en el evento de auditoría");
        }
        if (evento.getAccion() == null || evento.getAccion().isBlank()) {
            throw new EventoInvalidoException("El campo 'accion' es obligatorio en el evento de auditoría");
        }
        if (evento.getUsuario() == null || evento.getUsuario().isBlank()) {
            throw new EventoInvalidoException("El campo 'usuario' es obligatorio en el evento de auditoría");
        }

        log.info("[AUDITORIA-SERVICIO] Procesando auditoría: servicio={}, accion={}, usuario={}, detalle={}",
                evento.getServicio(), evento.getAccion(), evento.getUsuario(), evento.getDetalle());

        // Punto de extensión: indexar en base de datos de auditoría, ElasticSearch, etc.
    }
}

