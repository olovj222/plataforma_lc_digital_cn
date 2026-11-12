package com.plataforma_lc.adminRabbitMQ.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;

@ExtendWith(MockitoExtension.class)
class ListenerManagementServiceTest {

    @Mock
    private RabbitListenerEndpointRegistry registry;

    @InjectMocks
    private ListenerManagementService service;

    @Test
    void pauseListener_cuandoNoExiste_retornaMensajeNoEncontrado() {
        when(registry.getListenerContainer("l1")).thenReturn(null);

        assertEquals("Listener 'l1' no encontrado.", service.pauseListener("l1"));
    }

    @Test
    void pauseListener_cuandoYaEstabaDetenido_noLlamaStop() {
        MessageListenerContainer container = mock(MessageListenerContainer.class);
        when(container.isRunning()).thenReturn(false);
        when(registry.getListenerContainer("l1")).thenReturn(container);

        assertEquals("Listener 'l1' ya estaba detenido.", service.pauseListener("l1"));
        verify(container, never()).stop();
    }

    @Test
    void pauseListener_cuandoEstaActivo_detieneContainer() {
        MessageListenerContainer container = mock(MessageListenerContainer.class);
        when(container.isRunning()).thenReturn(true);
        when(registry.getListenerContainer("l1")).thenReturn(container);

        assertEquals("Listener 'l1' detenido correctamente.", service.pauseListener("l1"));
        verify(container).stop();
    }

    @Test
    void resumeListener_cuandoNoExiste_retornaMensajeNoEncontrado() {
        when(registry.getListenerContainer("l1")).thenReturn(null);

        assertEquals("Listener 'l1' no encontrado.", service.resumeListener("l1"));
    }

    @Test
    void resumeListener_cuandoYaEstabaActivo_noLlamaStart() {
        MessageListenerContainer container = mock(MessageListenerContainer.class);
        when(container.isRunning()).thenReturn(true);
        when(registry.getListenerContainer("l1")).thenReturn(container);

        assertEquals("Listener 'l1' ya estaba activo.", service.resumeListener("l1"));
        verify(container, never()).start();
    }

    @Test
    void resumeListener_cuandoEstaDetenido_iniciaContainer() {
        MessageListenerContainer container = mock(MessageListenerContainer.class);
        when(container.isRunning()).thenReturn(false);
        when(registry.getListenerContainer("l1")).thenReturn(container);

        assertEquals("Listener 'l1' reanudado correctamente.", service.resumeListener("l1"));
        verify(container).start();
    }

    @Test
    void getAllListenerIds_retornaListaDeIdsRegistrados() {
        when(registry.getListenerContainerIds()).thenReturn(Set.of("listener-a", "listener-b"));

        List<String> ids = service.getAllListenerIds();

        assertEquals(2, ids.size());
        assertTrue(ids.containsAll(List.of("listener-a", "listener-b")));
    }
}