package com.plataforma_lc.asistencia.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests E2E del microservicio de Asistencia.
 *
 * Requieren que el stack completo esté desplegado (EC2 + API Gateway + Azure AD).
 * Se activan SÓLO cuando la variable de entorno TEST_BASE_URL está definida,
 * por lo que nunca bloquean la build local ni el pipeline de unitarios/integración.
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
 *   mvn test -pl asistencia -Dtest=AsistenciaRestControllerE2ETest
 */
@EnabledIfEnvironmentVariable(named = "TEST_BASE_URL", matches = ".+")
class AsistenciaRestControllerE2ETest {

    private String baseUrl;
    private String authHeader;
    private HttpClient client;

    @BeforeEach
    void setUp() {
        // La URL del API Gateway se lee de la variable de entorno → no hay nada hardcodeado.
        // Cada vez que reinicies el laboratorio de AWS sólo actualizas TEST_BASE_URL.
        baseUrl = System.getenv("TEST_BASE_URL") + "/asistencia";

        String token = System.getenv("TEST_JWT_TOKEN");
        authHeader = (token != null && !token.isBlank()) ? "Bearer " + token : "";

        client = HttpClient.newHttpClient();
    }

    // -----------------------------------------------------------------------
    // Flujo principal: registrar asistencia con estudiante existente → 200 OK
    // -----------------------------------------------------------------------

    @Test
    void flujoCompleto_registrarAsistencia_conEstudianteExistente_debeRetornar200() throws Exception {
        String jsonPayload = """
            {
                "id_clase": 4,
                "id_estudiante": 11,
                "estado": "PRESENT",
                "fecha": "2028-06-21"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", authHeader)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("=========================================");
        System.out.println("STATUS HTTP DEVUELTO: " + response.statusCode());
        System.out.println("CUERPO DEVUELTO: " + response.body());
        System.out.println("=========================================");

        assertEquals(200, response.statusCode(), "El status HTTP no fue 200 OK");
        assertTrue(response.body().contains("PRESENT"), "La respuesta no contiene el estado esperado");
    }

    // -----------------------------------------------------------------------
    // Flujo alterno: estudiante inexistente → 400
    // -----------------------------------------------------------------------

    @Test
    void flujoAlterno_registrarAsistencia_conEstudianteInexistente_debeRetornar400() throws Exception {
        String jsonPayload = """
            {
                "id_clase": 4,
                "id_estudiante": 99999,
                "estado": "PRESENT",
                "fecha": "2026-06-01"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", authHeader)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("=========================================");
        System.out.println("TEST: Estudiante Inexistente");
        System.out.println("STATUS HTTP DEVUELTO: " + response.statusCode());
        System.out.println("CUERPO DEVUELTO: " + response.body());
        System.out.println("=========================================");

        assertEquals(400, response.statusCode(), "Se esperaba un 400 Bad Request por estudiante inexistente");
        assertTrue(response.body().contains("no existe"), "El mensaje de error no fue el esperado");
    }

    // -----------------------------------------------------------------------
    // Flujo alterno: estado inválido → 400
    // -----------------------------------------------------------------------

    @Test
    void flujoAlterno_registrarAsistencia_conEstadoInvalido_debeRetornar400() throws Exception {
        String jsonPayload = """
            {
                "id_clase": 4,
                "id_estudiante": 11,
                "estado": "TARDE",
                "fecha": "2026-06-01"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", authHeader)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("=========================================");
        System.out.println("TEST: Estado Inválido");
        System.out.println("STATUS HTTP DEVUELTO: " + response.statusCode());
        System.out.println("CUERPO DEVUELTO: " + response.body());
        System.out.println("=========================================");

        assertEquals(400, response.statusCode(), "Se esperaba un 400 Bad Request por estado inválido");
        assertTrue(response.body().contains("PRESENT, ABSENT o JUSTIFIED"), "El mensaje de error no menciona los estados válidos");
    }

    // -----------------------------------------------------------------------
    // Verifica que sin token el Gateway rechaza con 401
    // -----------------------------------------------------------------------

    @Test
    void sinToken_debeRetornar401DelGateway() throws Exception {
        String jsonPayload = """
            {
                "id_clase": 4,
                "id_estudiante": 11,
                "estado": "PRESENT",
                "fecha": "2026-06-01"
            }
            """;

        // Petición deliberadamente sin header Authorization
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("TEST: Sin token");
        System.out.println("STATUS HTTP DEVUELTO: " + response.statusCode());

        assertEquals(401, response.statusCode(), "El Gateway debería rechazar sin token con 401");
    }
}
