package com.plataforma_lc.justificativos.dto;

import java.time.Instant;

public record EventoAuditoriaDto(
    String eventId,
    String servicioOrigen,
    String tipoAccion,
    String usuarioId,
    String timestamp,
    DetallesAuditDto detalles
) {}