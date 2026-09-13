/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.common;

import java.time.LocalDateTime;
import lombok.Data;


@Data
public class ExceptionResponse {
    private String mensaje;
    private int status;
    private LocalDateTime timeStamp;
    
    public ExceptionResponse(String mensaje, int status) {
        this.mensaje = mensaje;
        this.status = status;
        this.timeStamp = LocalDateTime.now();
    }
}