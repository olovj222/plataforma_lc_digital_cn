/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.service;


import com.plataforma_lc.asistencia.entities.EstudianteResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class EstudianteClientService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @CircuitBreaker(name = "estudianteService", fallbackMethod = "fallbackEstudiante") 
    public EstudianteResponse obtenerEstudiante(Long estudianteId) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8080/estudiante/{id}", estudianteId)
                .retrieve()
                .onStatus(
                    status -> status.value() == 404,
                    response -> Mono.error(new NoSuchElementException("Estudiante no encontrado"))
                )
                .bodyToMono(EstudianteResponse.class)
                .block();
    }

    public EstudianteResponse fallbackEstudiante(Long estudianteId, Throwable ex) {
        if (ex instanceof NoSuchElementException) {
            return EstudianteResponse.builder()
                .nombre("Estudiante no encontrado")
                .build();
        }
        return EstudianteResponse.builder()
            .nombre("MS Estudiante no disponible")
            .build();
    }
}
