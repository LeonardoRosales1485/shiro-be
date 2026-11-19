package com.blue.open.Code.Common.Mcp;

import com.blue.open.Core.Test.TestConBaseDeDatos;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T3.2 del plan y Spec 4: prueba de punta a punta con un cliente MCP real. Un agente lista proyectos, crea una tarea
 * solo con tipo y titulo, la edita, la mueve, la comenta, la relaciona, la clona, la busca y ve el tablero; todo queda
 * en el historial con canal MCP. Con un usuario sin permiso de edicion, la herramienta responde el error.
 * Usa un puerto real: lo que crea queda guardado (sin la transaccion del test).
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class McpHerramientasTest extends TestConBaseDeDatos {

    private final ObjectMapper lector = new ObjectMapper();

    @LocalServerPort
    private int puerto;

    @Test
    void unAgenteTrabajaUnaTareaDePuntaAPunta() {
        try (McpSyncClient agente = ClienteMcpDePrueba.conectar(puerto, ClienteMcpDePrueba.TOKEN_DEMO)) {
            assertTrue(llamar(agente, "listar_proyectos", Map.of()).toString().contains("\"prefijo\":\"SHI\""));

            JsonNode creada = llamar(agente, "crear_tarea", Map.of("proyecto", "shi", "tipo", "tarea",
                    "titulo", "Tarea creada por un agente")).get("datos");
            String clave = creada.get("clave").asString();
            assertEquals("Por hacer", creada.get("estadoNombre").asString());

            llamar(agente, "editar_tarea", Map.of("clave", clave, "prioridad", "ALTA", "etiquetas", List.of("mcp")));
            JsonNode movida = llamar(agente, "mover_tarea", Map.of("clave", clave, "estado", "en curso")).get("datos");
            assertEquals("En curso", movida.get("estadoNombre").asString());
            llamar(agente, "comentar_tarea", Map.of("clave", clave, "cuerpo", "Comentario del agente"));
            JsonNode otra = llamar(agente, "crear_tarea", Map.of("proyecto", "SHI", "tipo", "BUG",
                    "titulo", "Bug para relacionar", "tablero", "QA")).get("datos");
            llamar(agente, "relacionar_tareas", Map.of("origen", otra.get("clave").asString(), "tipo", "BLOQUEA",
                    "destino", clave));
            JsonNode clon = llamar(agente, "clonar_tarea", Map.of("clave", clave, "tablero", "QA")).get("datos");
            assertEquals("Por probar", clon.get("estadoNombre").asString());

            JsonNode obtenida = llamar(agente, "obtener_tarea", Map.of("clave", clave.toLowerCase())).get("datos");
            assertEquals("ALTA", obtenida.get("prioridad").asString());
            assertTrue(llamar(agente, "buscar_tareas", Map.of("texto", "creada por un agente")).toString().contains(clave));
            assertTrue(llamar(agente, "ver_tablero", Map.of("proyecto", "SHI")).get("datos").get("En curso").toString()
                    .contains(clave));

            JsonNode historial = llamar(agente, "ver_historial", Map.of("clave", clave, "soloMcp", true)).get("datos");
            assertTrue(historial.size() >= 5, historial::toString);
            for (JsonNode fila : historial) {
                assertEquals("MCP", fila.get("canal").asString());
            }
        }
    }

    @Test
    void sinPermisoDeEdicionLaHerramientaRespondeElError() {
        try (McpSyncClient agente = ClienteMcpDePrueba.conectar(puerto, ClienteMcpDePrueba.TOKEN_CLIENTE)) {
            McpSchema.CallToolResult resultado = ClienteMcpDePrueba.llamar(agente, "mover_tarea",
                    Map.of("clave", "SHI-1", "estado", "En curso"));

            assertTrue(Boolean.TRUE.equals(resultado.isError()), resultado::toString);
            assertTrue(ClienteMcpDePrueba.texto(resultado).contains("No tenés permiso para editar"), resultado::toString);
        }
    }

    @Test
    void unaClaveInexistenteDiceQueNoExiste() {
        try (McpSyncClient agente = ClienteMcpDePrueba.conectar(puerto, ClienteMcpDePrueba.TOKEN_DEMO)) {
            McpSchema.CallToolResult resultado = ClienteMcpDePrueba.llamar(agente, "obtener_tarea", Map.of("clave", "SHI-99999"));

            assertTrue(Boolean.TRUE.equals(resultado.isError()), resultado::toString);
            assertTrue(ClienteMcpDePrueba.texto(resultado).contains("No existe la tarea SHI-99999"), resultado::toString);
        }
    }

    // Llama la herramienta, exige que no sea un error y responde su JSON (resumen y datos)
    private JsonNode llamar(McpSyncClient agente, String herramienta, Map<String, Object> argumentos) {
        McpSchema.CallToolResult resultado = ClienteMcpDePrueba.llamar(agente, herramienta, argumentos);
        assertFalse(Boolean.TRUE.equals(resultado.isError()), () -> herramienta + ": " + resultado);
        return lector.readTree(ClienteMcpDePrueba.texto(resultado));
    }
}
