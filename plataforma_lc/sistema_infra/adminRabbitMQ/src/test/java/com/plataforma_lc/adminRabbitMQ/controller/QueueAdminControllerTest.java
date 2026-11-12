package com.plataforma_lc.adminRabbitMQ.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.plataforma_lc.adminRabbitMQ.dto.BindingRequestDTO;
import com.plataforma_lc.adminRabbitMQ.dto.ExchangeRequestDTO;
import com.plataforma_lc.adminRabbitMQ.dto.QueueRequestDTO;
import com.plataforma_lc.adminRabbitMQ.service.ListenerManagementService;
import com.plataforma_lc.adminRabbitMQ.service.RabbitAdminService;

@WebMvcTest(QueueAdminController.class)
class QueueAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RabbitAdminService adminService;

    @MockBean
    private ListenerManagementService listenerService;

    @Test
    void createQueue_valido_retorna201() throws Exception {
        String body = """
                {
                  "name": "cola.justificativos",
                  "durable": true,
                  "exclusive": false,
                  "autoDelete": false
                }
                """;

        mockMvc.perform(post("/api/admin/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().string("Cola 'cola.justificativos' creada correctamente."));

        verify(adminService).createQueue(any(QueueRequestDTO.class));
    }

    @Test
    void createQueue_sinNombre_retorna400() throws Exception {
        String body = """
                {
                  "name": ""
                }
                """;

        mockMvc.perform(post("/api/admin/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(adminService);
    }

    @Test
    void deleteQueue_existente_retorna200_yNoExistente_retorna404() throws Exception {
        when(adminService.deleteQueue("cola.ok")).thenReturn(true);
        when(adminService.deleteQueue("cola.no")).thenReturn(false);

        mockMvc.perform(delete("/api/admin/queues/cola.ok"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cola 'cola.ok' eliminada correctamente."));

        mockMvc.perform(delete("/api/admin/queues/cola.no"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("La cola 'cola.no' no fue encontrada."));
    }

    @Test
    void getQueueInfo_existente_retorna200_yNoExistente_retorna404() throws Exception {
        when(adminService.getQueueInfo("cola.ok")).thenReturn(new QueueInformation("cola.ok", 3, 1));
        when(adminService.getQueueInfo("cola.no")).thenReturn(null);

        mockMvc.perform(get("/api/admin/queues/cola.ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("cola.ok"))
                .andExpect(jsonPath("$.messageCount").value(3));

        mockMvc.perform(get("/api/admin/queues/cola.no"))
                .andExpect(status().isNotFound());
    }

    @Test
    void purgeQueue_retorna200ConCantidad() throws Exception {
        when(adminService.purgeQueue("cola.ok")).thenReturn(7);

        mockMvc.perform(post("/api/admin/queues/cola.ok/purge"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cola 'cola.ok' purgada. Mensajes eliminados: 7"));
    }

    @Test
    void createYDeleteExchange_retornanEstadosEsperados() throws Exception {
        String body = """
                {
                  "name": "ex.eventos",
                  "type": "topic"
                }
                """;

        mockMvc.perform(post("/api/admin/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().string("Exchange 'ex.eventos' (tipo: topic) creado correctamente."));

        verify(adminService).createExchange(any(ExchangeRequestDTO.class));

        when(adminService.deleteExchange("ex.eventos")).thenReturn(true);
        when(adminService.deleteExchange("ex.no")).thenReturn(false);

        mockMvc.perform(delete("/api/admin/exchanges/ex.eventos"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/admin/exchanges/ex.no"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createYRemoveBinding_retornanEstadosEsperados() throws Exception {
        String body = """
                {
                  "queueName": "cola.anotaciones",
                  "exchangeName": "ex.anotaciones",
                  "routingKey": "anotaciones.negativa"
                }
                """;

        mockMvc.perform(post("/api/admin/bindings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().string("Binding creado: cola.anotaciones <-> ex.anotaciones [anotaciones.negativa]"));

        mockMvc.perform(delete("/api/admin/bindings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Binding eliminado: cola.anotaciones <-> ex.anotaciones"));

        verify(adminService).createBinding(any(BindingRequestDTO.class));
        verify(adminService).removeBinding(any(BindingRequestDTO.class));
    }

    @Test
    void listenersEndpoints_retornan200() throws Exception {
        when(listenerService.getAllListenerIds()).thenReturn(List.of("listener-1"));
        when(listenerService.pauseListener("listener-1")).thenReturn("Listener 'listener-1' detenido correctamente.");
        when(listenerService.resumeListener("listener-1")).thenReturn("Listener 'listener-1' reanudado correctamente.");

        mockMvc.perform(get("/api/admin/listeners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("listener-1"));

        mockMvc.perform(post("/api/admin/listeners/listener-1/pause"))
                .andExpect(status().isOk())
                .andExpect(content().string("Listener 'listener-1' detenido correctamente."));

        mockMvc.perform(post("/api/admin/listeners/listener-1/resume"))
                .andExpect(status().isOk())
                .andExpect(content().string("Listener 'listener-1' reanudado correctamente."));
    }
}