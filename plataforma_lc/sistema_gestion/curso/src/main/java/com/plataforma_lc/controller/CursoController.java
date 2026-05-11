/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.plataforma_lc.entities.Curso;
import com.plataforma_lc.repository.CursoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author Olov
 */
@RestController
@RequestMapping("/curso")
public class CursoController {
    @Autowired
    private CursoRepository cursoRepository;

    // 1. Listar todos los cursos
    @GetMapping
    public ResponseEntity<List<Curso>> list() {
        List<Curso> cursos = cursoRepository.findAll();
        if (cursos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(cursos);
    }

    // 2. Obtener un curso específico por su ID
    // (Este es el endpoint que llama el MS Estudiantes con el WebClient)
    @GetMapping("/{id}")
    public ResponseEntity<Curso> get(@PathVariable Long id) {
        Optional<Curso> curso = cursoRepository.findById(id);
        
        // Forma funcional y elegante de retornar el objeto o un 404
        return curso.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. Crear un nuevo curso
    @PostMapping
    public ResponseEntity<Curso> post(@RequestBody Curso input) {
        Curso nuevoCurso = cursoRepository.save(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCurso);
    }

    // 4. Actualizar un curso existente
    @PutMapping("/{id}")
    public ResponseEntity<Curso> put(@PathVariable Long id, @RequestBody Curso input) {
        Optional<Curso> optionalCurso = cursoRepository.findById(id);

        if (optionalCurso.isPresent()) {
            Curso cursoExistente = optionalCurso.get();
            // Actualizamos los campos
            cursoExistente.setNombre(input.getNombre());
            cursoExistente.setDescripcion(input.getDescripcion());
            cursoExistente.setCreditos(input.getCreditos());
            
            Curso cursoActualizado = cursoRepository.save(cursoExistente);
            return ResponseEntity.ok(cursoActualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. Eliminar un curso
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (cursoRepository.existsById(id)) {
            cursoRepository.deleteById(id);
            return ResponseEntity.ok().build(); // Retorna 200 OK sin cuerpo
        } else {
            return ResponseEntity.notFound().build(); // Retorna 404 si no existe
        }
    }
}

