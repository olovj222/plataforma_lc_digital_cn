/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.plataforma_lc.asistencia.integrationTest;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsistenciaE2ETest {

    @Test
    void flujoCompleto_registrarAsistencia_conEstudianteExistente_debeRetornar200() throws Exception {
        
        // 1. Preparamos el payload
        String jsonPayload = """
            {
                "id_clase": 1,
                "id_estudiante": 10,
                "estado": "PRESENT",
                "fecha": "2026-05-11"
            }
            """;

        // 2. Usamos el cliente nativo de Java (cero dependencias externas)
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/asistencia"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // 3. Disparamos la petición y capturamos la respuesta real
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 4. Imprimimos el diagnóstico en consola para ver la verdad
        System.out.println("=========================================");
        System.out.println("STATUS HTTP DEVUELTO: " + response.statusCode());
        System.out.println("CUERPO DEVUELTO: " + response.body());
        System.out.println("=========================================");

        // 5. Validaciones finales
        assertEquals(200, response.statusCode(), "El status HTTP no fue 200 OK");
        assertTrue(response.body().contains("PRESENT"), "La respuesta no contiene el estado esperado");
    }
}