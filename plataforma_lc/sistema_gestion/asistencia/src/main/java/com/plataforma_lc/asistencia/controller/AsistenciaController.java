/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.controller;

import com.plataforma_lc.asistencia.repository.AsistenciaRepository;
import com.plataforma_lc.asistencia.entities.Asistencia;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/asistencia")
public class AsistenciaController {
    @Autowired
    private AsistenciaRepository asistenciaRepository;
    
    @GetMapping()
    public List<Asistencia> list(){
        return asistenciaRepository.findAll();
    }
    
    @PostMapping
    public ResponseEntity<?> post(@RequestBody Asistencia input) {
        
        if (input.getId_curso() == 0) {
            return ResponseEntity.badRequest().body("Error: La asistencia debe tener una clase asignada (id_curso).");
        }

        if (input.getFecha() == null) {
            return ResponseEntity.badRequest().body("Error: La asistencia debe tener una fecha especificada.");
        }

        List<String> estadosValidos = new ArrayList<String>();
        estadosValidos.add("PRESENT");
        estadosValidos.add("ABSENT");
        estadosValidos.add("JUSTIFIED");
        if (input.getEstado() == null || !estadosValidos.contains(input.getEstado().toUpperCase())) {
            return ResponseEntity.badRequest().body("Error: El estado debe ser PRESENT, ABSENT o JUSTIFIED.");
        }
        input.setEstado(input.getEstado().toUpperCase());

        boolean yaExiste = asistenciaRepository.existeRegistroDuplicado(
            input.getId_curso(), 
            input.getId_estudiante(), 
            input.getFecha()
        );

        if (yaExiste) {
            return ResponseEntity.badRequest().body("Error: El estudiante ya tiene asistencia registrada en este curso para la fecha indicada.");
        }

        Asistencia retorno = asistenciaRepository.save(input);
        return ResponseEntity.ok(retorno);

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
