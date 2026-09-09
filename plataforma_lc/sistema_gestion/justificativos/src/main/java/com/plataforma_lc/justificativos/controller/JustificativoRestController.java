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
                                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                                @RequestHeader(value = "X-User-Roles", required = false) String roles) throws BusinessRuleException {
        requireRole(roles, "PROFESOR");

        input.setEstado(EstadoJustificativo.PENDIENTE);
        input.setAutorId(userId);
        Justificativo guardado = repository.save(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<Justificativo>> porEstudiante(@PathVariable("estudianteId") Long estudianteId,
                                                               @RequestHeader(value = "X-User-Roles", required = false) String roles) throws BusinessRuleException {
        requireAnyRole(roles, "PROFESOR", "ADMIN");
        return ResponseEntity.ok(repository.findByEstudianteId(estudianteId));
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Justificativo>> porCurso(@PathVariable("cursoId") Long cursoId,
                                                          @RequestHeader(value = "X-User-Roles", required = false) String roles) throws BusinessRuleException {
        requireAnyRole(roles, "PROFESOR", "ADMIN");
        return ResponseEntity.ok(repository.findByCursoId(cursoId));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<Justificativo>> pendientes(@RequestHeader(value = "X-User-Roles", required = false) String roles) throws BusinessRuleException {
        requireRole(roles, "ADMIN");
        return ResponseEntity.ok(repository.findByEstado(EstadoJustificativo.PENDIENTE));
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<Justificativo> aprobar(@PathVariable("id") Long id,
                                                  @RequestHeader(value = "X-User-Id", required = false) String userId,
                                                  @RequestHeader(value = "X-User-Roles", required = false) String roles) throws BusinessRuleException {
        requireRole(roles, "ADMIN");
        return ResponseEntity.ok(resolver(id, EstadoJustificativo.APROBADO, userId));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<Justificativo> rechazar(@PathVariable("id") Long id,
                                                   @RequestHeader(value = "X-User-Id", required = false) String userId,
                                                   @RequestHeader(value = "X-User-Roles", required = false) String roles) throws BusinessRuleException {
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
        if (rolesHeader == null) {
            throw new BusinessRuleException("No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value());
        }
        String upperHeader = rolesHeader.toUpperCase();
        String upperRequired = required.toUpperCase();

        if (!upperHeader.contains(upperRequired) && !upperHeader.contains("ROLE_" + upperRequired)) {
            throw new BusinessRuleException(
                "No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value()
            );
        }
    }

    private void requireAnyRole(String rolesHeader, String... allowed) {
    System.out.println(">>> ROLES HEADER RECIBIDO: [" + rolesHeader + "]"); // Log diagnóstico
    
    if (rolesHeader == null) {
        throw new BusinessRuleException("No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value());
    }
        String upperHeader = rolesHeader.toUpperCase();

        for (String role : allowed) {
            String upperRole = role.toUpperCase();
            if (upperHeader.contains(upperRole) || upperHeader.contains("ROLE_" + upperRole)) {
                return;
            }
        }
        throw new BusinessRuleException("No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value());
    }
}