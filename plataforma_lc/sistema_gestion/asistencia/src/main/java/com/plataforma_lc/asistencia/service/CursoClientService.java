/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.service;

import com.plataforma_lc.asistencia.entities.CursoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CursoClientService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value ("${ms.curso.url:http://localhost:8081}")
    private String cursoUrl;

    @CircuitBreaker(name = "cursoService", fallbackMethod = "fallbackCurso")
    public CursoResponse obtenerCurso(Long cursoId) {
        return webClientBuilder.build()
                .get()
                .uri(cursoUrl + "/curso/{id}", cursoId)
                .retrieve()
                .bodyToMono(CursoResponse.class)
                .block();
    }

    public CursoResponse fallbackCurso(Long cursoId, Throwable ex) {
        return null;
    }
}