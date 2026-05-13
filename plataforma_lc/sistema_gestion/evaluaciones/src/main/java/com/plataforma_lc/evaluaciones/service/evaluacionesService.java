/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.evaluaciones.service;


import com.plataforma_lc.evaluaciones.entities.CursoResponse;
import com.plataforma_lc.evaluaciones.entities.Evaluaciones;
import com.plataforma_lc.evaluaciones.repository.evaluacionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import org.springframework.web.client.RestTemplate;
/**
 *
 * @author juako
 */
@Service
public class evaluacionesService {

    @Autowired
    private evaluacionesRepository repo;

@Autowired
private RestTemplate restTemplate;

private void rellenarNombreCurso(Evaluaciones e) {
    try {
        CursoResponse curso = restTemplate.getForObject(
            "http://localhost:8081/curso/" + e.getCursoId(),
            CursoResponse.class
        );
        if (curso != null) {
            e.setCursoNombre(curso.getNombre());
        }
    } catch (Exception ex) {
        e.setCursoNombre("Información no disponible (MS apagado)");
    }
}

    public Evaluaciones guardar(Evaluaciones e) {
        Evaluaciones guardada = repo.save(e);
        rellenarNombreCurso(guardada);
        return guardada;
    }

    public Evaluaciones actualizar(Long id, Evaluaciones nueva) {
        Evaluaciones e = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Evaluación no encontrada con id: " + id));

        e.setNombre(nueva.getNombre());
        e.setCursoId(nueva.getCursoId());
        e.setEstudianteId(nueva.getEstudianteId());

        Evaluaciones guardada = repo.save(e);
        rellenarNombreCurso(guardada);
        return guardada;
    }

    public List<Evaluaciones> porCurso(Long cursoId) {
        List<Evaluaciones> lista = repo.findByCursoId(cursoId);
        lista.forEach(this::rellenarNombreCurso);
        return lista;
    }

    public Evaluaciones registrarNota(Long id, int nota) {
        if (nota < 1 || nota > 7) {
            throw new RuntimeException("Nota fuera de rango: debe estar entre 1 y 7");
        }

        Evaluaciones e = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Evaluación no encontrada con id: " + id));

        e.setCalificacion(nota);
        Evaluaciones guardada = repo.save(e);
        rellenarNombreCurso(guardada);
        return guardada;
    }

    public List<Evaluaciones> porEstudiante(Long id) {
        List<Evaluaciones> lista = repo.findByEstudianteId(id);
        lista.forEach(this::rellenarNombreCurso);
        return lista;
    }
}