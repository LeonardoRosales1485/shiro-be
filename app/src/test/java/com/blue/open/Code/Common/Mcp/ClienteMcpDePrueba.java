package com.blue.open.Code.Common.Mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Cliente MCP real (SDK Java oficial) para las pruebas: se conecta por Streamable HTTP a /api/mcp con un token de
 * acceso, como lo haria el LLM de un usuario. Tambien hace pedidos HTTP crudos para probar los rechazos del filtro.
 */
public final class ClienteMcpDePrueba {

    // Tokens de acceso de los seeds (en claro; la base guarda su hash)
    public static final String TOKEN_SYSADMIN = "sysadmin-access-token";

    public static final String TOKEN_DEMO = "demo-access-token";

    public static final String TOKEN_CLIENTE = "cliente-access-token";

    private ClienteMcpDePrueba() {
    }

    // Cliente MCP inicializado contra el puerto recibido, autenticado con el token. Hay que cerrarlo
    public static McpSyncClient conectar(int puerto, String token) {
        HttpClientStreamableHttpTransport transporte = HttpClientStreamableHttpTransport
                .builder("http://localhost:" + puerto)
                .endpoint("/api/mcp")
                .httpRequestCustomizer((pedido, metodo, uri, cuerpo, contexto) -> pedido.header("Authorization", "Bearer " + token))
                .build();
        McpSyncClient cliente = McpClient.sync(transporte).requestTimeout(Duration.ofSeconds(20)).build();
        cliente.initialize();
        return cliente;
    }

    // Llama una herramienta y responde el texto de su primer contenido
    public static McpSchema.CallToolResult llamar(McpSyncClient cliente, String herramienta, Map<String, Object> argumentos) {
        return cliente.callTool(new McpSchema.CallToolRequest(herramienta, argumentos));
    }

    // Texto del primer contenido de un resultado
    public static String texto(McpSchema.CallToolResult resultado) {
        return ((McpSchema.TextContent) resultado.content().get(0)).text();
    }

    // POST crudo de initialize a /api/mcp con el header Authorization recibido (o sin header si es null)
    public static HttpResponse<String> initializeCrudo(int puerto, String authorization) throws Exception {
        HttpRequest.Builder pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/mcp"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\","
                        + "\"params\":{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{},"
                        + "\"clientInfo\":{\"name\":\"prueba\",\"version\":\"1\"}}}"));
        if (authorization != null)
            pedido.header("Authorization", authorization);
        return HttpClient.newHttpClient().send(pedido.build(), HttpResponse.BodyHandlers.ofString());
    }
}
