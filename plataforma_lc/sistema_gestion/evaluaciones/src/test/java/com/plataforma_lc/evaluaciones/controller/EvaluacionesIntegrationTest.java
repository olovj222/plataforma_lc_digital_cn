package com.plataforma_lc.evaluaciones.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma_lc.evaluaciones.entities.Evaluaciones;
import com.plataforma_lc.evaluaciones.repository.evaluacionesRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
    }
)
@AutoConfigureMockMvc
public class EvaluacionesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Aislamos la base de datos para no tocar registros reales
    @MockitoBean
    private evaluacionesRepository repo;

    private static MockWebServer mockBackEnd;

    private Evaluaciones evaluacion;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    // Inyectamos dinámicamente las URLs de los microservicios al MockWebServer
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("ms.curso.url", () -> mockBackEnd.url("/").toString());
        registry.add("ms.estudiante.url", () -> mockBackEnd.url("/").toString());
    }

    @BeforeEach
    void init() {
        evaluacion = new Evaluaciones();
        evaluacion.setId(1L);
        evaluacion.setNombre("Control 1");
        evaluacion.setCursoId(10L);
        evaluacion.setEstudianteId(5L);
        evaluacion.setCalificacion(0);
    }

    // -------------------------------------------------------
    // POST /evaluaciones — crear
    // Verifica que el controller llama al service que llama al repo
    // y luego consulta los microservicios para rellenar nombres
    // -------------------------------------------------------

    @Test
    void crear_evaluacionValida_guardaEnRepoYConsultaMicroservicios() throws Exception {
        when(repo.save(any(Evaluaciones.class))).thenReturn(evaluacion);

        // Simulamos respuesta del MS Curso
        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"nombre\": \"Matemáticas\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Simulamos respuesta del MS Estudiante
        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"nombre\": \"Juan\", \"apPaterno\": \"Pérez\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockMvc.perform(post("/evaluaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(evaluacion)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Control 1"))
            .andExpect(jsonPath("$.cursoId").value(10));

        verify(repo, times(1)).save(any(Evaluaciones.class));
    }

    @Test
    void crear_evaluacionSinNombre_retorna400SinLlamarAlRepo() throws Exception {
        Evaluaciones sinNombre = new Evaluaciones();
        sinNombre.setCursoId(10L);
        sinNombre.setEstudianteId(5L);

        mockMvc.perform(post("/evaluaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sinNombre)))
            .andExpect(status().isBadRequest());

        verify(repo, never()).save(any(Evaluaciones.class));
    }

    // -------------------------------------------------------
    // PUT /evaluaciones/{id} — actualizar
    // -------------------------------------------------------

    @Test
    void actualizar_evaluacionExistente_retorna200YConsultaMicroservicios() throws Exception {
        Evaluaciones actualizada = new Evaluaciones();
        actualizada.setId(1L);
        actualizada.setNombre("Control Actualizado");
        actualizada.setCursoId(20L);
        actualizada.setEstudianteId(5L);

        when(repo.findById(1L)).thenReturn(Optional.of(evaluacion));
        when(repo.save(any(Evaluaciones.class))).thenReturn(actualizada);

        // Simulamos respuesta del MS Curso
        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"nombre\": \"Historia\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Simulamos respuesta del MS Estudiante
        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"nombre\": \"Juan\", \"apPaterno\": \"Pérez\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockMvc.perform(put("/evaluaciones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizada)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Control Actualizado"));

        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).save(any(Evaluaciones.class));
    }

    @Test
    void actualizar_idInexistente_retorna404SinGuardar() throws Exception {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/evaluaciones/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(evaluacion)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensaje").value(containsString("99")));

        verify(repo, never()).save(any(Evaluaciones.class));
    }

    // -------------------------------------------------------
    // POST /evaluaciones/{id}/nota — registrarNota
    // Regla de negocio clave: nota entre 1 y 7
    // -------------------------------------------------------

    @Test
    void ponerNota_notaValida_persisteYConsultaMicroservicios() throws Exception {
        evaluacion.setCalificacion(6);
        when(repo.findById(1L)).thenReturn(Optional.of(evaluacion));
        when(repo.save(any(Evaluaciones.class))).thenReturn(evaluacion);

        // Simulamos respuesta del MS Curso
        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"nombre\": \"Matemáticas\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Simulamos respuesta del MS Estudiante
        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"nombre\": \"Juan\", \"apPaterno\": \"Pérez\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockMvc.perform(post("/evaluaciones/1/nota")
                .param("nota", "6"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.calificacion").value(6));

        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).save(any(Evaluaciones.class));
    }

    @Test
    void ponerNota_notaCero_retorna400PorReglaNegocioSinConsultarRepo() throws Exception {
        // La validación ocurre ANTES de consultar el repo
        mockMvc.perform(post("/evaluaciones/1/nota")
                .param("nota", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensaje").value(containsString("1 y 7")));

        verify(repo, never()).findById(anyLong());
    }

    @Test
    void ponerNota_notaOcho_retorna400PorReglaNegocioSinConsultarRepo() throws Exception {
        mockMvc.perform(post("/evaluaciones/1/nota")
                .param("nota", "8"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensaje").value(containsString("1 y 7")));

        verify(repo, never()).findById(anyLong());
    }

    @Test
    void ponerNota_evaluacionNoExiste_retorna404() throws Exception {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/evaluaciones/99/nota")
                .param("nota", "5"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensaje").value(containsString("99")));
    }

    // -------------------------------------------------------
    // GET /evaluaciones/curso/{cursoId} — porCurso
    // Verifica que filtra por curso y consulta microservicios
    // -------------------------------------------------------

    @Test
    void porCurso_cursoConEvaluaciones_retornaListaYConsultaMicroservicios() throws Exception {
        when(repo.findByCursoId(10L)).thenReturn(List.of(evaluacion));

        // Simulamos respuesta del MS Curso y Estudiante para la evaluación
        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"nombre\": \"Matemáticas\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"nombre\": \"Juan\", \"apPaterno\": \"Pérez\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockMvc.perform(get("/evaluaciones/curso/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].cursoId").value(10))
            .andExpect(jsonPath("$[0].nombre").value("Control 1"));

        verify(repo, times(1)).findByCursoId(10L);
    }

    @Test
    void porCurso_cursoSinEvaluaciones_retornaListaVacia() throws Exception {
        when(repo.findByCursoId(99L)).thenReturn(List.of());

        mockMvc.perform(get("/evaluaciones/curso/99"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    // -------------------------------------------------------
    // GET /evaluaciones/estudiante/{id} — porEstudiante
    // -------------------------------------------------------

    @Test
    void porEstudiante_estudianteConEvaluaciones_retornaListaYConsultaMicroservicios() throws Exception {
        when(repo.findByEstudianteId(5L)).thenReturn(List.of(evaluacion));

        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"nombre\": \"Matemáticas\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"nombre\": \"Juan\", \"apPaterno\": \"Pérez\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockMvc.perform(get("/evaluaciones/estudiante/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].estudianteId").value(5));

        verify(repo, times(1)).findByEstudianteId(5L);
    }

    @Test
    void porEstudiante_estudianteSinEvaluaciones_retornaListaVacia() throws Exception {
        when(repo.findByEstudianteId(99L)).thenReturn(List.of());

        mockMvc.perform(get("/evaluaciones/estudiante/99"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    // -------------------------------------------------------
    // DELETE /evaluaciones/{id} — eliminar
    // -------------------------------------------------------

    @Test
    void eliminar_evaluacionExistente_eliminaDeRepoYRetorna200() throws Exception {
        when(repo.existsById(1L)).thenReturn(true);
        doNothing().when(repo).deleteById(1L);

        mockMvc.perform(delete("/evaluaciones/1"))
            .andExpect(status().isOk())
            .andExpect(content().string("Evaluación eliminada correctamente"));

        verify(repo, times(1)).existsById(1L);
        verify(repo, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_idInexistente_retorna404SinEliminar() throws Exception {
        when(repo.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/evaluaciones/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensaje").value(containsString("99")));

        verify(repo, never()).deleteById(anyLong());
    }
}