/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.curso.controller;

import com.plataforma_lc.curso.repository.CursoRepository;
import com.plataforma_lc.curso.entities.Curso;
import com.plataforma_lc.curso.exception.BusinessRuleException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/curso")
public class CursoRestController {

    @Autowired
    CursoRepository cursoRepository;

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

        // Verificar si tiene estudiantes asignados requiere consulta al MS estudiante
        // Por ahora validamos que exista y eliminamos
        cursoRepository.delete(curso);
        return ResponseEntity.ok().build();
    }
}