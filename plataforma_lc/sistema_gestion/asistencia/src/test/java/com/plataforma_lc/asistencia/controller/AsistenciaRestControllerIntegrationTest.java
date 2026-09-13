/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma_lc.asistencia.entities.Asistencia;
import com.plataforma_lc.asistencia.entities.Clase;
import com.plataforma_lc.asistencia.entities.EstudianteResponse;
import com.plataforma_lc.asistencia.repository.AsistenciaRepository;
import com.plataforma_lc.asistencia.repository.ClaseRepository;
import com.plataforma_lc.asistencia.service.EstudianteClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class AsistenciaRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Aislamos la base de datos
    @MockitoBean
    private AsistenciaRepository asistenciaRepository;

    @MockitoBean
    private ClaseRepository claseRepository;

    // Aislamos el cliente del microservicio para no hacer llamadas reales
    @MockitoBean
    private EstudianteClientService estudianteClientService;

    private Asistencia asistenciaInput;
    private Clase claseValida;
    private EstudianteResponse estudianteValido;

    @BeforeEach
    void setUp() {
        // Configuramos la data base para que nuestras pruebas funcionen
        claseValida = new Clase();
        claseValida.setId(10L);
        // Puedes agregar más atributos a claseValida si tu entidad los requiere

        estudianteValido = new EstudianteResponse();
        estudianteValido.setId(5L);
        estudianteValido.setNombre("Estudiante O'Higgins"); // Estudiante simulado válido

        asistenciaInput = new Asistencia();
        asistenciaInput.setId_clase(10L);
        asistenciaInput.setId_estudiante(5L);
        asistenciaInput.setFecha(new Date()); // Ajusta según el tipo de dato de tu entidad
        asistenciaInput.setEstado("PRESENT");
    }

    // -------------------------------------------------------
    // POST /asistencia — Casos de Éxito y Reglas de Negocio
    // -------------------------------------------------------

    @Test
    void post_asistenciaValida_guardaYRetorna200() throws Exception {
        // 1. Simulamos que la clase existe
        when(claseRepository.findById(10L)).thenReturn(Optional.of(claseValida));
        // 2. Simulamos que NO hay duplicados
        when(asistenciaRepository.existeRegistroDuplicado(anyLong(), anyLong(), any())).thenReturn(false);
        // 3. Simulamos que el MS Estudiante responde bien
        when(estudianteClientService.obtenerEstudiante(5L)).thenReturn(estudianteValido);
        // 4. Simulamos el guardado
        when(asistenciaRepository.save(any(Asistencia.class))).thenReturn(asistenciaInput);

        mockMvc.perform(post("/asistencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(asistenciaInput)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("PRESENT"));

        verify(asistenciaRepository, times(1)).save(any(Asistencia.class));
    }

    @Test
    void post_claseNoExiste_retorna400() throws Exception {
        // Simulamos que la clase NO existe en la base de datos
        when(claseRepository.findById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/asistencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(asistenciaInput)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("clase especificada no existe")));

        // Verificamos que el proceso se detuvo antes de guardar
        verify(asistenciaRepository, never()).save(any());
    }

    @Test
    void post_estadoInvalido_retorna400() throws Exception {
        when(claseRepository.findById(10L)).thenReturn(Optional.of(claseValida));
        
        asistenciaInput.setEstado("TARDE"); // Estado no permitido

        mockMvc.perform(post("/asistencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(asistenciaInput)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("PRESENT, ABSENT o JUSTIFIED")));
    }

    @Test
    void post_asistenciaDuplicada_retorna400() throws Exception {
        when(claseRepository.findById(10L)).thenReturn(Optional.of(claseValida));
        // Simulamos que la query de duplicados devuelve true
        when(asistenciaRepository.existeRegistroDuplicado(anyLong(), anyLong(), any())).thenReturn(true);

        mockMvc.perform(post("/asistencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(asistenciaInput)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("ya tiene asistencia registrada")));
    }

    // -------------------------------------------------------
    // POST /asistencia — Interacción con Microservicio Externo
    // -------------------------------------------------------

    @Test
    void post_estudianteNoExisteEnMS_retorna400() throws Exception {
        when(claseRepository.findById(10L)).thenReturn(Optional.of(claseValida));
        when(asistenciaRepository.existeRegistroDuplicado(anyLong(), anyLong(), any())).thenReturn(false);

        // Simulamos la respuesta de error de negocio de tu cliente Feign/RestTemplate
        EstudianteResponse estudianteNoEncontrado = new EstudianteResponse();
        estudianteNoEncontrado.setNombre("Estudiante no encontrado");
        when(estudianteClientService.obtenerEstudiante(5L)).thenReturn(estudianteNoEncontrado);

        mockMvc.perform(post("/asistencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(asistenciaInput)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("no existe")));
            
        verify(asistenciaRepository, never()).save(any());
    }

    @Test
    void post_msEstudianteCaido_retorna503() throws Exception {
        when(claseRepository.findById(10L)).thenReturn(Optional.of(claseValida));
        when(asistenciaRepository.existeRegistroDuplicado(anyLong(), anyLong(), any())).thenReturn(false);

        // Simulamos que saltó el Circuit Breaker (Fallback)
        EstudianteResponse msCaido = new EstudianteResponse();
        msCaido.setNombre("MS Estudiante no disponible");
        when(estudianteClientService.obtenerEstudiante(5L)).thenReturn(msCaido);

        mockMvc.perform(post("/asistencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(asistenciaInput)))
            .andExpect(status().isServiceUnavailable()) // Esperamos un 503
            .andExpect(content().string(containsString("no está disponible")));
            
        verify(asistenciaRepository, never()).save(any());
    }
    
    // -------------------------------------------------------
    // GET /asistencia/curso/{idCurso} — Búsquedas personalizadas
    // -------------------------------------------------------

    @Test
    void consultarPorCurso_retornaLista200() throws Exception {
        when(asistenciaRepository.buscarPorCurso(10L)).thenReturn(List.of(asistenciaInput));

        mockMvc.perform(get("/asistencia/curso/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].estado").value("PRESENT"));
            
        verify(asistenciaRepository, times(1)).buscarPorCurso(10L);
    }
}