package com.plataforma_lc.justificativos.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.plataforma_lc.justificativos.dto.DetallesAuditDto;
import com.plataforma_lc.justificativos.entities.EstadoJustificativo;
import com.plataforma_lc.justificativos.entities.Justificativo;
import com.plataforma_lc.justificativos.exception.ApiExceptionHandler;
import com.plataforma_lc.justificativos.publisher.AuditKafkaProducer;
import com.plataforma_lc.justificativos.publisher.JustificativoPublisher;
import com.plataforma_lc.justificativos.repository.JustificativoRepository;

@ExtendWith(MockitoExtension.class)
class JustificativoRestControllerTest {

    private static final String JSON_VALIDO =
        "{\"estudianteId\": 10, \"cursoId\": 1, "
        + "\"fechaInasistencia\": \"2026-11-10\", \"motivo\": \"Control medico\"}";

    @Mock
    private JustificativoRepository repository;

    @Mock
    private JustificativoPublisher publisher;

    @Mock
    private AuditKafkaProducer auditKafkaProducer;

    @InjectMocks
    private JustificativoRestController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    private Justificativo justificativo(Long id, EstadoJustificativo estado) {
        Justificativo j = new Justificativo();
        j.setId(id);
        j.setEstudianteId(10L);
        j.setCursoId(1L);
        j.setFechaInasistencia(LocalDate.of(2026, 11, 10));
        j.setMotivo("Control medico");
        j.setEstado(estado);
        j.setAutorId("profe-1");
        return j;
    }

    // ---------------- crear ----------------

    @Test
    void crear_conRolProfesor_retorna201_guardaPendienteYPublicaEvento() throws Exception {
        when(repository.save(any(Justificativo.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/justificativos")
                .header("X-User-Id", "profe-1")
                .header("X-User-Roles", "PROFESOR")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("PENDIENTE"))
            .andExpect(jsonPath("$.autorId").value("profe-1"));

        ArgumentCaptor<Justificativo> captor = ArgumentCaptor.forClass(Justificativo.class);
        verify(repository).save(captor.capture());
        assertEquals(EstadoJustificativo.PENDIENTE, captor.getValue().getEstado());
        assertEquals("profe-1", captor.getValue().getAutorId());
        verify(publisher).publicarEventoJustificativo(any(Justificativo.class));
        verify(auditKafkaProducer).emitirEventoAuditoria(
                eq("CREACION_JUSTIFICATIVO"), eq("profe-1"), any(DetallesAuditDto.class));
    }

    @Test
    void crear_sinRolProfesor_retorna403_yNoGuardaNiPublica() throws Exception {
        mockMvc.perform(post("/justificativos")
                .header("X-User-Id", "admin-1")
                .header("X-User-Roles", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
            .andExpect(status().isForbidden());

        verify(repository, never()).save(any(Justificativo.class));
        verify(publisher, never()).publicarEventoJustificativo(any(Justificativo.class));
        verify(auditKafkaProducer, never()).emitirEventoAuditoria(any(), any(), any());
    }

    // ---------------- aprobar / rechazar ----------------

    @Test
    void aprobar_conRolAdmin_cambiaEstadoYPublicaEvento() throws Exception {
        Justificativo existente = justificativo(5L, EstadoJustificativo.PENDIENTE);
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Justificativo.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/justificativos/5/aprobar")
                .header("X-User-Id", "admin-1")
                .header("X-User-Roles", "ADMIN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("APROBADO"))
            .andExpect(jsonPath("$.resueltoPor").value("admin-1"));

        assertNotNull(existente.getFechaResolucion());
        verify(publisher).publicarEventoJustificativo(existente);
        verify(auditKafkaProducer).emitirEventoAuditoria(
                eq("APROBACION_JUSTIFICATIVO"), eq("admin-1"), any(DetallesAuditDto.class));
    }

    @Test
    void rechazar_conRolAdmin_cambiaEstadoYPublicaEvento() throws Exception {
        Justificativo existente = justificativo(6L, EstadoJustificativo.PENDIENTE);
        when(repository.findById(6L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Justificativo.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/justificativos/6/rechazar")
                .header("X-User-Id", "admin-1")
                .header("X-User-Roles", "ADMIN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("RECHAZADO"));

        verify(publisher).publicarEventoJustificativo(existente);
        verify(auditKafkaProducer).emitirEventoAuditoria(
                eq("RECHAZO_JUSTIFICATIVO"), eq("admin-1"), any(DetallesAuditDto.class));
    }

    @Test
    void aprobar_idInexistente_retorna404_yNoPublica() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/justificativos/99/aprobar")
                .header("X-User-Id", "admin-1")
                .header("X-User-Roles", "ADMIN"))
            .andExpect(status().isNotFound());

        verify(publisher, never()).publicarEventoJustificativo(any(Justificativo.class));
        verify(auditKafkaProducer, never()).emitirEventoAuditoria(any(), any(), any());
    }

    @Test
    void aprobar_sinRolAdmin_retorna403_yNoConsultaElRepositorio() throws Exception {
        mockMvc.perform(put("/justificativos/5/aprobar")
                .header("X-User-Id", "profe-1")
                .header("X-User-Roles", "PROFESOR"))
            .andExpect(status().isForbidden());

        verify(repository, never()).findById(any(Long.class));
    }

    // ---------------- consultas ----------------

    @Test
    void pendientes_conRolAdmin_retornaLosPendientes() throws Exception {
        when(repository.findByEstado(EstadoJustificativo.PENDIENTE))
            .thenReturn(List.of(justificativo(1L, EstadoJustificativo.PENDIENTE)));

        mockMvc.perform(get("/justificativos/pendientes")
                .header("X-User-Roles", "ADMIN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    void pendientes_conRolProfesor_retorna403() throws Exception {
        mockMvc.perform(get("/justificativos/pendientes")
                .header("X-User-Roles", "PROFESOR"))
            .andExpect(status().isForbidden());
    }

    @Test
    void porEstudiante_conRolProfesor_retornaLista() throws Exception {
        when(repository.findByEstudianteId(10L))
            .thenReturn(List.of(justificativo(1L, EstadoJustificativo.APROBADO)));

        mockMvc.perform(get("/justificativos/estudiante/10")
                .header("X-User-Roles", "PROFESOR"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void porCurso_conRolAdmin_retornaLista() throws Exception {
        when(repository.findByCursoId(1L))
            .thenReturn(List.of(justificativo(1L, EstadoJustificativo.RECHAZADO),
                                justificativo(2L, EstadoJustificativo.PENDIENTE)));

        mockMvc.perform(get("/justificativos/curso/1")
                .header("X-User-Roles", "ADMIN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void porEstudiante_conRolNoPermitido_retorna403() throws Exception {
        mockMvc.perform(get("/justificativos/estudiante/10")
                .header("X-User-Roles", "ESTUDIANTE"))
            .andExpect(status().isForbidden());
    }
}