package com.plataforma_lc.anotaciones.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.plataforma_lc.anotaciones.entities.Anotacion;
import com.plataforma_lc.anotaciones.entities.TipoAnotacion;
import com.plataforma_lc.anotaciones.publisher.AnotacionPublisher;
import com.plataforma_lc.anotaciones.repository.AnotacionRepository;

@WebMvcTest(AnotacionRestController.class)
class AnotacionRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnotacionRepository repository;

    @MockitoBean
    private AnotacionPublisher anotacionPublisher;

    private Anotacion ejemplo(Long id, TipoAnotacion tipo) {
        Anotacion a = new Anotacion();
        a.setId(id);
        a.setEstudianteId(10L);
        a.setCursoId(1L);
        a.setTipo(tipo);
        a.setDescripcion("Detalle de prueba");
        a.setFecha(LocalDate.of(2026, 11, 12));
        a.setAutorId("profe-1");
        return a;
    }

    @Test
    void obtenerTodas_conRolAdmin_retorna200YLista() throws Exception {
        when(repository.findAll()).thenReturn(List.of(ejemplo(1L, TipoAnotacion.POSITIVA)));

        mockMvc.perform(get("/anotaciones")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].tipo").value("POSITIVA"));
    }

    @Test
    void obtenerTodas_sinRolAdmin_retorna403() throws Exception {
        mockMvc.perform(get("/anotaciones")
                        .header("X-User-Roles", "PROFESOR"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(repository);
    }

    @Test
    void crear_conRolProfesor_retorna201_guardaYPublicaEvento() throws Exception {
        Anotacion guardada = ejemplo(5L, TipoAnotacion.NEGATIVA);
        when(repository.save(any(Anotacion.class))).thenReturn(guardada);

        String body = """
                {
                  "estudianteId": 10,
                  "cursoId": 1,
                  "tipo": "NEGATIVA",
                  "descripcion": "Detalle de prueba"
                }
                """;

        mockMvc.perform(post("/anotaciones")
                        .header("X-User-Id", "profe-1")
                        .header("X-User-Roles", "PROFESOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.autorId").value("profe-1"))
                .andExpect(jsonPath("$.tipo").value("NEGATIVA"));

        verify(repository).save(any(Anotacion.class));
        verify(anotacionPublisher).publicarAnotacionNegativa(guardada);
    }

    @Test
    void crear_sinRolProfesor_retorna403_yNoGuardaNiPublica() throws Exception {
        String body = """
                {
                  "estudianteId": 10,
                  "cursoId": 1,
                  "tipo": "NEGATIVA",
                  "descripcion": "Detalle de prueba"
                }
                """;

        mockMvc.perform(post("/anotaciones")
                        .header("X-User-Id", "est-10")
                        .header("X-User-Roles", "ESTUDIANTE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verifyNoInteractions(repository, anotacionPublisher);
    }

    @Test
    void porEstudiante_conRolProfesor_retorna200() throws Exception {
        when(repository.findByEstudianteId(10L)).thenReturn(List.of(ejemplo(2L, TipoAnotacion.POSITIVA)));

        mockMvc.perform(get("/anotaciones/estudiante/10")
                        .header("X-User-Roles", "PROFESOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estudianteId").value(10L));
    }

    @Test
    void porCurso_conRolAdmin_retorna200() throws Exception {
        when(repository.findByCursoId(1L)).thenReturn(List.of(ejemplo(3L, TipoAnotacion.NEGATIVA)));

        mockMvc.perform(get("/anotaciones/curso/1")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cursoId").value(1L));
    }

    @Test
    void porEstudianteYTipo_conRolProfesor_retorna200() throws Exception {
        when(repository.findByEstudianteIdAndTipo(10L, TipoAnotacion.NEGATIVA))
                .thenReturn(List.of(ejemplo(4L, TipoAnotacion.NEGATIVA)));

        mockMvc.perform(get("/anotaciones/estudiante/10/tipo/NEGATIVA")
                        .header("X-User-Roles", "PROFESOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("NEGATIVA"));
    }

    @Test
    void eliminar_conRolAdminYExistente_retorna200() throws Exception {
        Anotacion existente = ejemplo(8L, TipoAnotacion.NEGATIVA);
        when(repository.findById(8L)).thenReturn(Optional.of(existente));

        mockMvc.perform(delete("/anotaciones/8")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk());

        verify(repository).delete(existente);
    }

    @Test
    void eliminar_cuandoNoExiste_retorna404() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/anotaciones/99")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isNotFound());
    }
}