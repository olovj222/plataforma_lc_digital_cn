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
    
    @GetMapping
    public ResponseEntity<List<Estudiante>> listarTodos() {
    // 1. Traemos todos los estudiantes de la base de datos
    List<Estudiante> estudiantes = (List<Estudiante>) estudianteRepository.findAll();

    // 2. Recorremos cada estudiante
    for (Estudiante estudiante : estudiantes) {
        // 3. Recorremos los cursos de ese estudiante
        for (EstudianteCurso relacion : estudiante.getCursos()) {
            try {
                // Hacemos la llamada al microservicio
                CursoResponse cursoRespuesta = webClientBuilder.build()
                        .get()
                        .uri("http://localhost:8081/curso/{id}", relacion.getCursoId())
                        .retrieve()
                        .bodyToMono(CursoResponse.class)
                        .block();

                if (cursoRespuesta != null) {
                    relacion.setCursoName(cursoRespuesta.getNombre());
                }
            } catch (Exception ex) {
                // Manejo de error si el MS está apagado
                relacion.setCursoName("Información no disponible (MS apagado)");
            }
        }
    }

    // 4. Devolvemos la lista ya rellenada
    return new ResponseEntity<>(estudiantes, HttpStatus.OK);
}

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Long id) {
    Optional<Estudiante> optionalEstudiante = estudianteRepository.findById(id);

    if (optionalEstudiante.isPresent()) {
        Estudiante estudiante = optionalEstudiante.get();

        // Iteramos sobre la relación EstudianteCurso
        for (EstudianteCurso relacion : estudiante.getCursos()) {
            try {
                // Intentamos buscar el nombre del curso
                CursoResponse cursoRespuesta = webClientBuilder.build()
                        .get()
                        .uri("http://localhost:8081/curso/{id}", relacion.getCursoId())
                        .retrieve()
                        .bodyToMono(CursoResponse.class)
                        .block(); 

                if (cursoRespuesta != null) {
                    relacion.setCursoName(cursoRespuesta.getNombre());
                }
            } catch (Exception ex) {
                // Si este curso en particular falla (o el MS está apagado),
                // manejamos el error SOLAMENTE para este registro, sin botar la app.
                relacion.setCursoName("Información no disponible (MS apagado)");
            }
        }
        
        // Retornamos SIEMPRE al estudiante. Si el MS de cursos estaba encendido, 
        // tendrá los nombres reales. Si estaba apagado, tendrá el texto de advertencia.
        return new ResponseEntity<>(estudiante, HttpStatus.OK);
        
    } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable("id") Long id, @RequestBody Estudiante input) {
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
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        estudianteRepository.deleteById(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
