package com.plataforma_lc.adminKafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Registra acciones realizadas por usuarios en los distintos microservicios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaEventDTO {

    /** Nombre del microservicio que genera el evento */
    private String servicio;

    /** Acción realizada (CREATE, UPDATE, DELETE, LOGIN, etc.) */
    private String accion;

    /** Identificador del usuario que realizó la acción */
    private String usuario;

    /** Descripción detallada de la operación */
    private String detalle;

    /** Marca temporal del evento */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

