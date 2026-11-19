package com.blue.open.Code.Common.Mcp;

import com.blue.open.Core.Test.TestConBaseDeDatos;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T0.2 del plan: prueba de compatibilidad del servidor MCP de Spring AI 2.0.2 con Spring Boot 4.1.1. Levanta la
 * aplicacion en un puerto real y un cliente MCP (SDK Java oficial) llama la herramienta de eco por Streamable HTTP
 * en /api/mcp, con un token de acceso (T3.1).
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class McpEcoTest extends TestConBaseDeDatos {

    @LocalServerPort
    private int puerto;

    @Test
    void unClienteMcpLlamaLaHerramientaDeEcoPorStreamableHttp() {
        try (McpSyncClient cliente = ClienteMcpDePrueba.conectar(puerto, ClienteMcpDePrueba.TOKEN_DEMO)) {
            List<String> herramientas = cliente.listTools().tools().stream().map(McpSchema.Tool::name).toList();
            assertTrue(herramientas.contains("eco"), "Herramientas publicadas: " + herramientas);

            McpSchema.CallToolResult resultado = ClienteMcpDePrueba.llamar(cliente, "eco", Map.of("texto", "hola Shiro"));

            assertFalse(Boolean.TRUE.equals(resultado.isError()), resultado::toString);
            assertEquals("hola Shiro", ClienteMcpDePrueba.texto(resultado));
        }
    }
}
