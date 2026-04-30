/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.evaluaciones.service;


import com.plataforma_lc.evaluaciones.entities.evaluaciones;
import com.plataforma_lc.evaluaciones.repository.evaluacionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 *
 * @author juako
 */
@Service
public class evaluacionesService {
    @Autowired
    private evaluacionesRepository repo;

    // Registrar evaluación
    public evaluaciones guardar(evaluaciones e) {
        return repo.save(e);
    }

    // Actualizar por ID
    public evaluaciones actualizar(Long id, evaluaciones nueva) {
        evaluaciones e = repo.findById(id).orElse(null);
        if (e != null) {
            e.setNombre(nueva.getNombre());
            e.setCurso(nueva.getCurso());
            return repo.save(e);
        }
        return null;
    }

    // Consultar por curso
    public List<evaluaciones> porCurso(String curso) {
        return repo.findByCurso(curso);
    }

    // Registrar calificación 
    public evaluaciones registrarNota(Long id, int nota) {
        if (nota < 1 || nota > 7) {
            throw new RuntimeException("Nota fuera de rango");
        }

        evaluaciones e = repo.findById(id).orElse(null);
        if (e != null) {
            e.setCalificacion(nota);
            return repo.save(e);
        }
        return null;
    }

    // Consultar por estudiante
    public List<evaluaciones> porEstudiante(Long id) {
        return repo.findByEstudianteId(id);
    }
}
    
    

