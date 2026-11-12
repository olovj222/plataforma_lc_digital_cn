package com.plataforma_lc.anotaciones.publisher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.plataforma_lc.anotaciones.entities.Anotacion;
import com.plataforma_lc.anotaciones.entities.TipoAnotacion;

@ExtendWith(MockitoExtension.class)
class AnotacionPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AnotacionPublisher publisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(publisher, "exchange", "anotaciones.exchange");
        ReflectionTestUtils.setField(publisher, "routingKey", "anotaciones.negativa");
    }

    private Anotacion crearAnotacion(TipoAnotacion tipo) {
        Anotacion a = new Anotacion();
        a.setId(7L);
        a.setEstudianteId(10L);
        a.setCursoId(1L);
        a.setTipo(tipo);
        a.setDescripcion("Observacion de conducta");
        a.setFecha(LocalDate.of(2026, 11, 12));
        a.setAutorId("profe-1");
        return a;
    }

    @Test
    @SuppressWarnings("rawtypes")
    void publicarAnotacionNegativa_conTipoNegativa_enviaPayloadARabbitMQ() {
        Anotacion negativa = crearAnotacion(TipoAnotacion.NEGATIVA);
        final Map[] captured = new Map[1];

        doAnswer(invocation -> {
            captured[0] = invocation.getArgument(2);
            return null;
        }).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        publisher.publicarAnotacionNegativa(negativa);

        verify(rabbitTemplate).convertAndSend(
                eq("anotaciones.exchange"),
                eq("anotaciones.negativa"),
                any(Object.class)
        );

        Map payload = captured[0];
        assertNotNull(payload);
        assertEquals(7L, payload.get("id"));
        assertEquals(10L, payload.get("estudianteId"));
        assertEquals(1L, payload.get("cursoId"));
        assertEquals("NEGATIVA", payload.get("tipo"));
        assertEquals("Observacion de conducta", payload.get("descripcion"));
        assertEquals("2026-11-12", payload.get("fecha"));
        assertEquals("profe-1", payload.get("autorId"));
    }

    @Test
    void publicarAnotacionNegativa_conTipoPositiva_noPublicaEnRabbitMQ() {
        Anotacion positiva = crearAnotacion(TipoAnotacion.POSITIVA);

        publisher.publicarAnotacionNegativa(positiva);

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void publicarAnotacionNegativa_siRabbitFalla_noPropagaLaExcepcion() {
        Anotacion negativa = crearAnotacion(TipoAnotacion.NEGATIVA);
        doThrow(new AmqpException("sin conexion con el broker"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        assertDoesNotThrow(() -> publisher.publicarAnotacionNegativa(negativa));
    }
}