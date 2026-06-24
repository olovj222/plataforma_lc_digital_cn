package com.plataforma_lc.evaluaciones.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas End-to-End dinámicas vinculadas a Eureka Server.
 * * Ya no dependemos de puertos estáticos hardcodeados en el entorno local/Docker.
 */
@SpringBootTest // Habilita el contexto de Spring para inyectar DiscoveryClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Evaluacionese2eTest {

    @Autowired
    private DiscoveryClient discoveryClient; // Cliente de descubrimiento de Eureka

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    // La URL base ahora es dinámica y cambia automáticamente según lo que dicte Eureka
    private String baseUrl;

    // ID que se reutiliza entre tests para simular un flujo real
    private static Long evaluacionId;

    @BeforeEach
    void setup() {
        // Consultamos dinámicamente a Eureka la ubicación exacta de EVALUACIONES
        List<ServiceInstance> instances = discoveryClient.getInstances("EVALUACIONES");
        
        if (instances == null || instances.isEmpty()) {
            fail("❌ ERROR E2E: El microservicio EVALUACIONES no está registrado en el servidor de Eureka.");
        }
        
        // Construye la URL base usando la URI activa en Eureka (ej: http://localhost:8083 o http://host.docker.internal:8083)
        this.baseUrl = instances.get(0).getUri().toString() + "/evaluaciones";
    }

    // -------------------------------------------------------
    // FLUJO COMPLETO: Crear → Buscar → Calificar → Actualizar → Eliminar
    // -------------------------------------------------------

    @Test
    @Order(1)
    void paso1_crear_evaluacionValida_retorna201YDevuelveId() throws Exception {
        String body = """
            {
                "nombre": "Control E2E",
                "cursoId": 1,
                "estudianteId": 1
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        assertNotNull(json.get("id"));
        assertEquals("Control E2E", json.get("nombre").asText());
        assertEquals(1, json.get("cursoId").asInt());
        assertEquals(1, json.get("estudianteId").asInt());

        evaluacionId = json.get("id").asLong();
        System.out.println("✅ Evaluación creada con ID: " + evaluacionId);
    }

    @Test
    @Order(2)
    void paso2_buscarPorCurso_retornaListaConLaEvaluacionCreada() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/curso/1"))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.isArray());
        assertTrue(json.size() > 0);

        boolean encontrado = false;
        for (JsonNode eval : json) {
            if (eval.get("id").asLong() == evaluacionId) {
                assertEquals("Control E2E", eval.get("nombre").asText());
                encontrado = true;
                break;
            }
        }
        assertTrue(encontrado, "La evaluación creada no aparece en la lista por curso");
    }

    @Test
    @Order(3)
    void paso3_buscarPorEstudiante_retornaListaConLaEvaluacionCreada() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/estudiante/1"))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.isArray());
        assertTrue(json.size() > 0);

        boolean encontrado = false;
        for (JsonNode eval : json) {
            if (eval.get("id").asLong() == evaluacionId) {
                encontrado = true;
                break;
            }
        }
        assertTrue(encontrado, "La evaluación creada no aparece en la lista por estudiante");
    }

    @Test
    @Order(4)
    void paso4_ponerNota_notaValida_retorna200ConCalificacion() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/" + evaluacionId + "/nota?nota=6"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        assertEquals(6, json.get("calificacion").asInt());
    }

    @Test
    @Order(5)
    void paso5_ponerNota_notaCero_retorna400PorReglaNegocio() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/" + evaluacionId + "/nota?nota=0"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.get("mensaje").asText().contains("1 y 7"));
    }

    @Test
    @Order(6)
    void paso6_ponerNota_notaOcho_retorna400PorReglaNegocio() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/" + evaluacionId + "/nota?nota=8"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.get("mensaje").asText().contains("1 y 7"));
    }

    @Test
    @Order(7)
    void paso7_actualizar_evaluacionExistente_retorna200ConDatosNuevos() throws Exception {
        String body = """
            {
                "nombre": "Control E2E Actualizado",
                "cursoId": 1,
                "estudianteId": 1
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/" + evaluacionId))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        assertEquals("Control E2E Actualizado", json.get("nombre").asText());
    }

    @Test
    @Order(8)
    @Disabled
    void paso8_eliminar_evaluacionExistente_retorna200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/" + evaluacionId))
            .DELETE()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("Evaluación eliminada correctamente", response.body());
    }

    @Test
    @Order(9)
    @Disabled
    void paso9_eliminar_evaluacionYaEliminada_retorna404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/" + evaluacionId))
            .DELETE()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.get("mensaje").asText().contains(String.valueOf(evaluacionId)));
    }

    // -------------------------------------------------------
    // CASOS BORDE independientes
    // -------------------------------------------------------

    @Test
    @Order(10)
    void crear_evaluacionSinNombre_retorna400() throws Exception {
        String body = """
            {
                "cursoId": 1,
                "estudianteId": 1
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.get("mensaje").asText().contains("nombre"));
    }

    @Test
    @Order(11)
    void crear_evaluacionSinCursoId_retorna400() throws Exception {
        String body = """
            {
                "nombre": "Sin curso",
                "estudianteId": 1
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.get("mensaje").asText().contains("curso"));
    }

    @Test
    @Order(12)
    void actualizar_idInexistente_retorna404() throws Exception {
        String body = """
            {
                "nombre": "No existe",
                "cursoId": 1,
                "estudianteId": 1
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/99999"))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.get("mensaje").asText().contains("99999"));
    }

    @Test
    @Order(13)
    void ponerNota_evaluacionInexistente_retorna404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/99999/nota?nota=5"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.get("mensaje").asText().contains("99999"));
    }

    @Test
    @Order(14)
    void porCurso_cursoSinEvaluaciones_retornaListaVacia() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/curso/99999"))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.isArray());
        assertEquals(0, json.size());
    }

    @Test
    @Order(15)
    void porEstudiante_estudianteSinEvaluaciones_retornaListaVacia() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/estudiante/99999"))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.isArray());
        assertEquals(0, json.size());
    }
}