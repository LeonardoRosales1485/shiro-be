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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T5.5 del plan y Spec 4: prueba de punta a punta de las herramientas de planificacion con un cliente MCP real:
 * backlog (ver y ordenar), sprint (crear, asignar, iniciar y cerrar con confirmar), version (crear, asignar,
 * changelog y publicar con confirmar) y busqueda de archivados.
 * Usa un puerto real: lo que crea queda guardado (sin la transaccion del test).
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class McpPlanificacionTest extends TestConBaseDeDatos {

    private final ObjectMapper lector = new ObjectMapper();

    @LocalServerPort
    private int puerto;

    @Test
    void unAgentePlanificaUnProyecto() {
        try (McpSyncClient agente = ClienteMcpDePrueba.conectar(puerto, ClienteMcpDePrueba.TOKEN_DEMO)) {
            llamar(agente, "administrar_proyecto", Map.of("accion", "crear", "prefijo", "MCPP", "nombre", "Planificación por MCP"));
            String a = crear(agente, "Primera");
            String b = crear(agente, "Segunda");
            String c = crear(agente, "Tercera");

            llamar(agente, "ordenar_backlog", Map.of("proyecto", "MCPP", "tareas", List.of(c, a)));
            assertEquals(List.of(c, a, b), backlog(agente));

            llamar(agente, "gestionar_sprint", Map.of("accion", "crear", "proyecto", "MCPP", "sprint", "S1",
                    "inicio", "2026-12-01", "fin", "2026-12-14"));
            llamar(agente, "gestionar_sprint", Map.of("accion", "asignar", "proyecto", "MCPP", "sprint", "S1", "tareas", List.of(a, b)));
            llamar(agente, "gestionar_sprint", Map.of("accion", "iniciar", "proyecto", "MCPP", "sprint", "S1"));
            llamar(agente, "mover_tarea", Map.of("clave", a, "estado", "Hecho"));

            JsonNode previa = llamar(agente, "gestionar_sprint", Map.of("accion", "cerrar", "proyecto", "MCPP", "sprint", "S1"));
            assertEquals("Se cerraría el sprint S1: 1 tareas terminadas quedan en el sprint y 1 sin terminar van al backlog. "
                    + "Repetí la llamada con confirmar: true para hacerlo.", previa.get("resumen").asString());
            Map<String, Object> cerrar = new HashMap<>(Map.of("accion", "cerrar", "proyecto", "MCPP", "sprint", "S1", "confirmar", true));
            assertEquals("CERRADO", llamar(agente, "gestionar_sprint", cerrar).get("datos").get("estado").asString());
            assertEquals(List.of(c, b), backlog(agente));

            llamar(agente, "gestionar_version", Map.of("accion", "crear", "proyecto", "MCPP", "version", "1.0"));
            llamar(agente, "gestionar_version", Map.of("accion", "asignar", "proyecto", "MCPP", "version", "1.0", "tareas", List.of(a)));
            assertTrue(llamar(agente, "gestionar_version", Map.of("accion", "changelog", "proyecto", "MCPP", "version", "1.0"))
                    .get("datos").asString().contains(a + " Primera"));
            assertTrue(llamar(agente, "gestionar_version", Map.of("accion", "publicar", "proyecto", "MCPP", "version", "1.0"))
                    .get("resumen").asString().startsWith("Se publicaría la versión 1.0"));
            Map<String, Object> publicar = new HashMap<>(Map.of("accion", "publicar", "proyecto", "MCPP", "version", "1.0",
                    "fecha", "2026-12-20", "confirmar", true));
            assertEquals("PUBLICADA", llamar(agente, "gestionar_version", publicar).get("datos").get("estado").asString());

            llamar(agente, "archivar", Map.of("tipo", "TAREA", "identificador", c));
            assertTrue(llamar(agente, "buscar_archivados", Map.of("tipo", "TAREA", "texto", "Tercera")).toString().contains(c));
        }
    }

    @Test
    void sinEditarElProyectoNoSePlanifica() {
        try (McpSyncClient agente = ClienteMcpDePrueba.conectar(puerto, ClienteMcpDePrueba.TOKEN_CLIENTE)) {
            McpSchema.CallToolResult resultado = ClienteMcpDePrueba.llamar(agente, "gestionar_sprint",
                    Map.of("accion", "crear", "proyecto", "SHI", "sprint", "Sin permiso", "inicio", "2026-12-01", "fin", "2026-12-14"));

            assertTrue(Boolean.TRUE.equals(resultado.isError()), resultado::toString);
        }
    }

    private String crear(McpSyncClient agente, String titulo) {
        return llamar(agente, "crear_tarea", Map.of("proyecto", "MCPP", "tipo", "TAREA", "titulo", titulo))
                .get("datos").get("clave").asString();
    }

    private List<String> backlog(McpSyncClient agente) {
        List<String> claves = new ArrayList<>();
        llamar(agente, "ver_backlog", Map.of("proyecto", "MCPP")).get("datos").forEach(t -> claves.add(t.get("clave").asString()));
        return claves;
    }

    // Llama la herramienta, exige que no sea un error y responde su JSON (resumen y datos)
    private JsonNode llamar(McpSyncClient agente, String herramienta, Map<String, Object> argumentos) {
        McpSchema.CallToolResult resultado = ClienteMcpDePrueba.llamar(agente, herramienta, argumentos);
        assertFalse(Boolean.TRUE.equals(resultado.isError()), () -> herramienta + ": " + resultado);
        return lector.readTree(ClienteMcpDePrueba.texto(resultado));
    }
}
