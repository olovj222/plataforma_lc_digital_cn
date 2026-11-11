package com.plataforma_lc.adminKafka.controller;

import com.plataforma_lc.adminKafka.dto.KafkaResponses.*;
import com.plataforma_lc.adminKafka.dto.TopicRequestDTO;
import com.plataforma_lc.adminKafka.service.KafkaAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST delgado para administración de tópicos y clúster Kafka.
 * Toda la lógica de negocio y llamadas a AdminClient delegan en {@link KafkaAdminService}.
 */
@Slf4j
@RestController
@RequestMapping("/api/kafka/admin")
@RequiredArgsConstructor
public class KafkaAdminController {

    private final KafkaAdminService adminService;

    // ========================================================================================
    // TÓPICOS
    // ========================================================================================

    /**
     * Lista todos los tópicos del clúster (excluyendo internos).
     * GET /api/kafka/admin/topics
     */
    @GetMapping("/topics")
    public ResponseEntity<ListadoTopicosResponse> listarTopicos() {
        return ResponseEntity.ok(adminService.listarTopicos());
    }

    /**
     * Crea un nuevo tópico con parámetros dinámicos validados.
     * POST /api/kafka/admin/topics
     */
    @PostMapping("/topics")
    public ResponseEntity<MensajeResponse> crearTopico(@Valid @RequestBody TopicRequestDTO request) {
        MensajeResponse respuesta = adminService.crearTopico(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Inspecciona a fondo un tópico existente (particiones, réplicas, ISR, configuraciones).
     * GET /api/kafka/admin/topics/{nombreTopico}
     */
    @GetMapping("/topics/{nombreTopico}")
    public ResponseEntity<TopicoDetalleResponse> inspeccionarTopico(@PathVariable("nombreTopico") String nombreTopico) {
        return ResponseEntity.ok(adminService.inspeccionarTopico(nombreTopico));
    }

    /**
     * Modifica dinámicamente configuraciones de un tópico con incrementalAlterConfigs (SET).
     * PUT /api/kafka/admin/topics/{nombreTopico}/config
     */
    @PutMapping("/topics/{nombreTopico}/config")
    public ResponseEntity<MensajeResponse> modificarConfiguracion(
            @PathVariable("nombreTopico") String nombreTopico,
            @RequestBody Map<String, String> configs) {
        return ResponseEntity.ok(adminService.modificarConfiguracion(nombreTopico, configs));
    }

    /**
     * Incrementa el número de particiones de un tópico (nunca permite reducirlas).
     * PUT /api/kafka/admin/topics/{nombreTopico}/particiones
     */
    @PutMapping("/topics/{nombreTopico}/particiones")
    public ResponseEntity<MensajeResponse> aumentarParticiones(
            @PathVariable("nombreTopico") String nombreTopico,
            @RequestBody Map<String, Integer> body) {
        Integer particiones = body != null ? body.get("particiones") : null;
        if (particiones == null) {
            throw new IllegalArgumentException("El cuerpo debe contener el campo 'particiones' con un valor entero positivo");
        }
        return ResponseEntity.ok(adminService.aumentarParticiones(nombreTopico, particiones));
    }

    /**
     * Elimina un tópico. Si es un tópico del sistema protegido, exige ?force=true.
     * DELETE /api/kafka/admin/topics/{nombreTopico}
     */
    @DeleteMapping("/topics/{nombreTopico}")
    public ResponseEntity<MensajeResponse> eliminarTopico(
            @PathVariable("nombreTopico") String nombreTopico,
            @RequestParam(name = "force", defaultValue = "false") boolean force) {
        return ResponseEntity.ok(adminService.eliminarTopico(nombreTopico, force));
    }

    // ========================================================================================
    // ALIAS DEPRECATED PARA COMPATIBILIDAD RETROACTIVA
    // ========================================================================================

    /**
     * Alias retrocompatible para listar consumer groups.
     * @deprecated Usar {@link KafkaConsumerGroupController#listarConsumerGroups()} en /api/kafka/admin/consumer-groups.
     */
    @Deprecated
    @GetMapping("/topics/consumer-groups")
    public ResponseEntity<List<ConsumerGroupItemResponse>> listarConsumerGroupsAlias() {
        return ResponseEntity.ok(adminService.listarConsumerGroups());
    }

    // ========================================================================================
    // CLÚSTER
    // ========================================================================================

    /**
     * Retorna información del clúster Kafka (clusterId, controlador, lista de brokers).
     * GET /api/kafka/admin/cluster
     */
    @GetMapping("/cluster")
    public ResponseEntity<ClusterInfoResponse> obtenerInfoCluster() {
        return ResponseEntity.ok(adminService.obtenerInfoCluster());
    }
}
