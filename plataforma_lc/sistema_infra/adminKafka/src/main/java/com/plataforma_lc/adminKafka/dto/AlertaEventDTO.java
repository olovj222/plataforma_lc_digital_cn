package com.plataforma_lc.adminKafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Permite notificar situaciones que requieren atención inmediata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaEventDTO {

    /** Identificador único de la alerta */
    private String alertaId;

    /** Tipo de alerta (INASISTENCIA_REITERADA, BAJO_RENDIMIENTO, ERROR_SISTEMA, etc.) */
    private String tipoAlerta;

    /** Nivel de severidad: INFO, WARNING, CRITICAL */
    private String nivel;

    /** Identificador del estudiante afectado (puede ser null si es alerta de sistema) */
    private Long estudianteId;

    /** Identificador del curso relacionado (puede ser null) */
    private Long cursoId;

    /** Mensaje descriptivo de la alerta */
    private String mensaje;

    /** Microservicio que originó la alerta */
    private String origenServicio;

    /** Marca temporal del evento */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

