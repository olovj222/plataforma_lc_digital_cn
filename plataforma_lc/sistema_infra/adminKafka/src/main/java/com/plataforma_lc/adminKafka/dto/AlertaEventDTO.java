package com.plataforma_lc.adminKafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para eventos de alertas de la plataforma.
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
    @NotBlank(message = "El tipo de alerta no puede estar en blanco")
    private String tipoAlerta;

    /** Nivel de severidad: INFO, WARNING, CRITICAL */
    @NotBlank(message = "El nivel de la alerta no puede estar en blanco")
    private String nivel;

    /** Identificador del estudiante afectado (puede ser null si es alerta de sistema) */
    private Long estudianteId;

    /** Identificador del curso relacionado (puede ser null) */
    private Long cursoId;

    /** Mensaje descriptivo de la alerta */
    @NotBlank(message = "El mensaje de la alerta no puede estar en blanco")
    private String mensaje;

    /** Microservicio que originó la alerta */
    private String origenServicio;

    /** Marca temporal del evento en formato ISO-8601 */
    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();
}
