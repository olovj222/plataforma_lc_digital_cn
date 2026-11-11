package com.plataforma_lc.adminKafka.controller;

import com.plataforma_lc.adminKafka.dto.AlertaEventDTO;
import com.plataforma_lc.adminKafka.dto.AnaliticaStreamDTO;
import com.plataforma_lc.adminKafka.dto.AuditoriaEventDTO;
import com.plataforma_lc.adminKafka.producer.AlertaProducer;
import com.plataforma_lc.adminKafka.producer.AnaliticaProducer;
import com.plataforma_lc.adminKafka.producer.AuditoriaProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para la publicación de mensajes a los tópicos Kafka.
 * Proporciona endpoints para enviar eventos de auditoría, analítica y alertas.
 * Valida los DTOs de entrada con {@link Valid} para asegurar integridad en los datos.
 */
@Slf4j
@RestController
@RequestMapping("/api/kafka/publish")
@RequiredArgsConstructor
public class KafkaPublishController {

    private final AuditoriaProducer auditoriaProducer;
    private final AnaliticaProducer analiticaProducer;
    private final AlertaProducer alertaProducer;

    /**
     * Publica un evento de auditoría.
     * POST /api/kafka/publish/auditoria
     *
     * @param evento DTO validado con los datos de auditoría
     * @return respuesta HTTP 202 Accepted
     */
    @PostMapping("/auditoria")
    public ResponseEntity<Map<String, String>> publicarAuditoria(@Valid @RequestBody AuditoriaEventDTO evento) {
        if (evento.getTimestamp() == null) {
            evento.setTimestamp(LocalDateTime.now());
        }
        auditoriaProducer.publicar(evento);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("mensaje", "Evento de auditoría enviado al tópico");
        response.put("servicio", evento.getServicio() != null ? evento.getServicio() : "");
        response.put("accion", evento.getAccion() != null ? evento.getAccion() : "");
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Publica un registro de analítica de asistencia.
     * POST /api/kafka/publish/analitica
     *
     * @param dto DTO validado del stream de analítica
     * @return respuesta HTTP 202 Accepted
     */
    @PostMapping("/analitica")
    public ResponseEntity<Map<String, String>> publicarAnalitica(@Valid @RequestBody AnaliticaStreamDTO dto) {
        if (dto.getTimestamp() == null) {
            dto.setTimestamp(LocalDateTime.now());
        }
        if (dto.getFecha() == null) {
            dto.setFecha(LocalDate.now());
        }
        analiticaProducer.publicar(dto);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("mensaje", "Stream de analítica enviado al tópico");
        response.put("cursoId", dto.getCursoId() != null ? String.valueOf(dto.getCursoId()) : "");
        response.put("estudianteId", dto.getEstudianteId() != null ? String.valueOf(dto.getEstudianteId()) : "");
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Publica un evento de alerta crítica.
     * POST /api/kafka/publish/alerta
     *
     * @param evento DTO validado con los datos de la alerta
     * @return respuesta HTTP 202 Accepted
     */
    @PostMapping("/alerta")
    public ResponseEntity<Map<String, String>> publicarAlerta(@Valid @RequestBody AlertaEventDTO evento) {
        if (evento.getTimestamp() == null) {
            evento.setTimestamp(LocalDateTime.now());
        }
        if (evento.getAlertaId() == null || evento.getAlertaId().isBlank()) {
            evento.setAlertaId(UUID.randomUUID().toString());
        }
        alertaProducer.publicar(evento);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("mensaje", "Alerta enviada al tópico");
        response.put("alertaId", evento.getAlertaId());
        response.put("nivel", evento.getNivel() != null ? evento.getNivel() : "");
        return ResponseEntity.accepted().body(response);
    }
}
