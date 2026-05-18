/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.estudiante.service;

/**
 *
 * @author Olov
 */
import com.plataforma_lc.estudiante.entities.CursoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CursoClientService {

    @Value("${curso.service.url:localhost:8081}")
    private String cursoHost;
    @Autowired
    private WebClient.Builder webClientBuilder;

    @CircuitBreaker(name = "cursoService", fallbackMethod = "cursoFallback")
    public CursoResponse obtenerCurso(Long cursoId) {
        return webClientBuilder.build()
                .get()
                .uri(cursoHost + "/curso/{id}", cursoId)
                .retrieve()
                .bodyToMono(CursoResponse.class)
                .block();
    }

    public CursoResponse cursoFallback(Long cursoId, Exception ex) {
        CursoResponse fallback = new CursoResponse();
        fallback.setNombre("MS Cursos no disponible");
        return fallback;
    }
}