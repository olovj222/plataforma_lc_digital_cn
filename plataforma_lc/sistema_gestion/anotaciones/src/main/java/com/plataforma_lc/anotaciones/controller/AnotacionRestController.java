package com.plataforma_lc.anotaciones.controller;

import com.plataforma_lc.anotaciones.entities.Anotacion;
import com.plataforma_lc.anotaciones.entities.TipoAnotacion;
import com.plataforma_lc.anotaciones.repository.AnotacionRepository;
import com.plataforma_lc.anotaciones.exception.BusinessRuleException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/anotaciones")
public class AnotacionRestController {

    private static final Logger log = LoggerFactory.getLogger(AnotacionRestController.class);

    @Autowired
    AnotacionRepository repository;

    @PostMapping
    public ResponseEntity<Anotacion> crear(@Valid @RequestBody Anotacion input,
                                            @RequestHeader(value = "X-User-Id", required = false) String userId,
                                            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        log.info("--> POST /anotaciones | X-User-Id: '{}', X-User-Roles: '{}'", userId, roles);
        requireRole(roles, "PROFESOR");

        input.setAutorId(userId);
        Anotacion guardada = repository.save(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<Anotacion>> porEstudiante(@PathVariable("estudianteId") Long estudianteId,
                                                           @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        log.info("--> GET /anotaciones/estudiante/{} | X-User-Roles: '{}'", estudianteId, roles);
        requireAnyRole(roles, "PROFESOR", "ADMIN");
        return ResponseEntity.ok(repository.findByEstudianteId(estudianteId));
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Anotacion>> porCurso(@PathVariable("cursoId") Long cursoId,
                                                      @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        log.info("--> GET /anotaciones/curso/{} | X-User-Roles: '{}'", cursoId, roles);
        requireAnyRole(roles, "PROFESOR", "ADMIN");
        return ResponseEntity.ok(repository.findByCursoId(cursoId));
    }

    @GetMapping("/estudiante/{estudianteId}/tipo/{tipo}")
    public ResponseEntity<List<Anotacion>> porEstudianteYTipo(@PathVariable("estudianteId") Long estudianteId,
                                                               @PathVariable("tipo") TipoAnotacion tipo,
                                                               @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        log.info("--> GET /anotaciones/estudiante/{}/tipo/{} | X-User-Roles: '{}'", estudianteId, tipo, roles);
        requireAnyRole(roles, "PROFESOR", "ADMIN");
        return ResponseEntity.ok(repository.findByEstudianteIdAndTipo(estudianteId, tipo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id,
                                          @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        log.info("--> DELETE /anotaciones/{} | X-User-Roles: '{}'", id, roles);
        requireRole(roles, "ADMIN");

        Anotacion anotacion = repository.findById(id)
            .orElseThrow(() -> new BusinessRuleException(
                "Anotación con id " + id + " no encontrada", HttpStatus.NOT_FOUND.value()
            ));

        repository.delete(anotacion);
        return ResponseEntity.ok().build();
    }

    // --- helpers ---

    private void requireRole(String rolesHeader, String required) {
        log.info("Verificando requireRole -> Recibido: '{}', Requerido: '{}'", rolesHeader, required);
        if (rolesHeader == null || rolesHeader.isBlank()) {
            log.warn("DENEGADO: El header X-User-Roles es NULL o VACÍO");
            throw new BusinessRuleException("No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value());
        }
        String upperHeader = rolesHeader.toUpperCase();
        String upperRequired = required.toUpperCase();

        if (!upperHeader.contains(upperRequired) && !upperHeader.contains("ROLE_" + upperRequired)) {
            log.warn("DENEGADO: El header '{}' no contiene '{}'", upperHeader, upperRequired);
            throw new BusinessRuleException(
                "No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value()
            );
        }
    }

    private void requireAnyRole(String rolesHeader, String... allowed) {
        log.info("Verificando requireAnyRole -> Recibido: '{}', Permitidos: {}", rolesHeader, allowed);
        if (rolesHeader == null || rolesHeader.isBlank()) {
            log.warn("DENEGADO: El header X-User-Roles es NULL o VACÍO");
            throw new BusinessRuleException("No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value());
        }
        String upperHeader = rolesHeader.toUpperCase();

        for (String role : allowed) {
            String upperRole = role.toUpperCase();
            if (upperHeader.contains(upperRole) || upperHeader.contains("ROLE_" + upperRole)) {
                return;
            }
        }
        log.warn("DENEGADO: El header '{}' no coincide con ninguno de los permitidos", upperHeader);
        throw new BusinessRuleException("No tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN.value());
    }
}