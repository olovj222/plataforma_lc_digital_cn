package com.plataforma_lc.justificativos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DetallesAuditDto(
        Long justificativoId,
        Long estudianteId,
        String estadoAnterior,
        String estadoNuevo,
        String motivo,
        String observacion
) {
    // Mantiene compatibilidad con llamadas anteriores que no envían motivo ni observación
    public DetallesAuditDto(Long justificativoId, Long estudianteId, String estadoNuevo) {
        this(justificativoId, estudianteId, null, estadoNuevo, null, null);
    }
}