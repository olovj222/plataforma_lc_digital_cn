package com.plataforma_lc.curso.controller;

import com.plataforma_lc.curso.entities.Curso;
import com.plataforma_lc.curso.entities.EstudianteResponse;
import com.plataforma_lc.curso.exception.BusinessRuleException;
import com.plataforma_lc.curso.repository.CursoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CursoRestControllerTest {
    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    CursoRepository cursoRepository;

    @InjectMocks
    CursoRestController controller;

    private Curso curso;
// seteamos valores base para el test
    @BeforeEach
    void setUp() {
        curso = new Curso();
        curso.setId(1L);
        curso.setNombre("Matemáticas");
        curso.setCodigo(101);
        curso.setProfesorId("prof-001");
    }

    // GET /curso 
    @Test
    void listarTodos_retornaListaYStatus200() {
        when(cursoRepository.findAll()).thenReturn(List.of(curso));

        ResponseEntity<List<Curso>> response = controller.listarTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Matemáticas", response.getBody().get(0).getNombre());
    }

    @Test
    void listarTodos_listaVacia_retornaStatus200() {
        when(cursoRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<Curso>> response = controller.listarTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // GET /curso/{id} 
    @Test
    void get_cursoExistente_retornaCursoYStatus200() throws BusinessRuleException {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        ResponseEntity<Curso> response = controller.get(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Matemáticas", response.getBody().getNombre());
    }

    @Test
    void get_cursoNoExistente_lanzaBusinessRuleException() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> controller.get(99L));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatus());
        assertTrue(ex.getMessage().contains("99"));
    }

    // POST /curso 
    @Test
    void post_cursoNuevo_retornaCreadoYStatus201() throws BusinessRuleException {
        when(cursoRepository.findAll()).thenReturn(List.of());
        when(cursoRepository.save(any(Curso.class))).thenReturn(curso);

        ResponseEntity<Curso> response = controller.post(curso);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Matemáticas", response.getBody().getNombre());
        verify(cursoRepository, times(1)).save(any(Curso.class));
    }

    @Test
    void post_cursoSinNombre_lanzaBusinessRuleException() {
        Curso cursoVacio = new Curso();
        cursoVacio.setCodigo(999);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> controller.post(cursoVacio));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatus());
        assertTrue(ex.getMessage().contains("nombre"));
        verify(cursoRepository, never()).save(any(Curso.class));
    }
    
    @Test
    void post_codigoDuplicado_lanzaBusinessRuleException() {
        when(cursoRepository.findAll()).thenReturn(List.of(curso));

        Curso duplicado = new Curso();
        duplicado.setNombre("Historia");
        duplicado.setCodigo(101);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> controller.post(duplicado));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatus());
        verify(cursoRepository, never()).save(any(Curso.class));
    }

    // PUT /curso/{id}
    @Test
    void put_cursoExistente_actualizaYRetornaStatus200() throws BusinessRuleException {
        Curso actualizado = new Curso();
        actualizado.setNombre("Lenguaje");
        actualizado.setCodigo(202);
        actualizado.setProfesorId("prof-002");

        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any(Curso.class))).thenReturn(actualizado);

        ResponseEntity<Curso> response = controller.put(1L, actualizado);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Lenguaje", response.getBody().getNombre());
        verify(cursoRepository, times(1)).save(any(Curso.class));
    }

    @Test
    void put_cursoNoExistente_lanzaBusinessRuleException() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> controller.put(99L, curso));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatus());
        verify(cursoRepository, never()).save(any(Curso.class));
    }

    // DELETE /curso/{id} 
    @Test
    void delete_cursoExistente_eliminaYRetornaStatus200() throws BusinessRuleException {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EstudianteResponse[].class))
            .thenReturn(reactor.core.publisher.Mono.just(new EstudianteResponse[0]));
            ResponseEntity<Void> response = controller.delete(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cursoRepository, times(1)).delete(curso);
    }

    @Test
    void delete_cursoNoExistente_lanzaBusinessRuleException() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> controller.delete(99L));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatus());
        verify(cursoRepository, never()).delete(any(Curso.class));
    }
}