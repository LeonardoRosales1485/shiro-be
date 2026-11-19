package com.blue.open.Code.Common.Mcp;

import com.blue.open.Core.Test.TestConBaseDeDatos;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T3.1 del plan y Spec 2: /mcp exige Authorization: Bearer con un token de acceso (protocolo tokens). Sin token, con
 * un JWT o con un token revocado responde 401 antes de llegar a las herramientas.
 * Usa un puerto real: el token que se crea y revoca queda guardado (sin la transaccion del test).
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class McpSeguridadTest extends TestConBaseDeDatos {

    private final ObjectMapper lector = new ObjectMapper();

    @LocalServerPort
    private int puerto;

    @Test
    void sinTokenResponde401() throws Exception {
        HttpResponse<String> respuesta = ClienteMcpDePrueba.initializeCrudo(puerto, null);

        assertEquals(401, respuesta.statusCode());
        assertEquals("NO_AUTENTICADO", lector.readTree(respuesta.body()).get("codigo").asString());
    }

    @Test
    void unJwtNoAlcanza() throws Exception {
        HttpResponse<String> respuesta = ClienteMcpDePrueba.initializeCrudo(puerto,
                "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvIn0.firma");

        assertEquals(401, respuesta.statusCode());
    }

    @Test
    void conUnTokenRevocadoResponde401() throws Exception {
        JsonNode token = lector.readTree(pedir("POST", "/api/usuario/crear-token", "{\"nombre\":\"MCP revocado\"}").body());
        pedir("DELETE", "/api/usuario/borrar-token/" + token.get("uuid").asString(), null);

        HttpResponse<String> respuesta = ClienteMcpDePrueba.initializeCrudo(puerto,
                "Bearer " + token.get("codigo").asString());

        assertEquals(401, respuesta.statusCode());
        assertEquals("ACCESS_TOKEN_INVALIDO", lector.readTree(respuesta.body()).get("codigo").asString());
    }

    @Test
    void conUnTokenValidoEntra() throws Exception {
        HttpResponse<String> respuesta = ClienteMcpDePrueba.initializeCrudo(puerto, "Bearer " + ClienteMcpDePrueba.TOKEN_DEMO);

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("serverInfo"), respuesta.body());
    }

    // Pedido HTTP a la API como el usuario demo (token de los seeds)
    private HttpResponse<String> pedir(String metodo, String ruta, String cuerpo) throws Exception {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ClienteMcpDePrueba.TOKEN_DEMO)
                .method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return HttpClient.newHttpClient().send(pedido, HttpResponse.BodyHandlers.ofString());
    }
}
