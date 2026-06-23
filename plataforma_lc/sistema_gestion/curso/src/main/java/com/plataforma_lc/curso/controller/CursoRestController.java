/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.curso.controller;

import com.plataforma_lc.curso.repository.CursoRepository;
import com.plataforma_lc.curso.entities.Curso;
import com.plataforma_lc.curso.entities.EstudianteResponse;
import com.plataforma_lc.curso.exception.BusinessRuleException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@RestController
@RequestMapping("/curso")
public class CursoRestController {

    @Autowired
    CursoRepository cursoRepository;

    // Inyectamos el Builder de WebClient
    @Autowired
    private WebClient.Builder webClientBuilder;

    // Leemos la URL del microservicio desde las propiedades (con un fallback por defecto)
    @Value("${ms.estudiante.url:http://localhost:8081}")
    private String estudianteServiceUrl;

    @GetMapping
    public ResponseEntity<List<Curso>> listarTodos() {
        List<Curso> cursos = cursoRepository.findAll();
        return new ResponseEntity<>(cursos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Curso> get(@PathVariable ("id")Long id) throws BusinessRuleException {
        Curso curso = cursoRepository.findById(id)
            .orElseThrow(() -> new BusinessRuleException(
                "Curso con id " + id + " no encontrado", HttpStatus.NOT_FOUND.value()
            ));
        return ResponseEntity.ok(curso);
    }

    @PostMapping
    public ResponseEntity<Curso> post(@Valid @RequestBody Curso input) throws BusinessRuleException {
        if (input.getNombre() == null || input.getNombre().isBlank()) {
        throw new BusinessRuleException(
            "El nombre del curso es obligatorio", HttpStatus.BAD_REQUEST.value()
        );
        }
        if (input.getCodigo() == null) {
            throw new BusinessRuleException(
                "El código del curso es obligatorio", HttpStatus.BAD_REQUEST.value()
            );
        }
        boolean codigoExiste = cursoRepository.findAll()
            .stream()
            .anyMatch(c -> c.getCodigo().equals(input.getCodigo()));

        if (codigoExiste) {
            throw new BusinessRuleException(
                "Ya existe un curso con el código " + input.getCodigo(), HttpStatus.BAD_REQUEST.value()
            );
        }

        Curso guardado = cursoRepository.save(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Curso> put(@PathVariable ("id")Long id, @RequestBody Curso input) throws BusinessRuleException {
        Curso curso = cursoRepository.findById(id)
            .orElseThrow(() -> new BusinessRuleException(
                "Curso con id " + id + " no encontrado", HttpStatus.NOT_FOUND.value()
            ));

        curso.setNombre(input.getNombre());
        curso.setCodigo(input.getCodigo());
        curso.setProfesorId(input.getProfesorId());

        Curso guardado = cursoRepository.save(curso);
        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ("id")Long id) throws BusinessRuleException {
        Curso curso = cursoRepository.findById(id)
            .orElseThrow(() -> new BusinessRuleException(
                "Curso con id " + id + " no encontrado", HttpStatus.NOT_FOUND.value()
            ));

        // 1. Consultamos al MS Estudiante de forma sincrónica usando .block()
        EstudianteResponse[] estudiantes = webClientBuilder.build()
            .get()
            .uri(estudianteServiceUrl + "/estudiante/curso/" + id)
            .retrieve()
            .bodyToMono(EstudianteResponse[].class)
            .block();

        // 2. Si el arreglo contiene elementos, rompemos la regla de negocio
        if (estudiantes != null && estudiantes.length > 0) {
            throw new BusinessRuleException(
                "No se puede eliminar el curso porque tiene estudiantes asignados", 
                HttpStatus.BAD_REQUEST.value()
            );
        }

        // 3. Si está limpio, procedemos a la eliminación segura
        cursoRepository.delete(curso);
        return ResponseEntity.ok().build();
    }
}