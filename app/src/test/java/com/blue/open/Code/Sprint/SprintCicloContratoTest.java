package com.blue.open.Code.Sprint;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-SPR-04 a CU-SPR-08: ciclo de un sprint (T5.2 del plan, Spec 3 y Spec 7). Un sprint se crea planificado, recibe
 * tareas del backlog, se inicia (uno solo activo por proyecto) y se cierra mandando lo que no se termino al backlog o
 * a otro sprint; lo terminado queda en el sprint cerrado. Un sprint activo no se archiva: se cierra primero.
 */
class SprintCicloContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String TABLERO_PRINCIPAL = "b2000000-0000-4000-8000-000000000001";

    private static final String HECHO = "a2000000-0000-4000-8000-000000000015";

    @Test
    void crearAsignarIniciarYCerrarAlBacklog() {
        String proyecto = proyecto("SPA");
        String tablero = tableroPrincipal(proyecto);
        String terminada = tarea(tablero, "Terminada");
        String pendiente = tarea(tablero, "Pendiente");
        String sprint = crear(proyecto, "Sprint A").get("uuid").asString();
        assertEquals("PLANIFICADO", obtener(sprint).get("estado").asString());

        accion(sprint, "asignar", Map.of("tareas", List.of(terminada, pendiente))).esperar(200);
        assertFalse(backlog(proyecto).contains(terminada));
        assertEquals("ACTIVO", accion(sprint, "iniciar", null).esperar(200).json().get("estado").asString());
        moverAHecho(terminada, tablero);

        assertEquals("CERRADO", accion(sprint, "cerrar", Map.of("destino", "BACKLOG")).esperar(200).json()
                .get("estado").asString());

        assertTrue(backlog(proyecto).contains(pendiente));
        assertFalse(backlog(proyecto).contains(terminada));
        assertEquals(sprint, tareaDto(terminada).get("sprintUuid").asString());
        assertTrue(tareaDto(pendiente).get("sprintUuid").isNull());
    }

    @Test
    void cerrarMandandoLoPendienteAOtroSprint() {
        String proyecto = proyecto("SPB");
        String pendiente = tarea(tableroPrincipal(proyecto), "Pendiente");
        String actual = crear(proyecto, "Actual").get("uuid").asString();
        String siguiente = crear(proyecto, "Siguiente").get("uuid").asString();
        accion(actual, "asignar", Map.of("tareas", List.of(pendiente))).esperar(200);
        accion(actual, "iniciar", null).esperar(200);

        accion(actual, "cerrar", Map.of("destino", "SPRINT", "sprintDestinoUuid", siguiente)).esperar(200);

        assertEquals(siguiente, tareaDto(pendiente).get("sprintUuid").asString());
    }

    @Test
    void unSoloSprintActivoPorProyecto() {
        String proyecto = proyecto("SPC");
        String primero = crear(proyecto, "Primero").get("uuid").asString();
        String segundo = crear(proyecto, "Segundo").get("uuid").asString();
        accion(primero, "iniciar", null).esperar(200);

        JsonNode error = accion(segundo, "iniciar", null).esperarError(400, "SPRINT_ACTIVO_EXISTENTE").json();

        assertEquals("Ya hay un sprint activo en el proyecto: Primero. Cerralo antes de iniciar otro.",
                error.get("mensajeParaUsuario").asString());
    }

    @Test
    void unSprintActivoNoSeArchivaYUnoPlanificadoDevuelveSusTareas() {
        String proyecto = proyecto("SPD");
        String tarea = tarea(tableroPrincipal(proyecto), "En el sprint");
        String activo = crear(proyecto, "Activo").get("uuid").asString();
        String planificado = crear(proyecto, "Planificado").get("uuid").asString();
        accion(activo, "iniciar", null).esperar(200);
        accion(planificado, "asignar", Map.of("tareas", List.of(tarea))).esperar(200);

        accion(activo, "archivar", null).esperarError(400, "SPRINT_ACTIVO_NO_SE_ARCHIVA");
        assertEquals(-1, accion(planificado, "archivar", null).esperar(200).json().get("status").asInt());
        assertTrue(backlog(proyecto).contains(tarea));
    }

    @Test
    void reglasDeAsignarYCerrar() {
        String proyecto = proyecto("SPE");
        String sprint = crear(proyecto, "Sprint").get("uuid").asString();
        String deOtroProyecto = tarea(TABLERO_PRINCIPAL, "De Shiro");

        accion(sprint, "asignar", Map.of("tareas", List.of(deOtroProyecto))).esperarError(400, "TAREA_FUERA_DEL_BACKLOG");
        accion(sprint, "cerrar", Map.of("destino", "BACKLOG")).esperarError(400, "SPRINT_NO_ACTIVO");
        guardar(Map.of("proyectoUuid", proyecto, "nombre", "Al revés", "inicio", "2026-12-14", "fin", "2026-12-01"))
                .esperarError(400, "SPRINT_FECHAS_INVALIDAS");
        String tarea = tarea(tableroPrincipal(proyecto), "Para quitar");
        accion(sprint, "asignar", Map.of("tareas", List.of(tarea))).esperar(200);
        accion(sprint, "quitar", Map.of("tareas", List.of(tarea))).esperar(200);
        assertTrue(backlog(proyecto).contains(tarea));
    }

    @Test
    void sinEditarElProyectoNoSeCreaUnSprint() {
        como("CLIENTE").post("/sprint/guardar").json(Map.of("proyectoUuid", SprintContratoTest.PROYECTO_SHIRO,
                "nombre", "Sin permiso", "inicio", "2026-12-01", "fin", "2026-12-14")).ejecutar()
                .esperarError(403, "SIN_PERMISO");
    }

    private Respuesta guardar(Map<String, Object> cuerpo) {
        return como(ADMIN).post("/sprint/guardar").json(cuerpo).ejecutar();
    }

    private JsonNode crear(String proyecto, String nombre) {
        return guardar(Map.of("proyectoUuid", proyecto, "nombre", nombre, "inicio", "2026-12-01", "fin", "2026-12-14"))
                .esperar(200).json();
    }

    private JsonNode obtener(String sprint) {
        return como(ADMIN).get("/sprint/uuid/{uuid}", sprint).ejecutar().esperar(200).json();
    }

    // POST /sprint/{uuid}/{accion} con el cuerpo recibido (o sin cuerpo)
    private Respuesta accion(String sprint, String accion, Map<String, Object> cuerpo) {
        var peticion = como(ADMIN).post("/sprint/{uuid}/" + accion, sprint);
        return (cuerpo == null ? peticion : peticion.json(cuerpo)).ejecutar();
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

    private String tarea(String tablero, String titulo) {
        Map<String, Object> cuerpo = new HashMap<>(Map.of("tableroUuid", tablero, "tipoCodigo", "TAREA", "titulo", titulo));
        return como(ADMIN).post("/tarea/guardar").json(cuerpo).preparacion().ejecutar().esperar(200).json()
                .get("uuid").asString();
    }

    private void moverAHecho(String tarea, String tablero) {
        como(ADMIN).post("/tarea/{uuid}/mover", tarea).json(Map.of("estadoUuid", HECHO)).preparacion().ejecutar().esperar(200);
    }

    private JsonNode tareaDto(String tarea) {
        return como(ADMIN).get("/tarea/uuid/{uuid}", tarea).preparacion().ejecutar().esperar(200).json();
    }

    // Uuids de las tareas del backlog del proyecto
    private List<String> backlog(String proyecto) {
        List<String> uuids = new ArrayList<>();
        como(ADMIN).get("/backlog/proyecto/{uuid}", proyecto).preparacion().ejecutar().esperar(200).json()
                .forEach(tarea -> uuids.add(tarea.get("uuid").asString()));
        return uuids;
    }
}
