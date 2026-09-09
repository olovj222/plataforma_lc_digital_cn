package com.plataforma_lc.asistencia.service;

import com.plataforma_lc.asistencia.entities.EstudianteResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EstudianteClientService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${MS_ESTUDIANTE_URL:http://estudiante:8080}")
    private String estudianteServiceUrl;

    @CircuitBreaker(name = "estudianteService", fallbackMethod = "fallbackEstudiante")
    public EstudianteResponse obtenerEstudiante(Long estudianteId) {
        return webClientBuilder.build()
                .get()
                .uri(estudianteServiceUrl + "/estudiante/{id}", estudianteId)
                .retrieve()
                .bodyToMono(EstudianteResponse.class)
                .block();
    }

    public EstudianteResponse fallbackEstudiante(Long estudianteId, Throwable ex) {
        return null;
    }
}