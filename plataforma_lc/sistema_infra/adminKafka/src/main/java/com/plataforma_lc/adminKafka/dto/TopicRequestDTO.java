package com.plataforma_lc.adminKafka.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de solicitud para la creación dinámica de tópicos Kafka.
 * Permite parametrizar nombre, particiones, réplicas y políticas de retención.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicRequestDTO {

    /**
     * Nombre del tópico a crear. Cumple con la convención de nomenclatura de Apache Kafka:
     * caracteres alfanuméricos, guiones bajos, guiones y puntos (longitud máxima 249 caracteres).
     */
    @NotBlank(message = "El nombre del tópico es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{1,249}$", message = "El nombre del tópico debe contener solo letras, números, puntos, guiones o guiones bajos y tener entre 1 y 249 caracteres")
    private String nombre;

    /** Número de particiones (por defecto 3) */
    @Min(value = 1, message = "Debe haber al menos 1 partición")
    @Builder.Default
    private int particiones = 3;

    /** Factor de replicación (por defecto 3) */
    @Min(value = 1, message = "El factor de replicación debe ser al menos 1")
    @Builder.Default
    private int factorReplicacion = 3;

    /** Tiempo de retención en milisegundos (-1 para indefinido, por defecto 7 días) */
    @Min(value = -1, message = "El tiempo de retención debe ser >= -1")
    @Builder.Default
    private long retentionMs = 604800000L;

    /** Política de limpieza: "delete", "compact" o "compact,delete" */
    @Pattern(regexp = "^(delete|compact|compact,delete)$", message = "La política de limpieza debe ser 'delete', 'compact' o 'compact,delete'")
    @Builder.Default
    private String cleanupPolicy = "delete";
}
