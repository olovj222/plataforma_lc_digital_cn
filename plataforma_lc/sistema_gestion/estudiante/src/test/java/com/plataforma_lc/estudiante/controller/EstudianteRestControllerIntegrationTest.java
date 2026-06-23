package com.plataforma_lc.estudiante.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma_lc.estudiante.entities.Estudiante;
import com.plataforma_lc.estudiante.entities.EstudianteCurso;
import com.plataforma_lc.estudiante.repository.EstudianteRepository;
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
import org.springframework.data.util.MethodInvocationRecorder.Recorded;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.server.MockWebSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.ArrayList;
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
public class EstudianteRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Aislamos la base de datos para no tocar registros reales
    @MockitoBean
    private EstudianteRepository estudianteRepository;

    private static MockWebServer mockBackEnd;

    private Estudiante estudiante;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    // Inyectamos dinámicamente la URL del MockWebServer al MS Curso
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("ms.curso.url", () -> mockBackEnd.url("/").toString());
    }

    @BeforeEach
    void init() {
        EstudianteCurso curso = new EstudianteCurso();
        curso.setCursoId(1L);

        estudiante = Estudiante.builder()
            .nombre("Juan")
            .apPaterno("Pérez")
            .apMaterno("López")
            .direccion("Av. Principal 123")
            .telefono("912345678")
            .cursos(new ArrayList<>(List.of(curso)))
            .asistencias(new ArrayList<>())
            .evaluaciones(new ArrayList<>())
            .build();

        // Seteamos el ID manualmente para simular que fue guardado
        // Usamos reflexión ya que es un long primitivo
        try {
            var field = Estudiante.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(estudiante, 1L);
        } catch (Exception ignored) {}
    }

    // -------------------------------------------------------
    // POST /estudiante — crear
    // Regla de negocio: debe tener al menos un curso asociado
    // -------------------------------------------------------

    @Test
    void post_estudianteValido_guardaEnRepoYRetorna201() throws Exception {
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(estudiante);

        mockMvc.perform(post("/estudiante")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(estudiante)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Juan"))
            .andExpect(jsonPath("$.apPaterno").value("Pérez"));

        verify(estudianteRepository, times(1)).save(any(Estudiante.class));
    }

    @Test
    void post_estudianteSinNombre_retorna400SinGuardar() throws Exception {
        Estudiante sinNombre = Estudiante.builder()
            .apPaterno("Pérez")
            .cursos(new ArrayList<>(List.of(new EstudianteCurso())))
            .asistencias(new ArrayList<>())
            .evaluaciones(new ArrayList<>())
            .build();

        mockMvc.perform(post("/estudiante")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sinNombre)))
            .andExpect(status().isBadRequest());

        verify(estudianteRepository, never()).save(any(Estudiante.class));
    }

    @Test
    void post_estudianteSinCursos_retorna400PorReglaNegocio() throws Exception {
        Estudiante sinCursos = Estudiante.builder()
            .nombre("Juan")
            .apPaterno("Pérez")
            .cursos(new ArrayList<>())
            .asistencias(new ArrayList<>())
            .evaluaciones(new ArrayList<>())
            .build();

        mockMvc.perform(post("/estudiante")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sinCursos)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensaje").value(containsString("curso")));

        verify(estudianteRepository, never()).save(any(Estudiante.class));
    }

    // -------------------------------------------------------
    // GET /estudiante/{id}
    // Verifica que consulta al MS Curso para rellenar el nombre
    // -------------------------------------------------------

    @Test
    void get_estudianteExistente_retorna200YConsultaMSCurso() throws Exception {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));

        // Simulamos respuesta del MS Curso
        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"id\": 1, \"nombre\": \"Matemáticas\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockMvc.perform(get("/estudiante/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Juan"));

        // Verificamos que se consultó el MS Curso
        RecordedRequest peticion = mockBackEnd.takeRequest();
        assertEquals("GET", peticion.getMethod());
        assertEquals("/curso/1", peticion.getPath());

        verify(estudianteRepository, times(1)).findById(1L);
    }

    @Test
    void get_estudianteNoExiste_retorna404() throws Exception {
        when(estudianteRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/estudiante/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensaje").value(containsString("99")));
    }

    @Test
    void get_MSCursoApagado_retorna200ConNombreFallback() throws Exception {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));

        // Simulamos que el MS Curso falla — el CircuitBreaker activará el fallback
        mockBackEnd.enqueue(new MockResponse().setResponseCode(500));

        mockMvc.perform(get("/estudiante/1"))
            .andExpect(status().isOk())
            // El fallback pone "MS Cursos no disponible"
            .andExpect(jsonPath("$.cursos[0].cursoName").value("MS Cursos no disponible"));
    }

    // -------------------------------------------------------
    // GET /estudiante — listar todos
    // -------------------------------------------------------

    @Test
    void listarTodos_conEstudiantes_retornaListaYConsultaMSCurso() throws Exception {
        when(estudianteRepository.findAll()).thenReturn(List.of(estudiante));

        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"id\": 1, \"nombre\": \"Matemáticas\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        mockMvc.perform(get("/estudiante"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").value("Juan"));

        verify(estudianteRepository, times(1)).findAll();
    }

    @Test
    void listarTodos_sinEstudiantes_retornaListaVacia() throws Exception {
        when(estudianteRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/estudiante"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    // -------------------------------------------------------
    // PUT /estudiante/{id}
    // -------------------------------------------------------

    @Test
    void put_estudianteExistente_actualizaYRetorna200() throws Exception {
        Estudiante actualizado = Estudiante.builder()
            .nombre("Pedro")
            .apPaterno("González")
            .apMaterno("Rojas")
            .direccion("Calle Nueva 456")
            .telefono("987654321")
            .cursos(new ArrayList<>())
            .asistencias(new ArrayList<>())
            .evaluaciones(new ArrayList<>())
            .build();

        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(actualizado);

        mockMvc.perform(put("/estudiante/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Pedro"));

        verify(estudianteRepository, times(1)).findById(1L);
        verify(estudianteRepository, times(1)).save(any(Estudiante.class));
    }

    @Test
    void put_idInexistente_retorna404SinGuardar() throws Exception {
        when(estudianteRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/estudiante/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(estudiante)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensaje").value(containsString("99")));

        verify(estudianteRepository, never()).save(any(Estudiante.class));
    }

    // -------------------------------------------------------
    // DELETE /estudiante/{id}
    // -------------------------------------------------------

    @Test
    void delete_estudianteExistente_eliminaDeRepoYRetorna200() throws Exception {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        doNothing().when(estudianteRepository).deleteById(1L);

        mockMvc.perform(delete("/estudiante/1"))
            .andExpect(status().isOk());

        verify(estudianteRepository, times(1)).findById(1L);
        verify(estudianteRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_idInexistente_retorna404SinEliminar() throws Exception {
        when(estudianteRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/estudiante/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensaje").value(containsString("99")));

        verify(estudianteRepository, never()).deleteById(anyLong());
    }
}