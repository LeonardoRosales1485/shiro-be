package com.blue.open.Code.Common.Mcp;

import com.blue.open.Core.Test.TestConBaseDeDatos;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MJ-13 del plan de mejoras y decision D-3: cada token de acceso puede hacer hasta 120 llamadas por minuto a /mcp; la
 * siguiente responde 429 DEMASIADAS_LLAMADAS_MCP con Retry-After. El limite es por token: otro token sigue andando.
 * Usa un token propio para no gastar el cupo de los tokens de los seeds que usan las otras pruebas de MCP.
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class McpLimiteDeLlamadasTest extends TestConBaseDeDatos {

    @LocalServerPort
    private int puerto;

    @Test
    void pasadasLasCientoVeinteLlamadasDelMinutoRespondeCuatrocientosVeintinueve() throws Exception {
        String token = crearToken("Limite de llamadas MCP");
        for (int i = 1; i <= 120; i++) {
            assertEquals(200, ClienteMcpDePrueba.initializeCrudo(puerto, "Bearer " + token).statusCode(), "Llamada " + i);
        }

        HttpResponse<String> excedida = ClienteMcpDePrueba.initializeCrudo(puerto, "Bearer " + token);

        assertEquals(429, excedida.statusCode());
        assertTrue(excedida.body().contains("\"DEMASIADAS_LLAMADAS_MCP\""), excedida.body());
        assertTrue(excedida.headers().firstValue("Retry-After").isPresent(), "Falta Retry-After");
        assertEquals(200, ClienteMcpDePrueba.initializeCrudo(puerto, "Bearer " + ClienteMcpDePrueba.TOKEN_SYSADMIN).statusCode());
    }

    // Crea un token del super admin con el nombre recibido y responde su codigo en claro
    private String crearToken(String nombre) throws Exception {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/usuario/crear-token"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ClienteMcpDePrueba.TOKEN_SYSADMIN)
                .POST(HttpRequest.BodyPublishers.ofString("{\"nombre\":\"" + nombre + "\"}"))
                .build();
        HttpResponse<String> respuesta = HttpClient.newHttpClient().send(pedido, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respuesta.statusCode(), respuesta.body());
        return respuesta.body().replaceAll(".*\"codigo\":\"([^\"]+)\".*", "$1");
    }
}
