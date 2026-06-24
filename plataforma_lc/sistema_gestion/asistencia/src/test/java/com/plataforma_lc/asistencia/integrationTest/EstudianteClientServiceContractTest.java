/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.integrationTest;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.plataforma_lc.asistencia.entities.EstudianteResponse;
import com.plataforma_lc.asistencia.service.EstudianteClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import au.com.dius.pact.core.model.PactSpecVersion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "MicroservicioEstudiantes", pactVersion = PactSpecVersion.V3) 
class EstudianteClientServiceContractTest {

    private EstudianteClientService estudianteClientService;

    @BeforeEach
    void setUp(MockServer mockServer) {
        estudianteClientService = new EstudianteClientService();
        
        ReflectionTestUtils.setField(estudianteClientService, "webClientBuilder", WebClient.builder());
        ReflectionTestUtils.setField(estudianteClientService, "estudianteUrl", mockServer.getUrl());
    }

    @Pact(consumer = "MicroservicioAsistencia")
    public RequestResponsePact contratoEstudianteExiste(PactDslWithProvider builder) {
        return builder
            .given("El estudiante con ID 10 existe")
            .uponReceiving("Una peticion para obtener los datos del estudiante 10")
                .path("/estudiante/10")
                .method("GET")
            .willRespondWith()
                .status(200)
                .matchHeader("Content-Type", "application/json")
                .body("{\"nombre\": \"Estudiante de Prueba\"}") 
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "contratoEstudianteExiste")
    void testObtenerEstudiante_CumpleContrato() {
        EstudianteResponse response = estudianteClientService.obtenerEstudiante(10L);
        assertNotNull(response);
        assertEquals("Estudiante de Prueba", response.getNombre());
    }
}