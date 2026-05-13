/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.evaluaciones.service;


import com.plataforma_lc.evaluaciones.entities.CursoResponse;
import com.plataforma_lc.evaluaciones.entities.evaluaciones;
import com.plataforma_lc.evaluaciones.repository.evaluacionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

import java.util.List;
/**
 *
 * @author juako
 */
@Service
public class evaluacionesService {

    @Autowired
    private evaluacionesRepository repo;

    @Autowired
    private WebClient.Builder webClientBuilder;

    // Método helper para obtener nombre del curso desde MS Curso
    private void rellenarNombreCurso(evaluaciones e) {
        try {
            CursoResponse curso = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8081/curso/{id}", e.getCursoId())
                    .retrieve()
                    .bodyToMono(CursoResponse.class)
                    .block();

            if (curso != null) {
                e.setCursoNombre(curso.getNombre());
            }
        } catch (Exception ex) {
            e.setCursoNombre("Información no disponible (MS apagado)");
        }
    }

    public evaluaciones guardar(evaluaciones e) {
        evaluaciones guardada = repo.save(e);
        rellenarNombreCurso(guardada);
        return guardada;
    }

    public evaluaciones actualizar(Long id, evaluaciones nueva) {
        evaluaciones e = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Evaluación no encontrada con id: " + id));

        e.setNombre(nueva.getNombre());
        e.setCursoId(nueva.getCursoId());
        e.setEstudianteId(nueva.getEstudianteId());

        evaluaciones guardada = repo.save(e);
        rellenarNombreCurso(guardada);
        return guardada;
    }

    public List<evaluaciones> porCurso(Long cursoId) {
        List<evaluaciones> lista = repo.findByCursoId(cursoId);
        lista.forEach(this::rellenarNombreCurso);
        return lista;
    }

    public evaluaciones registrarNota(Long id, int nota) {
        if (nota < 1 || nota > 7) {
            throw new RuntimeException("Nota fuera de rango: debe estar entre 1 y 7");
        }

        evaluaciones e = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Evaluación no encontrada con id: " + id));

        e.setCalificacion(nota);
        evaluaciones guardada = repo.save(e);
        rellenarNombreCurso(guardada);
        return guardada;
    }

    public List<evaluaciones> porEstudiante(Long id) {
        List<evaluaciones> lista = repo.findByEstudianteId(id);
        lista.forEach(this::rellenarNombreCurso);
        return lista;
    }
}