package com.plataforma_lc.adminKafka.exception;

import org.apache.kafka.common.errors.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/kafka/admin/topics");
    }

    @Test
    @DisplayName("TopicExistsException mapea a HTTP 409 Conflict")
    void handleConflict_topicExists() {
        TopicExistsException ex = new TopicExistsException("El tópico ya existe");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().status());
        assertEquals("El tópico ya existe", response.getBody().message());
    }

    @Test
    @DisplayName("UnknownTopicOrPartitionException mapea a HTTP 404 Not Found")
    void handleNotFound_unknownTopic() {
        UnknownTopicOrPartitionException ex = new UnknownTopicOrPartitionException("Tópico no encontrado");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
    }

    @Test
    @DisplayName("InvalidReplicationFactorException mapea a HTTP 400 Bad Request")
    void handleBadRequest_invalidRf() {
        InvalidReplicationFactorException ex = new InvalidReplicationFactorException("RF no válido");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException mapea a HTTP 400 Bad Request con detalles por campo")
    void handleValidationErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "nombre", "El nombre es obligatorio"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationErrors(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().detallesValidacion());
        assertEquals("El nombre es obligatorio", response.getBody().detallesValidacion().get("nombre"));
    }

    @Test
    @DisplayName("TimeoutException mapea a HTTP 503 Service Unavailable")
    void handleTimeout() {
        TimeoutException ex = new TimeoutException("Timeout al conectar");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTimeout(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(503, response.getBody().status());
    }

    @Test
    @DisplayName("Exception genérica mapea a HTTP 500 Internal Server Error")
    void handleGenericException() {
        RuntimeException ex = new RuntimeException("Error inesperado");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("Error inesperado", response.getBody().message());
    }
}

