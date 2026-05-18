/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.controller;

import com.plataforma_lc.asistencia.repository.AsistenciaRepository;
import com.plataforma_lc.asistencia.entities.Asistencia;
import com.plataforma_lc.asistencia.entities.Clase;
import com.plataforma_lc.asistencia.entities.EstudianteResponse;
import com.plataforma_lc.asistencia.repository.ClaseRepository;
import com.plataforma_lc.asistencia.service.EstudianteClientService;
import java.util.ArrayList;
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
@RequestMapping("/asistencia")
public class AsistenciaRestController {
    @Autowired
    private AsistenciaRepository asistenciaRepository;
    
    @Autowired
    private EstudianteClientService estudianteClientService;
    
    @GetMapping()
    public List<Asistencia> list(){
        return asistenciaRepository.findAll();
    }
    
    @Autowired
    private ClaseRepository claseRepository; 
    
    @PostMapping
    public ResponseEntity<?> post(@RequestBody Asistencia input) {

        // 1. Validar que la clase existe ← NUEVO
        Optional<Clase> claseOpt = claseRepository.findById(input.getId_clase());
        if (claseOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                .body("Error: La clase especificada no existe.");
        }

        // 2. Validar fecha (ya la tienes)
        if (input.getFecha() == null) {
            return ResponseEntity.badRequest()
                .body("Error: La asistencia debe tener una fecha especificada.");
        }

        // 3. Validar estado (ya lo tienes)
        List<String> estadosValidos = List.of("PRESENT", "ABSENT", "JUSTIFIED");
        if (input.getEstado() == null || !estadosValidos.contains(input.getEstado().toUpperCase())) {
            return ResponseEntity.badRequest()
                .body("Error: El estado debe ser PRESENT, ABSENT o JUSTIFIED.");
        }
        input.setEstado(input.getEstado().toUpperCase());

        // 4. Validar duplicado (ya lo tienes)
        boolean yaExiste = asistenciaRepository.existeRegistroDuplicado(
            input.getId_clase(),
            input.getId_estudiante(),
            input.getFecha()
        );
        if (yaExiste) {
            return ResponseEntity.badRequest()
                .body("Error: El estudiante ya tiene asistencia registrada en esta clase.");
        }

        // 5. Validar estudiante via Circuit Breaker (ya lo tienes)
        EstudianteResponse estudiante = estudianteClientService
                                    .obtenerEstudiante(input.getId_estudiante());

        if ("Estudiante no encontrado".equals(estudiante.getNombre())) {
            return ResponseEntity.badRequest()
                .body("Error: El estudiante con id " + input.getId_estudiante() + " no existe.");
        }

        if ("MS Estudiante no disponible".equals(estudiante.getNombre())) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Error: El microservicio de estudiantes no está disponible.");
        }

        return ResponseEntity.ok(asistenciaRepository.save(input));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable("id") Long id, @RequestBody Asistencia input) {
        Optional<Asistencia> optionalAsistencia = asistenciaRepository.findById(id);
        if (optionalAsistencia.isPresent()) {
            Asistencia newAsistencia = optionalAsistencia.get();
            newAsistencia.setId_clase(input.getId_clase());
            newAsistencia.setId_estudiante(input.getId_estudiante());
            newAsistencia.setEstado(input.getEstado());
            newAsistencia.setFecha(input.getFecha());
            
            Asistencia guardado = asistenciaRepository.save(newAsistencia);
            return new ResponseEntity<>(guardado, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        asistenciaRepository.deleteById(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }
    
    @GetMapping("/curso/{idCurso}")
    public ResponseEntity<List<Asistencia>> consultarPorCurso(@PathVariable("idCurso") long idCurso) {
        List<Asistencia> lista = asistenciaRepository.buscarPorCurso(idCurso);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/estudiante/{idEstudiante}")
    public ResponseEntity<List<Asistencia>> consultarPorEstudiante(@PathVariable("idEstudiante") long idEstudiante) {
        List<Asistencia> lista = asistenciaRepository.buscarPorEstudiante(idEstudiante);
        return ResponseEntity.ok(lista);
    }
    
}
