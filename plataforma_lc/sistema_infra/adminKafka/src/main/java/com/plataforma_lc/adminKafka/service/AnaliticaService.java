package com.plataforma_lc.adminKafka.service;

import com.plataforma_lc.adminKafka.dto.AnaliticaStreamDTO;
import com.plataforma_lc.adminKafka.exception.EventoInvalidoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio de lógica de dominio para procesamiento del flujo de analítica de asistencia.
 * Separa la regla de negocio del consumo de mensajería Kafka.
 */
@Slf4j
@Service
public class AnaliticaService {

    /**
     * Procesa un registro del stream de analítica validando sus datos y consolidando métricas.
     *
     * @param dto DTO del stream de analítica
     * @throws EventoInvalidoException si faltan campos obligatorios
     */
    public void procesar(AnaliticaStreamDTO dto) {
        if (dto == null) {
            throw new EventoInvalidoException("El registro de analítica no puede ser nulo");
        }
        if (dto.getCursoId() == null) {
            throw new EventoInvalidoException("El campo 'cursoId' es obligatorio en el registro de analítica");
        }
        if (dto.getEstudianteId() == null) {
            throw new EventoInvalidoException("El campo 'estudianteId' es obligatorio en el registro de analítica");
        }
        if (dto.getEstado() == null || dto.getEstado().isBlank()) {
            throw new EventoInvalidoException("El campo 'estado' es obligatorio en el registro de analítica");
        }

        log.info("[ANALITICA-SERVICIO] Procesando analítica de asistencia: cursoId={}, estudianteId={}, estado={}, fecha={}",
                dto.getCursoId(), dto.getEstudianteId(), dto.getEstado(), dto.getFecha());

        // Punto de extensión: agregación en ventanas de tiempo, cálculo de porcentajes de asistencia, etc.
    }
}

