package com.plataforma_lc.justificativos.controller;

import com.plataforma_lc.justificativos.entities.Justificativo;
import com.plataforma_lc.justificativos.entities.EstadoJustificativo;
import com.plataforma_lc.justificativos.repository.JustificativoRepository;
import com.plataforma_lc.justificativos.exception.BusinessRuleException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/justificativos")
public class JustificativoRestController {

    @Autowired
    JustificativoRepository repository;

    @PostMapping
    public ResponseEntity<Justificativo> crear(@Valid @RequestBody Justificativo input,
                                                @RequestHeader("X-User-Id") String userId,
                                                @RequestHeader("X-User-Roles") String roles) throws BusinessRuleException {
        requireRole(roles, "PROFESOR");

        input.setEstado(EstadoJustificativo.PENDIENTE);
        input.setAutorId(userId);
        Justificativo guardado = repository.save(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<Justificativo>> porEstudiante(@PathVariable Long estudianteId,
                                                               @RequestHeader("X-User-Roles") String roles) throws BusinessRuleException {
        requireAnyRole(roles, "PROFESOR", "ADMIN");
        return ResponseEntity.ok(repository.findByEstudianteId(estudianteId));
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Justificativo>> porCurso(@PathVariable Long cursoId,
                                                          @RequestHeader("X-User-Roles") String roles) throws BusinessRuleException {
        requireAnyRole(roles, "PROFESOR", "ADMIN");
        return ResponseEntity.ok(repository.findByCursoId(cursoId));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<Justificativo>> pendientes(@RequestHeader("X-User-Roles") String roles) throws BusinessRuleException {
        requireRole(roles, "ADMIN");
        return ResponseEntity.ok(repository.findByEstado(EstadoJustificativo.PENDIENTE));
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<Justificativo> aprobar(@PathVariable Long id,
                                                  @RequestHeader("X-User-Id") String userId,
                                                  @RequestHeader("X-User-Roles") String roles) throws BusinessRuleException {
        requireRole(roles, "ADMIN");
        return ResponseEntity.ok(resolver(id, EstadoJustificativo.APROBADO, userId));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<Justificativo> rechazar(@PathVariable Long id,
                                                   @RequestHeader("X-User-Id") String userId,
                                                   @RequestHeader("X-User-Roles") String roles) throws BusinessRuleException {
        requireRole(roles, "ADMIN");
        return ResponseEntity.ok(resolver(id, EstadoJustificativo.RECHAZADO, userId));
    }

    // --- helpers ---

    private Justificativo resolver(Long id, EstadoJustificativo estado, String resueltoPor) {
    Justificativo j = repository.findById(id)
        .orElseThrow(() -> new BusinessRuleException(
            "Justificativo con id " + id + " no encontrado", HttpStatus.NOT_FOUND.value()
        ));
    j.setEstado(estado);
    j.setResueltoPor(resueltoPor);
    j.setFechaResolucion(LocalDateTime.now());
    return repository.save(j);
}

    private void requireRole(String rolesHeader, String required) throws BusinessRuleException {
        if (rolesHeader == null || !rolesHeader.contains(required)) {
            throw new BusinessRuleException(
                "No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value()
            );
        }
    }

    private void requireAnyRole(String rolesHeader, String... allowed) throws BusinessRuleException {
        if (rolesHeader == null) {
            throw new BusinessRuleException("No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value());
        }
        for (String role : allowed) {
            if (rolesHeader.contains(role)) return;
        }
        throw new BusinessRuleException("No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value());
    }
}