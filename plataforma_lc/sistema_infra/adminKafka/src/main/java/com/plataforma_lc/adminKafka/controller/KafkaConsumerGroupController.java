package com.plataforma_lc.adminKafka.controller;

import com.plataforma_lc.adminKafka.dto.KafkaResponses.ConsumerGroupDetalleResponse;
import com.plataforma_lc.adminKafka.dto.KafkaResponses.ConsumerGroupItemResponse;
import com.plataforma_lc.adminKafka.dto.KafkaResponses.MensajeResponse;
import com.plataforma_lc.adminKafka.service.KafkaAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST delgado para administración e inspección de Consumer Groups en Kafka.
 */
@Slf4j
@RestController
@RequestMapping("/api/kafka/admin/consumer-groups")
@RequiredArgsConstructor
public class KafkaConsumerGroupController {

    private final KafkaAdminService adminService;

    /**
     * Lista todos los consumer groups activos y su estado.
     * GET /api/kafka/admin/consumer-groups
     */
    @GetMapping
    public ResponseEntity<List<ConsumerGroupItemResponse>> listarConsumerGroups() {
        return ResponseEntity.ok(adminService.listarConsumerGroups());
    }

    /**
     * Describe a fondo un consumer group: miembros, coordinador, particiones asignadas y lag por partición.
     * GET /api/kafka/admin/consumer-groups/{groupId}
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<ConsumerGroupDetalleResponse> describirConsumerGroup(@PathVariable String groupId) {
        return ResponseEntity.ok(adminService.describirConsumerGroup(groupId));
    }

    /**
     * Elimina un consumer group vacío.
     * DELETE /api/kafka/admin/consumer-groups/{groupId}
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<MensajeResponse> eliminarConsumerGroup(@PathVariable String groupId) {
        return ResponseEntity.ok(adminService.eliminarConsumerGroup(groupId));
    }
}

