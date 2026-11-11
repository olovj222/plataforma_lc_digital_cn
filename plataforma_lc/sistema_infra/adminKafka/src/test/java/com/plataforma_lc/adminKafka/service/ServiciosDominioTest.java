package com.plataforma_lc.adminKafka.service;

import com.plataforma_lc.adminKafka.dto.AlertaEventDTO;
import com.plataforma_lc.adminKafka.dto.AnaliticaStreamDTO;
import com.plataforma_lc.adminKafka.dto.AuditoriaEventDTO;
import com.plataforma_lc.adminKafka.exception.EventoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiciosDominioTest {

    private AuditoriaService auditoriaService;
    private AnaliticaService analiticaService;
    private AlertaService alertaService;

    @BeforeEach
    void setUp() {
        auditoriaService = new AuditoriaService();
        analiticaService = new AnaliticaService();
        alertaService = new AlertaService();
    }

    // ========================================================================================
    // AUDITORIA SERVICE
    // ========================================================================================

    @Test
    @DisplayName("AuditoriaService: payload válido procesa sin lanzar excepción")
    void auditoria_payloadValido_ok() {
        AuditoriaEventDTO evento = AuditoriaEventDTO.builder()
                .servicio("estudiante")
                .accion("CREATE")
                .usuario("admin@test.com")
                .detalle("Creación de estudiante")
                .build();

        assertDoesNotThrow(() -> auditoriaService.procesar(evento));
    }

    @Test
    @DisplayName("AuditoriaService: payload sin servicio lanza EventoInvalidoException")
    void auditoria_sinServicio_lanzaExcepcion() {
        AuditoriaEventDTO evento = AuditoriaEventDTO.builder()
                .servicio("")
                .accion("CREATE")
                .usuario("admin@test.com")
                .build();

        assertThrows(EventoInvalidoException.class, () -> auditoriaService.procesar(evento));
    }

    // ========================================================================================
    // ANALITICA SERVICE
    // ========================================================================================

    @Test
    @DisplayName("AnaliticaService: payload válido procesa sin lanzar excepción")
    void analitica_payloadValido_ok() {
        AnaliticaStreamDTO dto = AnaliticaStreamDTO.builder()
                .cursoId(10L)
                .estudianteId(100L)
                .fecha(LocalDate.now())
                .estado("PRESENTE")
                .build();

        assertDoesNotThrow(() -> analiticaService.procesar(dto));
    }

    @Test
    @DisplayName("AnaliticaService: payload con cursoId nulo lanza EventoInvalidoException")
    void analitica_cursoIdNulo_lanzaExcepcion() {
        AnaliticaStreamDTO dto = AnaliticaStreamDTO.builder()
                .cursoId(null)
                .estudianteId(100L)
                .estado("PRESENTE")
                .build();

        assertThrows(EventoInvalidoException.class, () -> analiticaService.procesar(dto));
    }

    // ========================================================================================
    // ALERTA SERVICE
    // ========================================================================================

    @Test
    @DisplayName("AlertaService: payload válido procesa sin lanzar excepción")
    void alerta_payloadValido_ok() {
        AlertaEventDTO evento = AlertaEventDTO.builder()
                .tipoAlerta("INASISTENCIA_REITERADA")
                .nivel("CRITICAL")
                .mensaje("Estudiante con 5 faltas")
                .build();

        assertDoesNotThrow(() -> alertaService.procesar(evento));
    }

    @Test
    @DisplayName("AlertaService: payload sin nivel lanza EventoInvalidoException")
    void alerta_sinNivel_lanzaExcepcion() {
        AlertaEventDTO evento = AlertaEventDTO.builder()
                .tipoAlerta("INASISTENCIA_REITERADA")
                .nivel("")
                .mensaje("Estudiante con 5 faltas")
                .build();

        assertThrows(EventoInvalidoException.class, () -> alertaService.procesar(evento));
    }
}

