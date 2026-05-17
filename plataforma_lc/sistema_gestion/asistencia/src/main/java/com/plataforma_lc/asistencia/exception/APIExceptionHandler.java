/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.exception;

import com.plataforma_lc.asistencia.common.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;


public class APIExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnknowHostException(Exception ex)
    {
        ExceptionResponse respuesta = new ExceptionResponse("Error desconocido",1);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(respuesta);
    }
    
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<?> handleBusinessRuleException(BusinessRuleException ex)
    {
        ExceptionResponse respuesta = new ExceptionResponse("Error: Regla de negocio o de validacion",2);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }
}
