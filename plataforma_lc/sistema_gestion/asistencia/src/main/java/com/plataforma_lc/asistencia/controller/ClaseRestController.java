/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.controller;

import com.plataforma_lc.asistencia.entities.Clase;
import com.plataforma_lc.asistencia.entities.CursoResponse;
import com.plataforma_lc.asistencia.repository.ClaseRepository;
import com.plataforma_lc.asistencia.service.CursoClientService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clase")
public class ClaseRestController {

    @Autowired
    private ClaseRepository claseRepository;
    
    @Autowired
    private CursoClientService cursoClientService;

    @GetMapping
    public List<Clase> list() {
        return claseRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Optional<Clase> opt = claseRepository.findById(id);
        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody Clase input) {
        if (input.getCursoId() == null || input.getCursoId() == 0) {
            return ResponseEntity.badRequest()
                .body("Error: La clase debe estar asociada a un curso.");
        }
        if (input.getFecha() == null) {
            return ResponseEntity.badRequest()
                .body("Error: La clase debe tener una fecha.");
        }

        // Validar que el curso existe ← NUEVO
        CursoResponse curso = cursoClientService.obtenerCurso(input.getCursoId());
        if (curso == null) {
            return ResponseEntity.badRequest().body("Error: El curso no existe o el servicio no está disponible.");
        }

        if (claseRepository.existeClaseEnFecha(input.getCursoId(), input.getFecha())) {
            return ResponseEntity.badRequest().body("Error: Ya existe una clase registrada para ese curso en esa fecha.");
        }

        return ResponseEntity.ok(claseRepository.save(input));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable Long id, @RequestBody Clase input) {
        Optional<Clase> opt = claseRepository.findById(id);
        if (opt.isPresent()) {
            Clase clase = opt.get();
            clase.setCursoId(input.getCursoId());
            clase.setFecha(input.getFecha());
            clase.setDescripcion(input.getDescripcion());
            return ResponseEntity.ok(claseRepository.save(clase));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        claseRepository.deleteById(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Clase>> porCurso(@PathVariable Long cursoId) {
        return ResponseEntity.ok(claseRepository.buscarPorCurso(cursoId));
    }
}