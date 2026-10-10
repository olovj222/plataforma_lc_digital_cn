package com.plataforma_lc.evaluaciones.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests E2E del microservicio de Evaluaciones.
 *
 * Requieren que el stack completo esté desplegado (EC2 + API Gateway + Azure AD).
 * Se activan SÓLO cuando la variable de entorno TEST_BASE_URL está definida,
 * por lo que nunca bloquean la build local ni el pipeline de unitarios/integración.
 *
 * La URL del API Gateway se resuelve dinámicamente desde la variable de entorno,
 * eliminando la necesidad de hardcodear la IP pública de EC2 (que cambia con
 * cada reinicio del laboratorio de AWS).
 *
 * Variables de entorno necesarias:
 *   TEST_BASE_URL  → URL pública del API Gateway, sin barra al final.
 *                    Ejemplo: http://54.123.45.67:8085
 *   TEST_JWT_TOKEN → Token JWT de Azure AD obtenido con el script
 *                    scripts/get-test-token.sh (flujo client_credentials).
 *
 * Uso rápido:
 *   export TEST_BASE_URL=http://<ec2-ip>:8085
 *   export TEST_JWT_TOKEN=$(bash scripts/get-test-token.sh)
 *   mvn test -pl evaluaciones -Dtest=Evaluacionese2eTest
 */
@EnabledIfEnvironmentVariable(named = "TEST_BASE_URL", matches = ".+")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Evaluacionese2eTest {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    // La URL base y el token se construyen una sola vez para todos los tests.
    // La IP de EC2 viene de la variable de entorno → no hay nada hardcodeado.
    private static String baseUrl;
    private static String authHeader;

    // ID que se reutiliza entre tests para simular un flujo real
    private static Long evaluacionId;

    @BeforeAll
    static void setup() {
        // Cada vez que reinicies el laboratorio de AWS, sólo necesitas actualizar
        // TEST_BASE_URL. El test la leerá automáticamente.
        baseUrl = System.getenv("TEST_BASE_URL") + "/evaluaciones";

        String token = System.getenv("TEST_JWT_TOKEN");
        authHeader = (token != null && !token.isBlank()) ? "Bearer " + token : "";

        System.out.println("▶ E2E Evaluaciones apuntando a: " + baseUrl);
    }

    // Método auxiliar para construir requests con el token Azure AD inyectado
    private HttpRequest.Builder buildRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", authHeader);
    }

    // -----------------------------------------------------------------------
    // Verifica que sin token el Gateway rechaza con 401
    // -----------------------------------------------------------------------

    @Test
    @Order(0)
    void paso0_sinToken_debeRetornar401DelGateway() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, response.statusCode(), "El Gateway debería rechazar sin token con 401");
        System.out.println("✅ Paso 0: 401 sin token — OK");
    }

    // -----------------------------------------------------------------------
    // FLUJO COMPLETO: Crear → Buscar → Calificar → Actualizar → Eliminar
    // -----------------------------------------------------------------------

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

        HttpRequest request = buildRequest(baseUrl)
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
        HttpRequest request = buildRequest(baseUrl + "/curso/1").GET().build();
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
        HttpRequest request = buildRequest(baseUrl + "/estudiante/1").GET().build();
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
        HttpRequest request = buildRequest(baseUrl + "/" + evaluacionId + "/nota?nota=6")
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
        HttpRequest request = buildRequest(baseUrl + "/" + evaluacionId + "/nota?nota=0")
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
        HttpRequest request = buildRequest(baseUrl + "/" + evaluacionId + "/nota?nota=8")
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

        HttpRequest request = buildRequest(baseUrl + "/" + evaluacionId)
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
    void paso8_eliminar_evaluacionExistente_retorna200() throws Exception {
        HttpRequest request = buildRequest(baseUrl + "/" + evaluacionId)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("Evaluación eliminada correctamente", response.body());
        System.out.println("✅ Evaluación " + evaluacionId + " eliminada correctamente");
    }

    @Test
    @Order(9)
    void paso9_eliminar_evaluacionYaEliminada_retorna404() throws Exception {
        HttpRequest request = buildRequest(baseUrl + "/" + evaluacionId)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.get("mensaje").asText().contains(String.valueOf(evaluacionId)));
    }

    // -----------------------------------------------------------------------
    // CASOS BORDE independientes
    // -----------------------------------------------------------------------

    @Test
    @Order(10)
    void crear_evaluacionSinNombre_retorna400() throws Exception {
        String body = """
            {
                "cursoId": 1,
                "estudianteId": 1
            }
            """;

        HttpRequest request = buildRequest(baseUrl)
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

        HttpRequest request = buildRequest(baseUrl)
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

        HttpRequest request = buildRequest(baseUrl + "/99999")
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
        HttpRequest request = buildRequest(baseUrl + "/99999/nota?nota=5")
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
        HttpRequest request = buildRequest(baseUrl + "/curso/99999").GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.isArray());
        assertEquals(0, json.size());
    }

    @Test
    @Order(15)
    void porEstudiante_estudianteSinEvaluaciones_retornaListaVacia() throws Exception {
        HttpRequest request = buildRequest(baseUrl + "/estudiante/99999").GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        JsonNode json = mapper.readTree(response.body());
        assertTrue(json.isArray());
        assertEquals(0, json.size());
    }
}