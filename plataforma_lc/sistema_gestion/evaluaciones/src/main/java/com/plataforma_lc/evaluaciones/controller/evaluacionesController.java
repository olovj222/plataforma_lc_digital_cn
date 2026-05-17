/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.evaluaciones.controller;

import com.plataforma_lc.evaluaciones.entities.Evaluaciones;
import com.plataforma_lc.evaluaciones.service.evaluacionesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
/**
 *
 * @author juako
 */

@RestController
@RequestMapping("/evaluaciones")
public class evaluacionesController {

    @Autowired
    private evaluacionesService service;

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Evaluaciones e) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(e));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Evaluaciones e) {
        return ResponseEntity.ok(service.actualizar(id, e));
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Evaluaciones>> porCurso(@PathVariable Long cursoId) {
        return ResponseEntity.ok(service.porCurso(cursoId));
    }

    @PostMapping("/{id}/nota")
    public ResponseEntity<?> ponerNota(@PathVariable Long id, @RequestParam int nota) {
        return ResponseEntity.ok(service.registrarNota(id, nota));
    }

    @GetMapping("/estudiante/{id}")
    public ResponseEntity<List<Evaluaciones>> porEstudiante(@PathVariable Long id) {
        return ResponseEntity.ok(service.porEstudiante(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok("Evaluación eliminada correctamente");
    }
}