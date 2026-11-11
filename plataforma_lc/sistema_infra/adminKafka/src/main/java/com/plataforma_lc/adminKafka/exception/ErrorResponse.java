package com.plataforma_lc.adminKafka.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Representación estándar para respuestas de error de la API REST.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> detallesValidacion
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> detallesValidacion) {
        this(LocalDateTime.now(), status, error, message, path, detallesValidacion);
    }
}
