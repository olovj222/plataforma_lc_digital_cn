/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/RestController.java to edit this template
 */
package com.plataforma_lc.estudiante.controller;


import com.plataforma_lc.estudiante.entities.CursoResponse;
import com.plataforma_lc.estudiante.entities.Estudiante;
import com.plataforma_lc.estudiante.entities.EstudianteCurso;
import com.plataforma_lc.estudiante.exception.BusinessRuleException;
import com.plataforma_lc.estudiante.repository.EstudianteRepository;
import com.plataforma_lc.estudiante.service.CursoClientService;
import jakarta.validation.Valid;
import java.util.ArrayList;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.reactive.function.client.WebClient;

/**
 *
 * @author Olov
 */
@RestController
@RequestMapping("/estudiante")
public class EstudianteRestController {
    
    @Autowired
    EstudianteRepository estudianteRepository;
    
    @Autowired
    CursoClientService cursoClientService;
    
    @GetMapping
    public ResponseEntity<List<Estudiante>> listarTodos() {
        List<Estudiante> estudiantes = estudianteRepository.findAll();

        for (Estudiante estudiante : estudiantes) {
            for (EstudianteCurso relacion : estudiante.getCursos()) {
                CursoResponse cursoRespuesta = cursoClientService.obtenerCurso(relacion.getCursoId());
                if (cursoRespuesta != null) {
                    relacion.setCursoName(cursoRespuesta.getNombre());
                }
            }
        }
        return new ResponseEntity<>(estudiantes, HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Estudiante> get(@PathVariable("id") Long id) throws BusinessRuleException {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException(
                        "Estudiante con id " + id + " no encontrado", HttpStatus.NOT_FOUND.value()
                ));

        for (EstudianteCurso relacion : estudiante.getCursos()) {
            CursoResponse cursoRespuesta = cursoClientService.obtenerCurso(relacion.getCursoId());
            if (cursoRespuesta != null) {
                relacion.setCursoName(cursoRespuesta.getNombre());
            }
        }
        return new ResponseEntity<>(estudiante, HttpStatus.OK);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Estudiante> put(@PathVariable("id") Long id, @RequestBody Estudiante input) throws BusinessRuleException {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException(
                        "Estudiante con id " + id + " no encontrado", HttpStatus.NOT_FOUND.value()
                ));

        estudiante.setNombre(input.getNombre());
        estudiante.setApPaterno(input.getApPaterno());
        estudiante.setApMaterno(input.getApMaterno());
        estudiante.setDireccion(input.getDireccion());
        estudiante.setTelefono(input.getTelefono());
        
        // Actualizar cursos
        estudiante.getCursos().clear();
        input.getCursos().forEach(curso -> {
            curso.setEstudiante(estudiante);
            estudiante.getCursos().add(curso);
        });

        Estudiante guardado = estudianteRepository.save(estudiante);
        return new ResponseEntity<>(guardado, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Estudiante> post(@Valid @RequestBody Estudiante input) throws BusinessRuleException {
        if (input.getCursos() == null || input.getCursos().isEmpty()) {
            throw new BusinessRuleException(
                    "El estudiante debe estar asociado al menos a un curso", HttpStatus.BAD_REQUEST.value()
            );
        }

        input.getCursos().forEach(curso -> curso.setEstudiante(input));
        Estudiante guardado = estudianteRepository.save(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) throws BusinessRuleException {
        estudianteRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException(
                        "Estudiante con id " + id + " no encontrado", HttpStatus.NOT_FOUND.value()
                ));

        estudianteRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
