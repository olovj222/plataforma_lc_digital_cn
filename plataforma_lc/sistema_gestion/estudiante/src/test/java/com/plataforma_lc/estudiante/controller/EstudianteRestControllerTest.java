package com.plataforma_lc.estudiante.controller;

import com.plataforma_lc.estudiante.entities.CursoResponse;
import com.plataforma_lc.estudiante.entities.Estudiante;
import com.plataforma_lc.estudiante.entities.EstudianteCurso;
import com.plataforma_lc.estudiante.exception.BusinessRuleException;
import com.plataforma_lc.estudiante.repository.EstudianteRepository;
import com.plataforma_lc.estudiante.service.CursoClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstudianteRestControllerTest {

    @Mock
    EstudianteRepository estudianteRepository;

    @Mock
    CursoClientService cursoClientService;

    @InjectMocks
    EstudianteRestController controller;

    private Estudiante estudiante;
    private EstudianteCurso estudianteCurso;
    private CursoResponse cursoResponse;

    @BeforeEach
    void setUp() {
        // seteamos datos base
        estudianteCurso = new EstudianteCurso();
        estudianteCurso.setId(1L);
        estudianteCurso.setCursoId(101L);

        List<EstudianteCurso> cursosRelacionados = new ArrayList<>();
        cursosRelacionados.add(estudianteCurso);

        // Configuramos el estudiante
        estudiante = Estudiante.builder()
                .id(1L)
                .nombre("Juan")
                .apPaterno("Pérez")
                .apMaterno("Gómez")
                .direccion("Calle Falsa 123")
                .telefono("555-1234")
                .cursos(cursosRelacionados)
                .build();

        // Configuramos la respuesta simulada del microservicio de cursos
        cursoResponse = new CursoResponse();
        cursoResponse.setNombre("Matemáticas Avanzadas");
    }

    // GET /estudiante

    @Test
    void listarTodos_retornaListaYAsignaNombreDelCurso_Status200() {
        when(estudianteRepository.findAll()).thenReturn(List.of(estudiante));
        when(cursoClientService.obtenerCurso(101L)).thenReturn(cursoResponse);

        ResponseEntity<List<Estudiante>> response = controller.listarTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Matemáticas Avanzadas", response.getBody().get(0).getCursos().get(0).getCursoName());
        verify(cursoClientService, times(1)).obtenerCurso(101L);
    }

    @Test
    void listarTodos_listaVacia_retornaStatus200() {
        when(estudianteRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<Estudiante>> response = controller.listarTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(cursoClientService, never()).obtenerCurso(any());
    }

    // GET /estudiante/{id}

    @Test
    void get_estudianteExistente_retornaEstudianteYAsignaNombreDelCurso_Status200() throws BusinessRuleException {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(cursoClientService.obtenerCurso(101L)).thenReturn(cursoResponse);

        ResponseEntity<Estudiante> response = controller.get(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Juan", response.getBody().getNombre());
        assertEquals("Matemáticas Avanzadas", response.getBody().getCursos().get(0).getCursoName());
    }

    @Test
    void get_estudianteNoExistente_lanzaBusinessRuleException() {
        when(estudianteRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> controller.get(99L));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatus());
        assertTrue(ex.getMessage().contains("99"));
        verify(cursoClientService, never()).obtenerCurso(any());
    }

    // POST /estudiante

    @Test
    void post_estudianteNuevo_retornaCreado_Status201() throws BusinessRuleException {
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(estudiante);

        ResponseEntity<Estudiante> response = controller.post(estudiante);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Juan", response.getBody().getNombre());
        verify(estudianteRepository, times(1)).save(any(Estudiante.class));
    }

    @Test
    void post_sinCursos_lanzaBusinessRuleException_Status400() {
        Estudiante estudianteSinCursos = Estudiante.builder()
                .nombre("Ana")
                .cursos(new ArrayList<>()) // Lista vacía
                .build();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> controller.post(estudianteSinCursos));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatus());
        assertTrue(ex.getMessage().contains("al menos a un curso"));
        verify(estudianteRepository, never()).save(any(Estudiante.class));
    }

    @Test
    void post_cursosNull_lanzaBusinessRuleException_Status400() {
        Estudiante estudianteCursosNull = Estudiante.builder()
                .nombre("Pedro")
                .cursos(null) // Nulo
                .build();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> controller.post(estudianteCursosNull));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatus());
        verify(estudianteRepository, never()).save(any(Estudiante.class));
    }

    // PUT /estudiante/{id}

    @Test
    void put_estudianteExistente_actualizaYRetorna_Status200() throws BusinessRuleException {
        Estudiante inputActualizacion = Estudiante.builder()
                .nombre("Juan Modificado")
                .apPaterno("Pérez")
                .apMaterno("Gómez")
                .direccion("Nueva Dirección")
                .telefono("999-8888")
                .cursos(List.of(estudianteCurso))
                .build();

        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(inputActualizacion);

        ResponseEntity<Estudiante> response = controller.put(1L, inputActualizacion);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Juan Modificado", response.getBody().getNombre());
        assertEquals("Nueva Dirección", response.getBody().getDireccion());
        verify(estudianteRepository, times(1)).save(any(Estudiante.class));
    }

    @Test
    void put_estudianteNoExistente_lanzaBusinessRuleException() {
        when(estudianteRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> controller.put(99L, estudiante));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatus());
        verify(estudianteRepository, never()).save(any(Estudiante.class));
    }

    // DELETE /estudiante/{id}

    @Test
    void delete_estudianteExistente_eliminaYRetorna_Status200() throws BusinessRuleException {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));

        ResponseEntity<Void> response = controller.delete(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(estudianteRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_estudianteNoExistente_lanzaBusinessRuleException() {
        when(estudianteRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> controller.delete(99L));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatus());
        verify(estudianteRepository, never()).deleteById(anyLong());
    }
}