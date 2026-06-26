/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.controller;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsistenciaRestControllerE2ETest {

    @Test
    void flujoCompleto_registrarAsistencia_conEstudianteExistente_debeRetornar200() throws Exception {
        
        String jsonPayload = """
            {
                "id_clase": 4,
                "id_estudiante": 11,
                "estado": "PRESENT",
                "fecha": "2026-06-01"
            }
            """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/asistencia"))
                .header("Content-Type", "application/json")
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
    @Test
    void flujoAlterno_registrarAsistencia_conEstudianteInexistente_debeRetornar400() throws Exception {
        // Usamos un ID de estudiante que sabemos que NO existe en el microservicio de estudiantes (ej. 99999)
        String jsonPayload = """
            {
                "id_clase": 4,
                "id_estudiante": 99999,
                "estado": "PRESENT",
                "fecha": "2026-06-01"
            }
            """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/asistencia"))
                .header("Content-Type", "application/json")
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

    @Test
    void flujoAlterno_registrarAsistencia_conEstadoInvalido_debeRetornar400() throws Exception {
        // Usamos un estado que no está permitido por la regla de negocio (ej. "TARDE")
        String jsonPayload = """
            {
                "id_clase": 4,
                "id_estudiante": 11,
                "estado": "TARDE",
                "fecha": "2026-06-01"
            }
            """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/asistencia"))
                .header("Content-Type", "application/json")
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
}
