package com.plataforma_lc.adminKafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "El nombre del servicio no puede estar en blanco")
    private String servicio;

    /** Acción realizada (CREATE, UPDATE, DELETE, LOGIN, etc.) */
    @NotBlank(message = "La acción realizada no puede estar en blanco")
    private String accion;

    /** Identificador del usuario que realizó la acción */
    @NotBlank(message = "El usuario no puede estar en blanco")
    private String usuario;

    /** Descripción detallada de la operación */
    private String detalle;

    /** Marca temporal del evento en formato ISO-8601 */
    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();
}
