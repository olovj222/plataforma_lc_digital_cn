package com.plataforma_lc.justificativos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DetallesAuditDto(
    Long justificativoId,
    Long estudianteId,
    String estadoAnterior,
    String estadoNuevo
) {}