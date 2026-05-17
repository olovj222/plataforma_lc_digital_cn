/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.controller;

import com.plataforma_lc.asistencia.repository.AsistenciaRepository;
import com.plataforma_lc.asistencia.entities.Asistencia;
import com.plataforma_lc.asistencia.entities.EstudianteResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/asistencia")
public class AsistenciaRestController {
    @Autowired
    private AsistenciaRepository asistenciaRepository;
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
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

        try {
            EstudianteResponse estudianteRespuesta = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8080/estudiante/{id}", input.getId_estudiante())
                    .retrieve()
                    .bodyToMono(EstudianteResponse.class)
                    .block();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: El microservicio esta apagado o no se encontro el registro.");
        }
        
        Asistencia retorno = asistenciaRepository.save(input);
        return ResponseEntity.ok(retorno);

    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable("id") Long id, @RequestBody Asistencia input) {
        Optional<Asistencia> optionalAsistencia = asistenciaRepository.findById(id);
        if (optionalAsistencia.isPresent()) {
            Asistencia newAsistencia = optionalAsistencia.get();
            newAsistencia.setId_curso(input.getId_curso());
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
