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

    // Lee la URL de la variable MS_CURSO_URL o usa por defecto el nombre del contenedor en Docker
    @Value("${MS_CURSO_URL:http://curso:8081}")
    private String cursoServiceUrl;

    @CircuitBreaker(name = "cursoService", fallbackMethod = "fallbackCurso")
    public CursoResponse obtenerCurso(Long cursoId) {
        return webClientBuilder.build()
                .get()
                .uri(cursoServiceUrl + "/curso/{id}", cursoId)
                .retrieve()
                .bodyToMono(CursoResponse.class)
                .block();
    }

    public CursoResponse fallbackCurso(Long cursoId, Throwable ex) {
        return null;
    }
}