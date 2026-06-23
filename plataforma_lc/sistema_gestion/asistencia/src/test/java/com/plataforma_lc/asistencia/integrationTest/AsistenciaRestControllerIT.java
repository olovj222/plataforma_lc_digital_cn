/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.integrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Levanta el contexto completo de la aplicación
@AutoConfigureMockMvc // Permite simular peticiones HTTP reales al controlador
class AsistenciaRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void post_debeRetornar400_cuandoElJsonEsInvalido() throws Exception {
        // Aquí envías un JSON directo al endpoint simulando un cliente real
        String asistenciaJson = "{\"id_clase\": 1, \"id_estudiante\": 10, \"estado\": \"INVALIDO\"}";

        mockMvc.perform(post("/asistencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asistenciaJson))
                .andExpect(status().isBadRequest());
    }
    
}
