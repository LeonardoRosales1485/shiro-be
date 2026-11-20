package com.blue.open.Code.Version;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-VER-10: tareas de una version (GET /version/{uuid}/tareas), para el detalle de versiones del front (T6.3): todas
 * las tareas vigentes asignadas, terminadas o no, con su tipo, tablero y estado, por orden de tipo y numero. Las
 * quitadas no aparecen. Exige VER en el proyecto.
 */
class VersionTareasContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String HECHO = "a2000000-0000-4000-8000-000000000015";

    @Test
    void listaLasTareasTerminadasYSinTerminar() {
        String proyecto = como(ADMIN).post("/proyecto/guardar").json(Map.of("nombre", "Proyecto VTA", "prefijo", "VTA"))
                .preparacion().ejecutar().esperar(200).json().get("uuid").asString();
        String tablero = tableroPrincipal(proyecto);
        JsonNode bug = tarea(tablero, "BUG", "Token revocado");
        JsonNode historia = tarea(tablero, "HISTORIA", "Importar sprints");
        JsonNode quitada = tarea(tablero, "TAREA", "Quitada");
        como(ADMIN).post("/tarea/{uuid}/mover", bug.get("uuid").asString()).json(Map.of("estadoUuid", HECHO)).preparacion().ejecutar().esperar(200);
        String version = como(ADMIN).post("/version/guardar").json(Map.of("proyectoUuid", proyecto, "nombre", "1.0")).ejecutar()
                .esperar(200).json().get("uuid").asString();
        List<String> tareas = List.of(bug.get("uuid").asString(), historia.get("uuid").asString(), quitada.get("uuid").asString());
        como(ADMIN).post("/version/{uuid}/asignar", version).json(Map.of("tareas", tareas)).ejecutar().esperar(200);
        como(ADMIN).post("/version/{uuid}/quitar", version).json(Map.of("tareas", List.of(quitada.get("uuid").asString())))
                .ejecutar().esperar(200);

        JsonNode respuesta = como(ADMIN).get("/version/{uuid}/tareas", version).ejecutar().esperar(200).json();

        assertEquals(2, respuesta.size());
        JsonNode primera = respuesta.get(0);
        assertEquals(historia.get("clave").asString(), primera.get("clave").asString());
        assertEquals("HISTORIA", primera.get("tipoCodigo").asString());
        assertEquals("PRINCIPAL", primera.get("tableroCodigo").asString());
        assertFalse(primera.get("terminada").asBoolean());
        JsonNode segunda = respuesta.get(1);
        assertEquals("Token revocado", segunda.get("titulo").asString());
        assertTrue(segunda.get("terminada").asBoolean());
        assertEquals("Hecho", segunda.get("estadoNombre").asString());
    }

    @Test
    void elGrupoClienteVeLasTareasDeUnaVersionDeShiro() {
        como("CLIENTE").get("/version/{uuid}/tareas", VersionContratoTest.VERSION_DE_SEED).ejecutar().esperar(200);
    }

    @Test
    void versionInexistente() {
        como(ADMIN).get("/version/{uuid}/tareas", "b9999999-0000-4000-8000-000000000999").ejecutar()
                .esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    private String tableroPrincipal(String proyecto) {
        for (JsonNode tablero : como(ADMIN).get("/proyecto/uuid/{uuid}", proyecto).preparacion().ejecutar().esperar(200)
                .json().get("tableros")) {
            if ("PRINCIPAL".equals(tablero.get("tipoTableroCodigo").asString()))
                return tablero.get("uuid").asString();
        }
        throw new IllegalStateException("Sin tablero principal");
    }

    private JsonNode tarea(String tablero, String tipo, String titulo) {
        Map<String, Object> cuerpo = new HashMap<>(Map.of("tableroUuid", tablero, "tipoCodigo", tipo, "titulo", titulo));
        return como(ADMIN).post("/tarea/guardar").json(cuerpo).preparacion().ejecutar().esperar(200).json();
    }
}
