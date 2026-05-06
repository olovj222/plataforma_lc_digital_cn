/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/RestController.java to edit this template
 */
package com.plataforma_lc.estudiante.controller;

import com.plataforma_lc.estudiante.entities.CursoResponse;
import com.plataforma_lc.estudiante.entities.Estudiante;
import com.plataforma_lc.estudiante.entities.EstudianteCurso;
import com.plataforma_lc.estudiante.repository.EstudianteRepository;
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
    private WebClient.Builder webClientBuilder;
    
    @GetMapping()
    public ResponseEntity<List<Estudiante>> list() {
        List<Estudiante> findAll = estudianteRepository.findAll();
        if (findAll.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(findAll);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Long id) {
        Optional<Estudiante> optionalEstudiante = estudianteRepository.findById(id);

        if (optionalEstudiante.isPresent()) {
            Estudiante estudiante = optionalEstudiante.get();
            List<EstudianteCurso> cursosNombres = new ArrayList<>();

            try {
                // Iteramos sobre la relación EstudianteCurso para obtener detalles del microservicio de Cursos
                for (EstudianteCurso relacion : estudiante.getCursos()) {
                    
                    // Llamada al microservicio de CURSOS (ajusta el nombre del servicio en el Discovery Server)
                    CursoResponse curso = webClientBuilder.build()
                            .get()
                            .uri("http://MS-CURSOS/curso/{id}", relacion.getCursoId())
                            .retrieve()
                            .bodyToMono(CursoResponse.class)
                            .block(); // Bloqueante para simplificar la lógica igual al ejemplo

                    if (curso != null) {
                        EstudianteCurso cursoRespuesta = new EstudianteCurso();
                        cursoRespuesta.setCursoName(curso.getNombre());
                        cursoRespuesta.setCursoId(curso.getId());
                        cursosNombres.add(cursoRespuesta);
                    }
                }
            } catch (Exception ex) {
                // Excepción personalizada del módulo Estudiante
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al consultar microservicio de Cursos: " + ex.getMessage());
            }
            
            estudiante.setCursos(cursosNombres);
            return new ResponseEntity<>(estudiante, HttpStatus.OK);
            
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable Long id, @RequestBody Estudiante input) {
        Optional<Estudiante> optionalEstudiante = estudianteRepository.findById(id);

        if (optionalEstudiante.isPresent()) {
            Estudiante newEstudiante = optionalEstudiante.get();
            // Actualizamos los campos básicos
            newEstudiante.setNombre(input.getNombre());
            
            Estudiante guardado = estudianteRepository.save(newEstudiante);
            return new ResponseEntity<>(guardado, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<?> post(@Valid @RequestBody Estudiante input) {
        // Regla: Debe estar asociado al menos a un curso
        if (input.getCursos() == null || input.getCursos().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: El estudiante debe estar asociado al menos a un curso para registrarse.");
        }

        input.getCursos().forEach(curso -> curso.setEstudiante(input));
        Estudiante retorno = estudianteRepository.save(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(retorno);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        estudianteRepository.deleteById(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
