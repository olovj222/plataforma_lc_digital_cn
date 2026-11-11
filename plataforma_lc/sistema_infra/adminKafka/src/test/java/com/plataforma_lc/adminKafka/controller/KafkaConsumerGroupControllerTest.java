package com.plataforma_lc.adminKafka.controller;

import com.plataforma_lc.adminKafka.dto.KafkaResponses.ConsumerGroupDetalleResponse;
import com.plataforma_lc.adminKafka.dto.KafkaResponses.ConsumerGroupItemResponse;
import com.plataforma_lc.adminKafka.dto.KafkaResponses.MensajeResponse;
import com.plataforma_lc.adminKafka.exception.GlobalExceptionHandler;
import com.plataforma_lc.adminKafka.service.KafkaAdminService;
import org.apache.kafka.common.errors.GroupIdNotFoundException;
import org.apache.kafka.common.errors.GroupNotEmptyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = KafkaConsumerGroupController.class)
@Import(GlobalExceptionHandler.class)
class KafkaConsumerGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KafkaAdminService adminService;

    @Test
    @DisplayName("GET /api/kafka/admin/consumer-groups retorna 200 y lista")
    void listarGrupos_retorna200() throws Exception {
        when(adminService.listarConsumerGroups()).thenReturn(List.of(
                new ConsumerGroupItemResponse("grupo-1", "STABLE", false)
        ));

        mockMvc.perform(get("/api/kafka/admin/consumer-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].groupId").value("grupo-1"))
                .andExpect(jsonPath("$[0].estado").value("STABLE"));
    }

    @Test
    @DisplayName("GET /api/kafka/admin/consumer-groups/{groupId} retorna 200 y detalle de lag")
    void describirGrupo_retorna200() throws Exception {
        ConsumerGroupDetalleResponse detalle = new ConsumerGroupDetalleResponse(
                "grupo-1", "STABLE", "broker-1", "range", Collections.emptyList(), Collections.emptyList(), 0L
        );
        when(adminService.describirConsumerGroup("grupo-1")).thenReturn(detalle);

        mockMvc.perform(get("/api/kafka/admin/consumer-groups/grupo-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value("grupo-1"))
                .andExpect(jsonPath("$.lagTotal").value(0));
    }

    @Test
    @DisplayName("GET /api/kafka/admin/consumer-groups/{groupId} inexistente retorna 404")
    void describirGrupo_inexistente_retorna404() throws Exception {
        when(adminService.describirConsumerGroup("grupo-no-existe"))
                .thenThrow(new GroupIdNotFoundException("Grupo no existe"));

        mockMvc.perform(get("/api/kafka/admin/consumer-groups/grupo-no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/kafka/admin/consumer-groups/{groupId} de grupo no vacío retorna 409")
    void eliminarGrupo_noVacio_retorna409() throws Exception {
        when(adminService.eliminarConsumerGroup("grupo-activo"))
                .thenThrow(new GroupNotEmptyException("El grupo no está vacío"));

        mockMvc.perform(delete("/api/kafka/admin/consumer-groups/grupo-activo"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("DELETE /api/kafka/admin/consumer-groups/{groupId} de grupo vacío retorna 200")
    void eliminarGrupo_vacio_retorna200() throws Exception {
        when(adminService.eliminarConsumerGroup("grupo-vacio"))
                .thenReturn(new MensajeResponse("Consumer group eliminado exitosamente", "grupo-vacio"));

        mockMvc.perform(delete("/api/kafka/admin/consumer-groups/grupo-vacio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Consumer group eliminado exitosamente"));
    }
}
