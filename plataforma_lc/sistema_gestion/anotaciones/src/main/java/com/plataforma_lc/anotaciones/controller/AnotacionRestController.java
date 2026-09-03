package com.plataforma_lc.anotaciones.controller;

import com.plataforma_lc.anotaciones.entities.Anotacion;
import com.plataforma_lc.anotaciones.entities.TipoAnotacion;
import com.plataforma_lc.anotaciones.repository.AnotacionRepository;
import com.plataforma_lc.anotaciones.exception.BusinessRuleException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/anotaciones")
public class AnotacionRestController {

    @Autowired
    AnotacionRepository repository;

    @PostMapping
    public ResponseEntity<Anotacion> crear(@Valid @RequestBody Anotacion input,
                                            @RequestHeader("X-User-Id") String userId,
                                            @RequestHeader("X-User-Roles") String roles) {
        requireRole(roles, "PROFESOR");

        input.setAutorId(userId);
        Anotacion guardada = repository.save(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<Anotacion>> porEstudiante(@PathVariable Long estudianteId,
                                                           @RequestHeader("X-User-Roles") String roles) {
        requireAnyRole(roles, "PROFESOR", "ADMIN");
        return ResponseEntity.ok(repository.findByEstudianteId(estudianteId));
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Anotacion>> porCurso(@PathVariable Long cursoId,
                                                      @RequestHeader("X-User-Roles") String roles) {
        requireAnyRole(roles, "PROFESOR", "ADMIN");
        return ResponseEntity.ok(repository.findByCursoId(cursoId));
    }

    @GetMapping("/estudiante/{estudianteId}/tipo/{tipo}")
    public ResponseEntity<List<Anotacion>> porEstudianteYTipo(@PathVariable Long estudianteId,
                                                                @PathVariable TipoAnotacion tipo,
                                                                @RequestHeader("X-User-Roles") String roles) {
        requireAnyRole(roles, "PROFESOR", "ADMIN");
        return ResponseEntity.ok(repository.findByEstudianteIdAndTipo(estudianteId, tipo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                          @RequestHeader("X-User-Roles") String roles) {
        requireRole(roles, "ADMIN"); // solo Admin puede borrar, evita que un profesor elimine evidencia

        Anotacion anotacion = repository.findById(id)
            .orElseThrow(() -> new BusinessRuleException(
                "Anotación con id " + id + " no encontrada", HttpStatus.NOT_FOUND.value()
            ));

        repository.delete(anotacion);
        return ResponseEntity.ok().build();
    }

    // --- helpers ---

    private void requireRole(String rolesHeader, String required) {
        if (rolesHeader == null || !rolesHeader.contains(required)) {
            throw new BusinessRuleException(
                "No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value()
            );
        }
    }

    private void requireAnyRole(String rolesHeader, String... allowed) {
        if (rolesHeader == null) {
            throw new BusinessRuleException("No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value());
        }
        for (String role : allowed) {
            if (rolesHeader.contains(role)) return;
        }
        throw new BusinessRuleException("No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value());
    }
}