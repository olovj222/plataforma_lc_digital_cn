/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.curso.common;

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