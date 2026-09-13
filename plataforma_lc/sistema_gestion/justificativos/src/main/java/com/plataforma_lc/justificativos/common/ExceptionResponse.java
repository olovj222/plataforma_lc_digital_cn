package com.plataforma_lc.justificativos.common;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExceptionResponse {
    private String mensaje;
    private int status;
    private LocalDateTime timestamp;

    public ExceptionResponse(String mensaje, int status) {
        this.mensaje = mensaje;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}