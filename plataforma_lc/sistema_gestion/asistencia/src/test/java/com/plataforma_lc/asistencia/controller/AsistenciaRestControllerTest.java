/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.plataforma_lc.asistencia.controller;

import com.plataforma_lc.asistencia.repository.AsistenciaRepository;
import com.plataforma_lc.asistencia.entities.Asistencia;
import com.plataforma_lc.asistencia.entities.Clase;
import com.plataforma_lc.asistencia.entities.EstudianteResponse;
import com.plataforma_lc.asistencia.repository.ClaseRepository;
import com.plataforma_lc.asistencia.service.EstudianteClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.util.List;
import java.util.Optional;
import org.apache.commons.configuration.PropertyConverter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsistenciaRestControllerTest {

    @Mock private AsistenciaRepository asistenciaRepository;
    @Mock private ClaseRepository claseRepository;
    @Mock private EstudianteClientService estudianteClientService;

    @InjectMocks private AsistenciaRestController controller;


    private Asistencia asistenciaValida() {
        Asistencia a = new Asistencia();
        a.setId_clase(1L);
        a.setId_estudiante(10L);
        a.setEstado("PRESENT");
        a.setFecha(PropertyConverter.toDate("10/5/2026", "dd/MM/yyyy"));
        return a;
    }


    @Test
    void list_debeRetornarTodasLasAsistencias() {
        // Para que este test falle hay que entregarle tres asistencias o cambiar la cantidad de asistencias esperadas
        List<Asistencia> lista = List.of(new Asistencia(), new Asistencia());
        when(asistenciaRepository.findAll()).thenReturn(lista);

        List<Asistencia> resultado = controller.list();

        assertEquals(2, resultado.size());
        verify(asistenciaRepository).findAll();
    }


    @Test
    void post_debeRetornar400_cuandoClaseNoExiste() {
        Asistencia input = asistenciaValida();
        when(claseRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = controller.post(input);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(resp.getBody().toString().contains("clase especificada no existe"));
    }

    @Test
    void post_debeRetornar400_cuandoFechaEsNula() {
        Asistencia input = asistenciaValida();
        input.setFecha(null);
        when(claseRepository.findById(1L)).thenReturn(Optional.of(new Clase()));

        ResponseEntity<?> resp = controller.post(input);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(resp.getBody().toString().contains("fecha"));
    }

    @Test
    void post_debeRetornar400_cuandoEstadoEsInvalido() {
        Asistencia input = asistenciaValida();
        input.setEstado("INVALIDO");
        when(claseRepository.findById(1L)).thenReturn(Optional.of(new Clase()));

        ResponseEntity<?> resp = controller.post(input);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(resp.getBody().toString().contains("estado debe ser"));
    }

    @Test
    void post_debeRetornar400_cuandoEstadoEsNulo() {
        Asistencia input = asistenciaValida();
        input.setEstado(null);
        when(claseRepository.findById(1L)).thenReturn(Optional.of(new Clase()));

        ResponseEntity<?> resp = controller.post(input);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void post_debeRetornar400_cuandoAsistenciaYaExiste() {
        Asistencia input = asistenciaValida();
        when(claseRepository.findById(1L)).thenReturn(Optional.of(new Clase()));
        when(asistenciaRepository.existeRegistroDuplicado(1L, 10L, input.getFecha()))
            .thenReturn(true);

        ResponseEntity<?> resp = controller.post(input);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(resp.getBody().toString().contains("ya tiene asistencia registrada"));
    }

    @Test
    void post_debeRetornar400_cuandoEstudianteNoExiste() {
        Asistencia input = asistenciaValida();
        when(claseRepository.findById(1L)).thenReturn(Optional.of(new Clase()));
        when(asistenciaRepository.existeRegistroDuplicado(anyLong(), anyLong(), any()))
            .thenReturn(false);

        EstudianteResponse fallback = new EstudianteResponse();
        fallback.setNombre("Estudiante no encontrado");
        when(estudianteClientService.obtenerEstudiante(10L)).thenReturn(fallback);

        ResponseEntity<?> resp = controller.post(input);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(resp.getBody().toString().contains("no existe"));
    }

    @Test
    void post_debeRetornar503_cuandoMsEstudiantesNoDisponible() {
        Asistencia input = asistenciaValida();
        when(claseRepository.findById(1L)).thenReturn(Optional.of(new Clase()));
        when(asistenciaRepository.existeRegistroDuplicado(anyLong(), anyLong(), any()))
            .thenReturn(false);

        EstudianteResponse fallback = new EstudianteResponse();
        fallback.setNombre("MS Estudiante no disponible");
        when(estudianteClientService.obtenerEstudiante(10L)).thenReturn(fallback);

        ResponseEntity<?> resp = controller.post(input);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
    }

    @Test
    void post_debeGuardarYRetornar200_cuandoTodoEsValido() {
        Asistencia input = asistenciaValida();
        Asistencia guardada = asistenciaValida();

        when(claseRepository.findById(1L)).thenReturn(Optional.of(new Clase()));
        when(asistenciaRepository.existeRegistroDuplicado(anyLong(), anyLong(), any()))
            .thenReturn(false);

        EstudianteResponse estudiante = new EstudianteResponse();
        estudiante.setNombre("Juan Pérez");
        when(estudianteClientService.obtenerEstudiante(10L)).thenReturn(estudiante);
        when(asistenciaRepository.save(input)).thenReturn(guardada);

        ResponseEntity<?> resp = controller.post(input);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(guardada, resp.getBody());
        verify(asistenciaRepository).save(input);
    }

    @Test
    void post_debeNormalizarEstadoAMayusculas() {
        Asistencia input = asistenciaValida();
        input.setEstado("present");   

        when(claseRepository.findById(1L)).thenReturn(Optional.of(new Clase()));
        when(asistenciaRepository.existeRegistroDuplicado(anyLong(), anyLong(), any()))
            .thenReturn(false);

        EstudianteResponse est = new EstudianteResponse();
        est.setNombre("Ana García");
        when(estudianteClientService.obtenerEstudiante(10L)).thenReturn(est);
        when(asistenciaRepository.save(any())).thenReturn(input);

        controller.post(input);

        assertEquals("PRESENT", input.getEstado());
    }


    @Test
    void put_debeActualizarYRetornar200_cuandoAsistenciaExiste() {
        Asistencia existente = asistenciaValida();
        Asistencia cambios   = asistenciaValida();
        cambios.setEstado("ABSENT");

        when(asistenciaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(asistenciaRepository.save(existente)).thenReturn(existente);

        ResponseEntity<?> resp = controller.put(1L, cambios);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("ABSENT", existente.getEstado());
    }

    @Test
    void put_debeRetornar404_cuandoAsistenciaNoExiste() {
        when(asistenciaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = controller.put(99L, new Asistencia());

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }


    @Test
    void delete_debeEliminarYRetornar200() {
        doNothing().when(asistenciaRepository).deleteById(1L);

        ResponseEntity<?> resp = controller.delete(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        verify(asistenciaRepository).deleteById(1L);
    }


    @Test
    void consultarPorCurso_debeRetornarListaDeAsistencias() {
        List<Asistencia> lista = List.of(new Asistencia());
        when(asistenciaRepository.buscarPorCurso(5L)).thenReturn(lista);

        ResponseEntity<List<Asistencia>> resp = controller.consultarPorCurso(5L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
    }


    @Test
    void consultarPorEstudiante_debeRetornarListaDeAsistencias() {
        List<Asistencia> lista = List.of(new Asistencia(), new Asistencia());
        when(asistenciaRepository.buscarPorEstudiante(10L)).thenReturn(lista);

        ResponseEntity<List<Asistencia>> resp = controller.consultarPorEstudiante(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(2, resp.getBody().size());
    }
}
