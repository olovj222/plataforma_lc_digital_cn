package com.plataforma_lc.curso.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma_lc.curso.entities.Curso;
import com.plataforma_lc.curso.repository.CursoRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
    }
)
@AutoConfigureMockMvc
public class CursoRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Aislamos la base de datos para no borrar registros reales
    @MockitoBean
    private CursoRepository cursoRepository;

    private static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    // Magia pura: Le inyectamos la URL dinámica del servidor falso a tu Spring Boot
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("ms.estudiante.url", () -> mockBackEnd.url("/").toString());
    }

    @Test
    void delete_DeberiaEliminarCurso_SiNoTieneEstudiantesAsignados() throws Exception {
        // 1. ARRANGE: Preparamos la base de datos
        Curso cursoFalso = new Curso();
        cursoFalso.setId(1L);
        cursoFalso.setNombre("Matemáticas");
        cursoFalso.setCodigo(101);
        
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoFalso));

        // 2. ARRANGE: Preparamos la respuesta del Microservicio de Estudiantes
        // Simulamos que responde un JSON con un arreglo vacío [] (Cero estudiantes en el curso)
        mockBackEnd.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // 3. ACT: Hacemos la petición DELETE a nuestro propio endpoint
        mockMvc.perform(delete("/curso/1"))
                .andExpect(status().isOk());

        // 4. ASSERT: Verificamos que el repositorio realmente se haya llamado para borrar
        verify(cursoRepository, times(1)).delete(cursoFalso);

        // 5. ASSERT: Verificamos que el Controlador le haya preguntado al MS Estudiante
        RecordedRequest peticionInterceptada = mockBackEnd.takeRequest();
        assertEquals("GET", peticionInterceptada.getMethod());
        // Asumimos que esta será la ruta a la que tu WebClient llamará
        assertEquals("/estudiante/curso/1", peticionInterceptada.getPath());
    }
}
