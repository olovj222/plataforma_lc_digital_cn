package com.plataforma_lc.adminRabbitMQ.controller;

import com.plataforma_lc.adminRabbitMQ.dto.QueueRequestDTO;
import com.plataforma_lc.adminRabbitMQ.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/queues")
public class QueueAdminController {

    private final RabbitAdminService adminService;

    public QueueAdminController(RabbitAdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<String> createQueue(@Valid @RequestBody QueueRequestDTO dto) {
        adminService.createQueue(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Cola '" + dto.getName() + "' creada correctamente.");
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<String> deleteQueue(@PathVariable("name") String name) {
        boolean deleted = adminService.deleteQueue(name);
        if (deleted) {
            return ResponseEntity.ok("Cola '" + name + "' eliminada correctamente.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("La cola '" + name + "' no fue encontrada.");
    }
}
