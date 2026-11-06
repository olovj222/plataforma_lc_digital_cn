package com.plataforma_lc.adminKafka.controller;

import com.plataforma_lc.adminKafka.dto.AlertaEventDTO;
import com.plataforma_lc.adminKafka.dto.AnaliticaStreamDTO;
import com.plataforma_lc.adminKafka.dto.AuditoriaEventDTO;
import com.plataforma_lc.adminKafka.producer.AlertaProducer;
import com.plataforma_lc.adminKafka.producer.AnaliticaProducer;
import com.plataforma_lc.adminKafka.producer.AuditoriaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Proporciona endpoints para enviar eventos de auditoría, analítica y alertas.
 * Útil para pruebas manuales y para que otros microservicios publiquen vía HTTP.
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
     */
    @PostMapping("/auditoria")
    public ResponseEntity<Map<String, String>> publicarAuditoria(@RequestBody AuditoriaEventDTO evento) {
        if (evento.getTimestamp() == null) {
            evento.setTimestamp(LocalDateTime.now());
        }
        auditoriaProducer.publicar(evento);
        return ResponseEntity.accepted()
                .body(Map.of("mensaje", "Evento de auditoría enviado al tópico",
                             "servicio", evento.getServicio(),
                             "accion", evento.getAccion()));
    }

    /**
     * Publica un registro de analítica de asistencia.
     * POST /api/kafka/publish/analitica
     */
    @PostMapping("/analitica")
    public ResponseEntity<Map<String, String>> publicarAnalitica(@RequestBody AnaliticaStreamDTO dto) {
        if (dto.getTimestamp() == null) {
            dto.setTimestamp(LocalDateTime.now());
        }
        if (dto.getFecha() == null) {
            dto.setFecha(LocalDate.now());
        }
        analiticaProducer.publicar(dto);
        return ResponseEntity.accepted()
                .body(Map.of("mensaje", "Stream de analítica enviado al tópico",
                             "cursoId", String.valueOf(dto.getCursoId()),
                             "estudianteId", String.valueOf(dto.getEstudianteId())));
    }

    /**
     * Publica un evento de alerta.
     * POST /api/kafka/publish/alerta
     */
    @PostMapping("/alerta")
    public ResponseEntity<Map<String, String>> publicarAlerta(@RequestBody AlertaEventDTO evento) {
        if (evento.getTimestamp() == null) {
            evento.setTimestamp(LocalDateTime.now());
        }
        if (evento.getAlertaId() == null || evento.getAlertaId().isBlank()) {
            evento.setAlertaId(UUID.randomUUID().toString());
        }
        alertaProducer.publicar(evento);
        return ResponseEntity.accepted()
                .body(Map.of("mensaje", "Alerta enviada al tópico",
                             "alertaId", evento.getAlertaId(),
                             "nivel", evento.getNivel()));
    }
}

