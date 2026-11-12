package com.plataforma_lc.anotaciones.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.plataforma_lc.anotaciones.entities.Anotacion;
import com.plataforma_lc.anotaciones.entities.TipoAnotacion;

@DataJpaTest
@ActiveProfiles("test")
class AnotacionRepositoryIntegrationTest {

    @Autowired
    private AnotacionRepository repository;

    private Anotacion nueva(Long estudianteId, Long cursoId, TipoAnotacion tipo) {
        Anotacion a = new Anotacion();
        a.setEstudianteId(estudianteId);
        a.setCursoId(cursoId);
        a.setTipo(tipo);
        a.setDescripcion("Participacion destacada en clases");
        a.setFecha(LocalDate.of(2026, 11, 10));
        a.setAutorId("profe-1");
        return a;
    }

    @Test
    void guardar_asignaIdYPersisteLosCampos() {
        Anotacion guardada = repository.save(nueva(10L, 1L, TipoAnotacion.POSITIVA));

        assertNotNull(guardada.getId());
        Anotacion leida = repository.findById(guardada.getId()).orElseThrow();
        assertEquals(10L, leida.getEstudianteId());
        assertEquals(1L, leida.getCursoId());
        assertEquals(TipoAnotacion.POSITIVA, leida.getTipo());
        assertEquals("profe-1", leida.getAutorId());
        assertNotNull(leida.getFecha());
    }

    @Test
    void findByEstudianteId_devuelveSoloLasDeEseEstudiante() {
        repository.save(nueva(10L, 1L, TipoAnotacion.POSITIVA));
        repository.save(nueva(10L, 2L, TipoAnotacion.NEGATIVA));
        repository.save(nueva(99L, 1L, TipoAnotacion.POSITIVA));

        List<Anotacion> delEstudiante = repository.findByEstudianteId(10L);

        assertEquals(2, delEstudiante.size());
        assertTrue(delEstudiante.stream().allMatch(a -> a.getEstudianteId().equals(10L)));
    }

    @Test
    void findByCursoId_devuelveSoloLasDeEseCurso() {
        repository.save(nueva(10L, 1L, TipoAnotacion.POSITIVA));
        repository.save(nueva(11L, 2L, TipoAnotacion.NEGATIVA));

        List<Anotacion> delCurso = repository.findByCursoId(2L);

        assertEquals(1, delCurso.size());
        assertEquals(2L, delCurso.get(0).getCursoId());
    }

    @Test
    void findByEstudianteIdAndTipo_devuelveSoloLasDeEseEstudianteYTipo() {
        repository.save(nueva(10L, 1L, TipoAnotacion.POSITIVA));
        repository.save(nueva(10L, 1L, TipoAnotacion.NEGATIVA));
        repository.save(nueva(99L, 1L, TipoAnotacion.NEGATIVA));

        List<Anotacion> negativasEstudiante10 = repository.findByEstudianteIdAndTipo(10L, TipoAnotacion.NEGATIVA);

        assertEquals(1, negativasEstudiante10.size());
        assertEquals(10L, negativasEstudiante10.get(0).getEstudianteId());
        assertEquals(TipoAnotacion.NEGATIVA, negativasEstudiante10.get(0).getTipo());
    }
}