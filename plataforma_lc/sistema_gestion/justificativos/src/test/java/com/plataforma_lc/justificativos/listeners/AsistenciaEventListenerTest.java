package com.plataforma_lc.justificativos.listeners;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.test.util.ReflectionTestUtils;

import com.plataforma_lc.justificativos.dto.AsistenciaDTO;

/**
 * Pruebas unitarias del consumidor RabbitMQ: procesamiento normal y envio a la DLQ
 * cuando falla (simulacion de fallos habilitada).
 */
class AsistenciaEventListenerTest {

    private AsistenciaEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new AsistenciaEventListener();
    }

    private AsistenciaDTO evento(String motivo) {
        AsistenciaDTO dto = new AsistenciaDTO();
        dto.setId(1L);
        dto.setId_clase(4L);
        dto.setId_estudiante(11L);
        dto.setFecha("2026-11-10");
        dto.setEstado("ABSENT");
        dto.setMotivo(motivo);
        return dto;
    }

    @Test
    void procesarEvento_normal_noLanzaExcepcion() {
        ReflectionTestUtils.setField(listener, "simulacionFallosHabilitada", true);

        assertDoesNotThrow(() -> listener.procesarEventoAsistencia(evento("Control medico")));
    }

    @Test
    void procesarEvento_conMarcadorYSimulacionHabilitada_vaALaDlqSinReencolar() {
        ReflectionTestUtils.setField(listener, "simulacionFallosHabilitada", true);

        assertThrows(AmqpRejectAndDontRequeueException.class,
            () -> listener.procesarEventoAsistencia(evento("SIMULAR_FALLO_JUSTIFICATIVOS")));
    }

    @Test
    void procesarEvento_conMarcadorPeroSimulacionDeshabilitada_noFalla() {
        ReflectionTestUtils.setField(listener, "simulacionFallosHabilitada", false);

        assertDoesNotThrow(
            () -> listener.procesarEventoAsistencia(evento("SIMULAR_FALLO_JUSTIFICATIVOS")));
    }

    @Test
    void procesarEvento_sinMotivo_noFallaAunqueLaSimulacionEsteHabilitada() {
        ReflectionTestUtils.setField(listener, "simulacionFallosHabilitada", true);

        assertDoesNotThrow(() -> listener.procesarEventoAsistencia(evento(null)));
    }
}