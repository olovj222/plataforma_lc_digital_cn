package com.plataforma_lc.adminKafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud para la creación dinámica de tópicos Kafka.
 * Permite parametrizar nombre, particiones, réplicas y políticas de retención.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicRequestDTO {

    /** Nombre del tópico a crear */
    @NotBlank(message = "El nombre del tópico es obligatorio")
    private String nombre;

    /** Número de particiones (por defecto 3) */
    @Min(value = 1, message = "Debe haber al menos 1 partición")
    @Builder.Default
    private int particiones = 3;

    /** Factor de replicación (por defecto 3) */
    @Min(value = 1, message = "El factor de replicación debe ser al menos 1")
    @Builder.Default
    private int factorReplicacion = 3;

    /** Tiempo de retención en milisegundos (por defecto 7 días) */
    @Builder.Default
    private long retentionMs = 604800000L;

    /** Política de limpieza: "delete" o "compact" (por defecto "delete") */
    @Builder.Default
    private String cleanupPolicy = "delete";
}

