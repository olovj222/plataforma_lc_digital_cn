/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.service;


import com.plataforma_lc.asistencia.entities.EstudianteResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EstudianteClientService {
    
    @Value("${estudiante.service.url:localhost:8080}")
    private String estudianteHost;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @CircuitBreaker(name = "estudianteService", fallbackMethod = "estudianteFallback")
    public EstudianteResponse obtenerEstudiante(Long estudianteId) {
        return webClientBuilder.build()
                .get()
                .uri("http://" + estudianteHost + "/estudiante/{id}", estudianteId)
                .retrieve()
                .bodyToMono(EstudianteResponse.class)
                .block();
    }

    public EstudianteResponse estudianteFallback(Long estudianteId, Exception ex) {
        EstudianteResponse fallback = new EstudianteResponse();
        fallback.setNombre("MS Estudiante no disponible");
        return fallback;
    }
}
