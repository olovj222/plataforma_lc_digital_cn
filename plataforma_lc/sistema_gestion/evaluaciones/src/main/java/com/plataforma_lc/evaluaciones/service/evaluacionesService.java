/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.evaluaciones.service;


import com.plataforma_lc.evaluaciones.entities.CursoResponse;
import com.plataforma_lc.evaluaciones.entities.EstudianteResponse;
import com.plataforma_lc.evaluaciones.entities.Evaluaciones;
import com.plataforma_lc.evaluaciones.exception.BusinessRuleException;
import com.plataforma_lc.evaluaciones.repository.evaluacionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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

    private void rellenarNombreEstudiante(Evaluaciones e) {
        try {
            EstudianteResponse estudiante = restTemplate.getForObject(
                "http://localhost:8080/estudiante/" + e.getEstudianteId(),
                EstudianteResponse.class
            );
            if (estudiante != null) {
                e.setEstudianteNombre(estudiante.getNombre() + " " + estudiante.getApPaterno());
            } else {
                e.setEstudianteNombre("Información no disponible (MS apagado)");
            }
        } catch (Exception ex) {
            e.setEstudianteNombre("Información no disponible (MS apagado)");
        }
    }

    public Evaluaciones guardar(Evaluaciones e) {
        Evaluaciones guardada = repo.save(e);
        rellenarNombreCurso(guardada);
        rellenarNombreEstudiante(guardada);
        return guardada;
    }

    public Evaluaciones actualizar(Long id, Evaluaciones nueva) {
        Evaluaciones e = repo.findById(id)
            .orElseThrow(() -> new BusinessRuleException(
                "Evaluación no encontrada con id: " + id,
                HttpStatus.NOT_FOUND.value()
            ));
        e.setNombre(nueva.getNombre());
        e.setCursoId(nueva.getCursoId());
        e.setEstudianteId(nueva.getEstudianteId());
        Evaluaciones guardada = repo.save(e);
        rellenarNombreCurso(guardada);
        rellenarNombreEstudiante(guardada);
        return guardada;
    }

    public List<Evaluaciones> porCurso(Long cursoId) {
        List<Evaluaciones> lista = repo.findByCursoId(cursoId);
        lista.forEach(this::rellenarNombreCurso);
        lista.forEach(this::rellenarNombreEstudiante);
        return lista;
    }

    public Evaluaciones registrarNota(Long id, int nota) {
        if (nota < 1 || nota > 7) {
            throw new BusinessRuleException(
                "Nota fuera de rango: debe estar entre 1 y 7",
                HttpStatus.BAD_REQUEST.value()
            );
        }
        Evaluaciones e = repo.findById(id)
            .orElseThrow(() -> new BusinessRuleException(
                "Evaluación no encontrada con id: " + id,
                HttpStatus.NOT_FOUND.value()
            ));
        e.setCalificacion(nota);
        Evaluaciones guardada = repo.save(e);
        rellenarNombreCurso(guardada);
        rellenarNombreEstudiante(guardada);
        return guardada;
    }

    public List<Evaluaciones> porEstudiante(Long id) {
        List<Evaluaciones> lista = repo.findByEstudianteId(id);
        lista.forEach(this::rellenarNombreCurso);
        lista.forEach(this::rellenarNombreEstudiante);
        return lista;
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new BusinessRuleException(
                "Evaluación no encontrada con id: " + id,
                HttpStatus.NOT_FOUND.value()
            );
        }
        repo.deleteById(id);
    }
}