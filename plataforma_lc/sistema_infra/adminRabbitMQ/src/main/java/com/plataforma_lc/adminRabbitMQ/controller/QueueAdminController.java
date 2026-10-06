package com.plataforma_lc.adminRabbitMQ.controller;

import com.plataforma_lc.adminRabbitMQ.dto.BindingRequestDTO;
import com.plataforma_lc.adminRabbitMQ.dto.ExchangeRequestDTO;
import com.plataforma_lc.adminRabbitMQ.dto.QueueRequestDTO;
import com.plataforma_lc.adminRabbitMQ.service.ListenerManagementService;
import com.plataforma_lc.adminRabbitMQ.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class QueueAdminController {

    private final RabbitAdminService adminService;
    private final ListenerManagementService listenerService;

    public QueueAdminController(RabbitAdminService adminService,
                                 ListenerManagementService listenerService) {
        this.adminService = adminService;
        this.listenerService = listenerService;
    }

    // ════════════════════════════════════════════════════════════════════════
    // COLAS
    // ════════════════════════════════════════════════════════════════════════

    @PostMapping("/queues")
    public ResponseEntity<String> createQueue(@Valid @RequestBody QueueRequestDTO dto) {
        adminService.createQueue(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Cola '" + dto.getName() + "' creada correctamente.");
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<String> deleteQueue(@PathVariable("name") String name) {
        boolean deleted = adminService.deleteQueue(name);
        if (deleted) {
            return ResponseEntity.ok("Cola '" + name + "' eliminada correctamente.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("La cola '" + name + "' no fue encontrada.");
    }

    @GetMapping("/queues/{name}")
    public ResponseEntity<?> getQueueInfo(@PathVariable("name") String name) {
        QueueInformation info = adminService.getQueueInfo(name);
        if (info == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("La cola '" + name + "' no existe en el broker.");
        }
        return ResponseEntity.ok(info);
    }

    @PostMapping("/queues/{name}/purge")
    public ResponseEntity<String> purgeQueue(@PathVariable("name") String name) {
        int purged = adminService.purgeQueue(name);
        return ResponseEntity.ok("Cola '" + name + "' purgada. Mensajes eliminados: " + purged);
    }

    // ════════════════════════════════════════════════════════════════════════
    // EXCHANGES
    // ════════════════════════════════════════════════════════════════════════

    @PostMapping("/exchanges")
    public ResponseEntity<String> createExchange(@Valid @RequestBody ExchangeRequestDTO dto) {
        adminService.createExchange(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Exchange '" + dto.getName() + "' (tipo: " + dto.getType() + ") creado correctamente.");
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<String> deleteExchange(@PathVariable("name") String name) {
        boolean deleted = adminService.deleteExchange(name);
        if (deleted) {
            return ResponseEntity.ok("Exchange '" + name + "' eliminado correctamente.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("El exchange '" + name + "' no fue encontrado.");
    }

    // ════════════════════════════════════════════════════════════════════════
    // BINDINGS
    // ════════════════════════════════════════════════════════════════════════

    @PostMapping("/bindings")
    public ResponseEntity<String> createBinding(@Valid @RequestBody BindingRequestDTO dto) {
        adminService.createBinding(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Binding creado: " + dto.getQueueName() + " <-> " + dto.getExchangeName()
                        + " [" + dto.getRoutingKey() + "]");
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<String> removeBinding(@Valid @RequestBody BindingRequestDTO dto) {
        adminService.removeBinding(dto);
        return ResponseEntity.ok("Binding eliminado: " + dto.getQueueName() + " <-> " + dto.getExchangeName());
    }

    // ════════════════════════════════════════════════════════════════════════
    // LISTENERS (Control dinámico de consumers)
    // ════════════════════════════════════════════════════════════════════════

    @GetMapping("/listeners")
    public ResponseEntity<List<String>> getAllListeners() {
        return ResponseEntity.ok(listenerService.getAllListenerIds());
    }

    @PostMapping("/listeners/{listenerId}/pause")
    public ResponseEntity<String> pauseListener(@PathVariable("listenerId") String listenerId) {
        String result = listenerService.pauseListener(listenerId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/listeners/{listenerId}/resume")
    public ResponseEntity<String> resumeListener(@PathVariable("listenerId") String listenerId) {
        String result = listenerService.resumeListener(listenerId);
        return ResponseEntity.ok(result);
    }
}
