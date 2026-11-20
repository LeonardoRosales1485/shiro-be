package com.blue.open.Code.Sprint;

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
 * CU-SPR-09: cierre de un sprint (GET /sprint/{uuid}/cierre, decision del 19/11 sobre la propuesta 46). Al cerrar se
 * guarda quien lo cerro y cuando, y por cada tarea que tenia el sprint: su estado y sus puntos al cerrar, si estaba
 * terminada y adonde fue (se queda en el sprint por estar terminada, vuelve al backlog o pasa a otro sprint). Exige VER.
 */
class SprintCierreContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String HECHO = "a2000000-0000-4000-8000-000000000015";

    @Test
    void guardaQuienCerroYAdondeFueCadaTarea() {
        String proyecto = proyecto("SCA");
        String tablero = tableroPrincipal(proyecto);
        String hecha = tarea(tablero, "Terminada", 3);
        String pendiente = tarea(tablero, "Pendiente", 5);
        como(ADMIN).post("/tarea/{uuid}/mover", hecha).json(Map.of("estadoUuid", HECHO)).preparacion().ejecutar().esperar(200);
        String sprint = sprint(proyecto, "Sprint A");
        String siguiente = sprint(proyecto, "Sprint B");
        accion(sprint, "asignar", Map.of("tareas", List.of(hecha, pendiente)));
        accion(sprint, "iniciar", null);
        accion(sprint, "cerrar", Map.of("destino", "SPRINT", "sprintDestinoUuid", siguiente));

        JsonNode cierre = como(ADMIN).get("/sprint/{uuid}/cierre", sprint).ejecutar().esperar(200).json();

        assertFalse(cierre.get("cerradoPorNombre").isNull());
        assertFalse(cierre.get("fechaDeCierre").isNull());
        assertEquals(2, cierre.get("tareas").size());
        JsonNode terminada = porUuid(cierre.get("tareas"), hecha);
        assertTrue(terminada.get("terminada").asBoolean());
        assertEquals("TERMINADA", terminada.get("destino").asString());
        assertEquals("Hecho", terminada.get("estadoAlCerrar").asString());
        assertEquals(3, terminada.get("puntos").asInt());
        JsonNode movida = porUuid(cierre.get("tareas"), pendiente);
        assertFalse(movida.get("terminada").asBoolean());
        assertEquals("SPRINT", movida.get("destino").asString());
        assertEquals("Sprint B", movida.get("sprintDestinoNombre").asString());
        assertEquals("Pendiente", movida.get("titulo").asString());
        assertEquals("PRINCIPAL", movida.get("tableroCodigo").asString());

        JsonNode dto = como(ADMIN).get("/sprint/uuid/{uuid}", sprint).ejecutar().esperar(200).json();
        assertEquals(cierre.get("cerradoPorNombre").asString(), dto.get("cerradoPorNombre").asString());
    }

    @Test
    void loPendienteQueVuelveAlBacklogQuedaRegistrado() {
        String proyecto = proyecto("SCB");
        String pendiente = tarea(tableroPrincipal(proyecto), "Al backlog", null);
        String sprint = sprint(proyecto, "Sprint A");
        accion(sprint, "asignar", Map.of("tareas", List.of(pendiente)));
        accion(sprint, "iniciar", null);
        accion(sprint, "cerrar", Map.of("destino", "BACKLOG"));

        JsonNode tarea = como(ADMIN).get("/sprint/{uuid}/cierre", sprint).ejecutar().esperar(200).json().get("tareas").get(0);

        assertEquals("BACKLOG", tarea.get("destino").asString());
        assertTrue(tarea.get("sprintDestinoNombre").isNull());
    }

    @Test
    void unSprintSinCerrarNoTieneCierre() {
        String sprint = sprint(proyecto("SCC"), "Sprint A");

        JsonNode cierre = como(ADMIN).get("/sprint/{uuid}/cierre", sprint).ejecutar().esperar(200).json();

        assertTrue(cierre.get("fechaDeCierre").isNull());
        assertEquals(0, cierre.get("tareas").size());
    }

    @Test
    void sinVerElProyectoNoSeLee() {
        String sprint = sprint(proyecto("SCD"), "Sprint A");

        como("CLIENTE").get("/sprint/{uuid}/cierre", sprint).ejecutar().esperarError(403, "SIN_PERMISO");
    }

    @Test
    void sprintInexistente() {
        como(ADMIN).get("/sprint/{uuid}/cierre", "b6999999-0000-4000-8000-000000000999").ejecutar()
                .esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    private String proyecto(String prefijo) {
        return como(ADMIN).post("/proyecto/guardar").json(Map.of("nombre", "Proyecto " + prefijo, "prefijo", prefijo))
                .preparacion().ejecutar().esperar(200).json().get("uuid").asString();
    }

    private String tableroPrincipal(String proyecto) {
        for (JsonNode tablero : como(ADMIN).get("/proyecto/uuid/{uuid}", proyecto).preparacion().ejecutar().esperar(200)
                .json().get("tableros")) {
            if ("PRINCIPAL".equals(tablero.get("tipoTableroCodigo").asString()))
                return tablero.get("uuid").asString();
        }
        throw new IllegalStateException("Sin tablero principal");
    }

    private String tarea(String tablero, String titulo, Integer puntos) {
        Map<String, Object> cuerpo = new HashMap<>(Map.of("tableroUuid", tablero, "tipoCodigo", "TAREA", "titulo", titulo));
        if (puntos != null)
            cuerpo.put("puntos", puntos);
        return como(ADMIN).post("/tarea/guardar").json(cuerpo).preparacion().ejecutar().esperar(200).json().get("uuid").asString();
    }

    private String sprint(String proyecto, String nombre) {
        return como(ADMIN).post("/sprint/guardar").json(Map.of("proyectoUuid", proyecto, "nombre", nombre, "inicio", "2026-12-01",
                "fin", "2026-12-14")).preparacion().ejecutar().esperar(200).json().get("uuid").asString();
    }

    private void accion(String sprint, String accion, Map<String, Object> cuerpo) {
        var peticion = como(ADMIN).post("/sprint/{uuid}/" + accion, sprint);
        (cuerpo == null ? peticion : peticion.json(cuerpo)).preparacion().ejecutar().esperar(200);
    }

    private static JsonNode porUuid(JsonNode tareas, String uuid) {
        for (JsonNode tarea : tareas) {
            if (uuid.equals(tarea.get("uuid").asString()))
                return tarea;
        }
        throw new IllegalStateException("Sin la tarea " + uuid);
    }
}
