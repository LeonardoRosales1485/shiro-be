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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T3.3 del plan y Spec 4: herramientas de archivo y administracion. Archivar un proyecto o un usuario y cambiar
 * permisos exigen confirmar: true; sin ese parametro la herramienta no cambia nada y responde que haria. La
 * administracion es solo para el super admin y los Administradores.
 * Usa un puerto real: lo que crea queda guardado (sin la transaccion del test).
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class McpArchivoYAdministracionTest extends TestConBaseDeDatos {

    private final ObjectMapper lector = new ObjectMapper();

    @LocalServerPort
    private int puerto;

    @Test
    void archivarUnProyectoSinConfirmarSoloMuestraLaVistaPrevia() {
        try (McpSyncClient agente = ClienteMcpDePrueba.conectar(puerto, ClienteMcpDePrueba.TOKEN_DEMO)) {
            llamar(agente, "administrar_proyecto", Map.of("accion", "crear", "prefijo", "MCPA", "nombre", "Archivo por MCP"));
            llamar(agente, "crear_tarea", Map.of("proyecto", "MCPA", "tipo", "TAREA", "titulo", "Una"));
            llamar(agente, "crear_tarea", Map.of("proyecto", "MCPA", "tipo", "TAREA", "titulo", "Otra"));

            JsonNode previa = llamar(agente, "archivar", Map.of("tipo", "PROYECTO", "identificador", "MCPA"));

            assertEquals("Se archivarían 1 proyecto, 4 tableros y 2 tareas. Repetí la llamada con confirmar: true para hacerlo.",
                    previa.get("resumen").asString());
            assertTrue(llamar(agente, "listar_proyectos", Map.of()).toString().contains("\"prefijo\":\"MCPA\""));

            llamar(agente, "archivar", Map.of("tipo", "PROYECTO", "identificador", "MCPA", "confirmar", true));
            assertFalse(llamar(agente, "listar_proyectos", Map.of()).toString().contains("\"prefijo\":\"MCPA\""));
            llamar(agente, "restaurar", Map.of("tipo", "PROYECTO", "identificador", "MCPA"));
            assertTrue(llamar(agente, "listar_proyectos", Map.of()).toString().contains("\"prefijo\":\"MCPA\""));
        }
    }

    @Test
    void archivarYRestaurarUnaTareaNoPideConfirmacion() {
        try (McpSyncClient agente = ClienteMcpDePrueba.conectar(puerto, ClienteMcpDePrueba.TOKEN_DEMO)) {
            String clave = llamar(agente, "crear_tarea", Map.of("proyecto", "SHI", "tipo", "TAREA", "titulo", "Para archivar"))
                    .get("datos").get("clave").asString();

            assertEquals(-1, llamar(agente, "archivar", Map.of("tipo", "TAREA", "identificador", clave)).get("datos")
                    .get("status").asInt());
            assertEquals(0, llamar(agente, "restaurar", Map.of("tipo", "TAREA", "identificador", clave)).get("datos")
                    .get("status").asInt());
        }
    }

    @Test
    void administrarUsuariosYPermisosConConfirmacion() {
        try (McpSyncClient agente = ClienteMcpDePrueba.conectar(puerto, ClienteMcpDePrueba.TOKEN_DEMO)) {
            llamar(agente, "administrar_usuarios", Map.of("accion", "crear", "nombreDeUsuario", "agente-mcp",
                    "email", "agente-mcp@example.com", "contrasena", "clave-segura-1"));

            JsonNode previaGrupos = llamar(agente, "administrar_usuarios", Map.of("accion", "asignar_grupos",
                    "nombreDeUsuario", "agente-mcp", "grupos", List.of("CLIENTE")));
            assertTrue(previaGrupos.get("resumen").asString().startsWith("Se asignarían"), previaGrupos::toString);
            Map<String, Object> asignar = new HashMap<>(Map.of("accion", "asignar_grupos", "nombreDeUsuario", "agente-mcp",
                    "grupos", List.of("CLIENTE"), "confirmar", true));
            assertEquals("CLIENTE", llamar(agente, "administrar_usuarios", asignar).get("datos").get(0).get("nombre").asString());

            Map<String, Object> regla = new HashMap<>(Map.of("accion", "crear", "sujeto", "usuario:agente-mcp",
                    "recurso", "SHI/QA", "nivel", "EDITAR", "efecto", "PERMITIR"));
            JsonNode previaRegla = llamar(agente, "administrar_permisos", regla);
            assertTrue(previaRegla.get("resumen").asString().startsWith("Se crearía"), previaRegla::toString);
            regla.put("confirmar", true);
            assertEquals("EDITAR", llamar(agente, "administrar_permisos", regla).get("datos").get("nivel").asString());

            JsonNode previaUsuario = llamar(agente, "archivar", Map.of("tipo", "USUARIO", "identificador", "agente-mcp"));
            assertTrue(previaUsuario.get("resumen").asString().startsWith("Se archivaría el usuario agente-mcp"), previaUsuario::toString);
            llamar(agente, "archivar", Map.of("tipo", "USUARIO", "identificador", "agente-mcp", "confirmar", true));
        }
    }

    @Test
    void sinSerAdministradorNoSeAdministra() {
        try (McpSyncClient agente = ClienteMcpDePrueba.conectar(puerto, ClienteMcpDePrueba.TOKEN_CLIENTE)) {
            McpSchema.CallToolResult resultado = ClienteMcpDePrueba.llamar(agente, "administrar_proyecto",
                    Map.of("accion", "crear", "prefijo", "NOAD", "nombre", "No admin"));

            assertTrue(Boolean.TRUE.equals(resultado.isError()), resultado::toString);
            assertTrue(ClienteMcpDePrueba.texto(resultado).contains("Solo el super admin y los Administradores"), resultado::toString);
        }
    }

    // Llama la herramienta, exige que no sea un error y responde su JSON (resumen y datos)
    private JsonNode llamar(McpSyncClient agente, String herramienta, Map<String, Object> argumentos) {
        McpSchema.CallToolResult resultado = ClienteMcpDePrueba.llamar(agente, herramienta, argumentos);
        assertFalse(Boolean.TRUE.equals(resultado.isError()), () -> herramienta + ": " + resultado);
        return lector.readTree(ClienteMcpDePrueba.texto(resultado));
    }
}
