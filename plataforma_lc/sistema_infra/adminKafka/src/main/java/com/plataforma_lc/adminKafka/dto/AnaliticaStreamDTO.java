package com.plataforma_lc.adminKafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa el estado de asistencia de un estudiante a un curso en una fecha determinada.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnaliticaStreamDTO {

    /** Identificador del curso */
    private Long cursoId;

    /** Identificador del estudiante */
    private Long estudianteId;

    /** Fecha de la asistencia */
    private LocalDate fecha;

    /** Estado de asistencia (PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO) */
    private String estado;

    /** Marca temporal del registro */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

