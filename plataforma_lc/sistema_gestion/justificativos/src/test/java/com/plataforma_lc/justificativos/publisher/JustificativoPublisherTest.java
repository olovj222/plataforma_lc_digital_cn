package com.plataforma_lc.justificativos.publisher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.plataforma_lc.justificativos.entities.EstadoJustificativo;
import com.plataforma_lc.justificativos.entities.Justificativo;

/**
 * Pruebas unitarias del productor RabbitMQ: payload enviado y resiliencia ante fallos del broker.
 */
@ExtendWith(MockitoExtension.class)
class JustificativoPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private JustificativoPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new JustificativoPublisher(rabbitTemplate);
        ReflectionTestUtils.setField(publisher, "exchange", "asistencia.exchange");
        ReflectionTestUtils.setField(publisher, "routingKey", "asistencia.routingkey");
    }

    private Justificativo justificativo() {
        Justificativo j = new Justificativo();
        j.setId(7L);
        j.setEstudianteId(10L);
        j.setCursoId(1L);
        j.setFechaInasistencia(LocalDate.of(2026, 11, 10));
        j.setMotivo("Control medico");
        j.setEstado(EstadoJustificativo.APROBADO);
        return j;
    }

    @Test
    @SuppressWarnings("unchecked")
    void publicarEvento_enviaPayloadAlExchangeConLaRoutingKey() {
        publisher.publicarEventoJustificativo(justificativo());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
            eq("asistencia.exchange"), eq("asistencia.routingkey"), captor.capture());

        Map<String, Object> payload = (Map<String, Object>) captor.getValue();
        assertEquals(Long.valueOf(7L), payload.get("id"));
        assertEquals(Long.valueOf(10L), payload.get("estudianteId"));
        assertEquals(Long.valueOf(1L), payload.get("cursoId"));
        assertEquals("2026-11-10", payload.get("fecha"));
        assertEquals("APROBADO", payload.get("estado"));
        assertEquals("Control medico", payload.get("motivo"));
    }

    @Test
    void publicarEvento_siRabbitFalla_noPropagaLaExcepcion() {
        doThrow(new AmqpException("sin conexion con el broker"))
            .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        assertDoesNotThrow(() -> publisher.publicarEventoJustificativo(justificativo()));
    }
}