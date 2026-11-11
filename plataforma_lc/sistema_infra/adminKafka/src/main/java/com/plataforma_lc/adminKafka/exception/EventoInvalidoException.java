package com.plataforma_lc.adminKafka.exception;

/**
 * Excepción lanzada cuando un payload de evento recibido no cumple las reglas de negocio o validación.
 * Esta excepción se clasifica como no-reintentable para ser enviada inmediatamente al DLT.
 */
public class EventoInvalidoException extends RuntimeException {

    public EventoInvalidoException(String message) {
        super(message);
    }

    public EventoInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
