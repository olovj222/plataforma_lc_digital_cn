package com.plataforma_lc.adminKafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma_lc.adminKafka.dto.KafkaResponses.*;
import com.plataforma_lc.adminKafka.dto.TopicRequestDTO;
import com.plataforma_lc.adminKafka.exception.GlobalExceptionHandler;
import com.plataforma_lc.adminKafka.service.KafkaAdminService;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = KafkaAdminController.class)
@Import(GlobalExceptionHandler.class)
class KafkaAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KafkaAdminService adminService;

    @Test
    @DisplayName("GET /api/kafka/admin/topics retorna 200 y lista de tópicos")
    void listarTopicos_retorna200() throws Exception {
        when(adminService.listarTopicos()).thenReturn(new ListadoTopicosResponse(2, List.of("topic.a", "topic.b")));

        mockMvc.perform(get("/api/kafka/admin/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTopicos").value(2))
                .andExpect(jsonPath("$.topicos[0]").value("topic.a"));
    }

    @Test
    @DisplayName("POST /api/kafka/admin/topics crea tópico y retorna 201 Created")
    void crearTopico_retorna201() throws Exception {
        TopicRequestDTO dto = TopicRequestDTO.builder()
                .nombre("nuevo.topico")
                .particiones(3)
                .factorReplicacion(3)
                .retentionMs(86400000L)
                .cleanupPolicy("delete")
                .build();

        when(adminService.crearTopico(any())).thenReturn(new MensajeResponse("Tópico creado exitosamente", "nuevo.topico"));

        mockMvc.perform(post("/api/kafka/admin/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Tópico creado exitosamente"))
                .andExpect(jsonPath("$.recurso").value("nuevo.topico"));
    }

    @Test
    @DisplayName("POST /api/kafka/admin/topics con tópico duplicado retorna 409 Conflict")
    void crearTopico_duplicado_retorna409() throws Exception {
        TopicRequestDTO dto = TopicRequestDTO.builder()
                .nombre("topico.existente")
                .build();

        when(adminService.crearTopico(any())).thenThrow(new TopicExistsException("El tópico ya existe"));

        mockMvc.perform(post("/api/kafka/admin/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("POST /api/kafka/admin/topics con payload inválido retorna 400 Bad Request")
    void crearTopico_invalido_retorna400() throws Exception {
        TopicRequestDTO dto = TopicRequestDTO.builder()
                .nombre("") // Blanco
                .particiones(0) // < 1
                .build();

        mockMvc.perform(post("/api/kafka/admin/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detallesValidacion").exists());
    }

    @Test
    @DisplayName("GET /api/kafka/admin/topics/{nombre} existente retorna 200 y detalles")
    void inspeccionarTopico_retorna200() throws Exception {
        TopicoDetalleResponse detalle = new TopicoDetalleResponse(
                "test.topic", "id-123", false, 1,
                List.of(new ParticionDetalleResponse(0, "broker-1", List.of("broker-1"), List.of("broker-1"), 1)),
                Map.of("retention.ms", "604800000")
        );

        when(adminService.inspeccionarTopico("test.topic")).thenReturn(detalle);

        mockMvc.perform(get("/api/kafka/admin/topics/test.topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("test.topic"))
                .andExpect(jsonPath("$.totalParticiones").value(1));
    }

    @Test
    @DisplayName("GET /api/kafka/admin/topics/{nombre} inexistente retorna 404 Not Found")
    void inspeccionarTopico_inexistente_retorna404() throws Exception {
        when(adminService.inspeccionarTopico("no.existe"))
                .thenThrow(new UnknownTopicOrPartitionException("Tópico no existe"));

        mockMvc.perform(get("/api/kafka/admin/topics/no.existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/kafka/admin/topics/{nombre}/config modifica config y retorna 200")
    void modificarConfig_retorna200() throws Exception {
        Map<String, String> configs = Map.of("retention.ms", "86400000");
        when(adminService.modificarConfiguracion(eq("test.topic"), any()))
                .thenReturn(new MensajeResponse("Configuración del tópico actualizada exitosamente", "test.topic"));

        mockMvc.perform(put("/api/kafka/admin/topics/test.topic/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configs)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Configuración del tópico actualizada exitosamente"));
    }

    @Test
    @DisplayName("PUT /api/kafka/admin/topics/{nombre}/particiones aumenta particiones y retorna 200")
    void aumentarParticiones_retorna200() throws Exception {
        Map<String, Integer> body = Map.of("particiones", 6);
        when(adminService.aumentarParticiones("test.topic", 6))
                .thenReturn(new MensajeResponse("Particiones aumentadas a 6 exitosamente", "test.topic"));

        mockMvc.perform(put("/api/kafka/admin/topics/test.topic/particiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Particiones aumentadas a 6 exitosamente"));
    }

    @Test
    @DisplayName("DELETE /api/kafka/admin/topics/{nombre} elimina tópico y retorna 200")
    void eliminarTopico_retorna200() throws Exception {
        when(adminService.eliminarTopico("test.topic", false))
                .thenReturn(new MensajeResponse("Tópico eliminado exitosamente", "test.topic"));

        mockMvc.perform(delete("/api/kafka/admin/topics/test.topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tópico eliminado exitosamente"));
    }

    @Test
    @DisplayName("GET /api/kafka/admin/cluster retorna 200 e información de los brokers")
    void obtenerInfoCluster_retorna200() throws Exception {
        ClusterInfoResponse clusterInfo = new ClusterInfoResponse(
                "cluster-xyz", "broker-1 (kafka-1:29092)", 3,
                List.of(
                        new BrokerInfoResponse(1, "kafka-1", 29092, null),
                        new BrokerInfoResponse(2, "kafka-2", 29092, null),
                        new BrokerInfoResponse(3, "kafka-3", 29092, null)
                )
        );
        when(adminService.obtenerInfoCluster()).thenReturn(clusterInfo);

        mockMvc.perform(get("/api/kafka/admin/cluster"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clusterId").value("cluster-xyz"))
                .andExpect(jsonPath("$.totalBrokers").value(3))
                .andExpect(jsonPath("$.brokers[0].host").value("kafka-1"));
    }
}
