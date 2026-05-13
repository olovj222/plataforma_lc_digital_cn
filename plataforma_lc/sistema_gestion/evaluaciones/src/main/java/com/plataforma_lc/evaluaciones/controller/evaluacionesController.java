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
    public ResponseEntity<?> crear(@RequestBody Evaluaciones e) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(e));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Evaluaciones e) {
        try {
            return ResponseEntity.ok(service.actualizar(id, e));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/curso/{cursoId}")
    public List<Evaluaciones> porCurso(@PathVariable Long cursoId) {
        return service.porCurso(cursoId);
    }

    @PostMapping("/{id}/nota")
    public ResponseEntity<?> ponerNota(@PathVariable Long id, @RequestParam int nota) {
        try {
            return ResponseEntity.ok(service.registrarNota(id, nota));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/estudiante/{id}")
    public List<Evaluaciones> porEstudiante(@PathVariable Long id) {
        return service.porEstudiante(id);
    }
}