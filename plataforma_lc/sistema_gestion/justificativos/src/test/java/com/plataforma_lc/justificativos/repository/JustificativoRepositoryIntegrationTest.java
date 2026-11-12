package com.plataforma_lc.justificativos.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma_lc.justificativos.entities.EstadoJustificativo;
import com.plataforma_lc.justificativos.entities.Justificativo;

/**
 * Prueba de integracion de la entidad y el repositorio JPA contra H2 en memoria.
 * Tambien demuestra que el contexto de la aplicacion carga con el perfil "test".
 * Cada test se revierte (@Transactional), asi que no deja datos entre pruebas.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
class JustificativoRepositoryIntegrationTest {

    @Autowired
    private JustificativoRepository repository;

    private Justificativo nuevo(Long estudianteId, Long cursoId, EstadoJustificativo estado) {
        Justificativo j = new Justificativo();
        j.setEstudianteId(estudianteId);
        j.setCursoId(cursoId);
        j.setFechaInasistencia(LocalDate.of(2026, 11, 10));
        j.setMotivo("Control medico");
        j.setEstado(estado);
        j.setAutorId("profe-1");
        return j;
    }

    @Test
    void guardar_asignaIdYPersisteLosCampos() {
        Justificativo guardado = repository.save(nuevo(10L, 1L, EstadoJustificativo.PENDIENTE));

        assertNotNull(guardado.getId());
        Justificativo leido = repository.findById(guardado.getId()).orElseThrow();
        assertEquals(10L, leido.getEstudianteId());
        assertEquals(EstadoJustificativo.PENDIENTE, leido.getEstado());
        assertEquals("profe-1", leido.getAutorId());
        assertNotNull(leido.getFechaCreacion());
    }

    @Test
    void findByEstado_devuelveSoloLosDelEstadoPedido() {
        repository.save(nuevo(10L, 1L, EstadoJustificativo.PENDIENTE));
        repository.save(nuevo(11L, 1L, EstadoJustificativo.APROBADO));

        List<Justificativo> pendientes = repository.findByEstado(EstadoJustificativo.PENDIENTE);

        assertEquals(1, pendientes.size());
        assertEquals(EstadoJustificativo.PENDIENTE, pendientes.get(0).getEstado());
    }

    @Test
    void findByEstudianteId_devuelveSoloLosDeEseEstudiante() {
        repository.save(nuevo(10L, 1L, EstadoJustificativo.PENDIENTE));
        repository.save(nuevo(10L, 2L, EstadoJustificativo.RECHAZADO));
        repository.save(nuevo(99L, 1L, EstadoJustificativo.PENDIENTE));

        List<Justificativo> delEstudiante = repository.findByEstudianteId(10L);

        assertEquals(2, delEstudiante.size());
        assertTrue(delEstudiante.stream().allMatch(j -> j.getEstudianteId().equals(10L)));
    }

    @Test
    void findByCursoId_devuelveSoloLosDeEseCurso() {
        repository.save(nuevo(10L, 1L, EstadoJustificativo.PENDIENTE));
        repository.save(nuevo(11L, 2L, EstadoJustificativo.PENDIENTE));

        List<Justificativo> delCurso = repository.findByCursoId(2L);

        assertEquals(1, delCurso.size());
        assertEquals(2L, delCurso.get(0).getCursoId());
    }
}