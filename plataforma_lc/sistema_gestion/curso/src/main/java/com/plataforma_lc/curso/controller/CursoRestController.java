/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.curso.controller;

import com.plataforma_lc.curso.repository.CursoRepository;
import com.plataforma_lc.curso.entities.Curso;
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
    public ResponseEntity<?> get(@PathVariable("id") Long id) {
        Optional<Curso> optionalCurso = cursoRepository.findById(id);

        if (optionalCurso.isPresent()) {
            return new ResponseEntity<>(optionalCurso.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<?> post(@Valid @RequestBody Curso input) {
        Curso guardado = cursoRepository.save(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable("id") Long id, @RequestBody Curso input) {
        Optional<Curso> optionalCurso = cursoRepository.findById(id);

        if (optionalCurso.isPresent()) {
            Curso curso = optionalCurso.get();
            curso.setNombre(input.getNombre());
            curso.setCodigo(input.getCodigo());
            curso.setProfesorId(input.getProfesorId());

            Curso guardado = cursoRepository.save(curso);
            return new ResponseEntity<>(guardado, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        cursoRepository.deleteById(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}