package com.plataforma_lc.evaluaciones.controller;

import com.plataforma_lc.evaluaciones.controller.evaluacionesController;
import com.plataforma_lc.evaluaciones.entities.Evaluaciones;
import com.plataforma_lc.evaluaciones.exception.BusinessRuleException;
import com.plataforma_lc.evaluaciones.service.evaluacionesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluacionesControllerTest {

    @Mock
    private evaluacionesService service;

    private evaluacionesController controller;

    private Evaluaciones evaluacion;

    @BeforeEach
    void setUp() {
        controller = new evaluacionesController();
        ReflectionTestUtils.setField(controller, "service", service);

        evaluacion = new Evaluaciones();
        evaluacion.setId(1L);
        evaluacion.setNombre("Control 1");
        evaluacion.setCursoId(10L);
        evaluacion.setEstudianteId(5L);
        evaluacion.setCalificacion(0); // sin nota aún
    }

    // -------------------------------------------------------
    // POST /evaluaciones — crear
    // El controlador delega al service, que llama al repo y
    // rellena nombre de curso y estudiante via RestTemplate.
    // Aquí verificamos que el controller retorna 201 y el cuerpo
    // que le entrega el service (ya con nombres rellenos).
    // -------------------------------------------------------

    @Test
    void crear_evaluacionNueva_retorna201YEvaluacionConNombresRellenos() {
        evaluacion.setCursoNombre("Matemáticas");
        evaluacion.setEstudianteNombre("Juan Pérez");
        when(service.guardar(any(Evaluaciones.class))).thenReturn(evaluacion);

        ResponseEntity<?> response = controller.crear(evaluacion);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Evaluaciones cuerpo = (Evaluaciones) response.getBody();
        assertEquals("Matemáticas", cuerpo.getCursoNombre());
        assertEquals("Juan Pérez", cuerpo.getEstudianteNombre());
        verify(service, times(1)).guardar(any(Evaluaciones.class));
    }

    @Test
    void crear_microserviciosApagados_retorna201ConNombreNoDisponible() {
        // El service maneja el error de conexión y pone el mensaje por defecto
        evaluacion.setCursoNombre("Información no disponible (MS apagado)");
        evaluacion.setEstudianteNombre("Información no disponible (MS apagado)");
        when(service.guardar(any(Evaluaciones.class))).thenReturn(evaluacion);

        ResponseEntity<?> response = controller.crear(evaluacion);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Evaluaciones cuerpo = (Evaluaciones) response.getBody();
        assertTrue(cuerpo.getCursoNombre().contains("MS apagado"));
        assertTrue(cuerpo.getEstudianteNombre().contains("MS apagado"));
    }

    // -------------------------------------------------------
    // PUT /evaluaciones/{id} — actualizar
    // El service lanza BusinessRuleException si el id no existe.
    // El controller no captura excepciones, las propaga.
    // -------------------------------------------------------

    @Test
    void actualizar_evaluacionExistente_retorna200ConDatosActualizados() {
        Evaluaciones actualizada = new Evaluaciones();
        actualizada.setId(1L);
        actualizada.setNombre("Control 2");
        actualizada.setCursoId(10L);
        actualizada.setEstudianteId(5L);
        when(service.actualizar(eq(1L), any(Evaluaciones.class))).thenReturn(actualizada);

        ResponseEntity<?> response = controller.actualizar(1L, actualizada);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Control 2", ((Evaluaciones) response.getBody()).getNombre());
        verify(service, times(1)).actualizar(eq(1L), any(Evaluaciones.class));
    }

    @Test
    void actualizar_idInexistente_propagaBusinessRuleException() {
        when(service.actualizar(eq(99L), any(Evaluaciones.class)))
            .thenThrow(new BusinessRuleException("Evaluación no encontrada con id: 99", 404));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
            () -> controller.actualizar(99L, evaluacion));

        assertEquals(404, ex.getStatus());
        assertTrue(ex.getMessage().contains("99"));
        verify(service, times(1)).actualizar(eq(99L), any(Evaluaciones.class));
    }

    // -------------------------------------------------------
    // POST /evaluaciones/{id}/nota — registrarNota
    // Regla de negocio clave: la nota debe estar entre 1 y 7.
    // El service lanza BusinessRuleException si está fuera de rango
    // o si el id no existe.
    // -------------------------------------------------------

    @Test
    void ponerNota_notaValida_retorna200ConCalificacionActualizada() {
        evaluacion.setCalificacion(6);
        when(service.registrarNota(1L, 6)).thenReturn(evaluacion);

        ResponseEntity<?> response = controller.ponerNota(1L, 6);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(6, ((Evaluaciones) response.getBody()).getCalificacion());
        verify(service, times(1)).registrarNota(1L, 6);
    }

    @Test
    void ponerNota_notaMinimaUno_retorna200() {
        evaluacion.setCalificacion(1);
        when(service.registrarNota(1L, 1)).thenReturn(evaluacion);

        ResponseEntity<?> response = controller.ponerNota(1L, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((Evaluaciones) response.getBody()).getCalificacion());
    }

    @Test
    void ponerNota_notaMaximaSiete_retorna200() {
        evaluacion.setCalificacion(7);
        when(service.registrarNota(1L, 7)).thenReturn(evaluacion);

        ResponseEntity<?> response = controller.ponerNota(1L, 7);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(7, ((Evaluaciones) response.getBody()).getCalificacion());
    }

    @Test
    void ponerNota_notaMenorAUno_propagaBusinessRuleException() {
        // Nota 0 viola la regla de negocio: mínimo es 1
        when(service.registrarNota(1L, 0))
            .thenThrow(new BusinessRuleException("Nota fuera de rango: debe estar entre 1 y 7", 400));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
            () -> controller.ponerNota(1L, 0));

        assertEquals(400, ex.getStatus());
        assertTrue(ex.getMessage().contains("1 y 7"));
    }

    @Test
    void ponerNota_notaMayorASiete_propagaBusinessRuleException() {
        // Nota 8 viola la regla de negocio: máximo es 7
        when(service.registrarNota(1L, 8))
            .thenThrow(new BusinessRuleException("Nota fuera de rango: debe estar entre 1 y 7", 400));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
            () -> controller.ponerNota(1L, 8));

        assertEquals(400, ex.getStatus());
        assertTrue(ex.getMessage().contains("1 y 7"));
    }

    @Test
    void ponerNota_evaluacionNoExiste_propagaBusinessRuleException() {
        when(service.registrarNota(eq(99L), anyInt()))
            .thenThrow(new BusinessRuleException("Evaluación no encontrada con id: 99", 404));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
            () -> controller.ponerNota(99L, 5));

        assertEquals(404, ex.getStatus());
        assertTrue(ex.getMessage().contains("99"));
    }

    // -------------------------------------------------------
    // GET /evaluaciones/curso/{cursoId} — porCurso
    // Retorna todas las evaluaciones de un curso con nombres
    // de curso y estudiante rellenos.
    // -------------------------------------------------------

    @Test
    void porCurso_cursoConVariasEvaluaciones_retornaTodasConNombres() {
        Evaluaciones segunda = new Evaluaciones();
        segunda.setId(2L);
        segunda.setCursoId(10L);
        segunda.setCursoNombre("Matemáticas");
        evaluacion.setCursoNombre("Matemáticas");

        when(service.porCurso(10L)).thenReturn(List.of(evaluacion, segunda));

        ResponseEntity<List<Evaluaciones>> response = controller.porCurso(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        // Todas deben tener el nombre del curso relleno
        response.getBody().forEach(e ->
            assertEquals("Matemáticas", e.getCursoNombre())
        );
    }

    @Test
    void porCurso_cursoSinEvaluaciones_retornaListaVacia() {
        when(service.porCurso(99L)).thenReturn(List.of());

        ResponseEntity<List<Evaluaciones>> response = controller.porCurso(99L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // -------------------------------------------------------
    // GET /evaluaciones/estudiante/{id} — porEstudiante
    // Retorna evaluaciones de distintos cursos para un estudiante.
    // -------------------------------------------------------

    @Test
    void porEstudiante_estudianteConEvaluacionesEnVariosCursos_retornaTodasCorrectamente() {
        Evaluaciones enOtroCurso = new Evaluaciones();
        enOtroCurso.setId(2L);
        enOtroCurso.setEstudianteId(5L);
        enOtroCurso.setCursoId(20L); // curso distinto
        enOtroCurso.setEstudianteNombre("Juan Pérez");
        evaluacion.setEstudianteNombre("Juan Pérez");

        when(service.porEstudiante(5L)).thenReturn(List.of(evaluacion, enOtroCurso));

        ResponseEntity<List<Evaluaciones>> response = controller.porEstudiante(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        // Son de cursos distintos
        assertNotEquals(
            response.getBody().get(0).getCursoId(),
            response.getBody().get(1).getCursoId()
        );
    }

    @Test
    void porEstudiante_estudianteSinEvaluaciones_retornaListaVacia() {
        when(service.porEstudiante(99L)).thenReturn(List.of());

        ResponseEntity<List<Evaluaciones>> response = controller.porEstudiante(99L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // -------------------------------------------------------
    // DELETE /evaluaciones/{id} — eliminar
    // El service lanza BusinessRuleException si el id no existe.
    // -------------------------------------------------------

    @Test
    void eliminar_evaluacionExistente_retornaMensajeConfirmacionYStatus200() {
        doNothing().when(service).eliminar(1L);

        ResponseEntity<?> response = controller.eliminar(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Evaluación eliminada correctamente", response.getBody());
        verify(service, times(1)).eliminar(1L);
    }

    @Test
    void eliminar_idInexistente_propagaBusinessRuleException() {
        // El service lanza excepción si no existe antes de borrar
        doThrow(new BusinessRuleException("Evaluación no encontrada con id: 99", 404))
            .when(service).eliminar(99L);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
            () -> controller.eliminar(99L));

        assertEquals(404, ex.getStatus());
        assertTrue(ex.getMessage().contains("99"));
        verify(service, times(1)).eliminar(99L);
    }
}