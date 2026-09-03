package com.plataforma_lc.justificativos.exception;

public class BusinessRuleException extends RuntimeException {
    private final int status;

    public BusinessRuleException(String mensaje, int status) {
        super(mensaje);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}