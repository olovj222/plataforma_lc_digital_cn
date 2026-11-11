package com.plataforma_lc.adminKafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para el stream de analítica de asistencia.
 * Representa el estado de asistencia de un estudiante a un curso en una fecha determinada.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnaliticaStreamDTO {

    /** Identificador del curso */
    @NotNull(message = "El identificador del curso no puede ser nulo")
    private Long cursoId;

    /** Identificador del estudiante */
    @NotNull(message = "El identificador del estudiante no puede ser nulo")
    private Long estudianteId;

    /** Fecha de la asistencia */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    /** Estado de asistencia (PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO) */
    @NotBlank(message = "El estado de asistencia no puede estar en blanco")
    private String estado;

    /** Marca temporal del registro en formato ISO-8601 */
    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();
}
