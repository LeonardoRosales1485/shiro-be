package com.blue.open.Code.Version;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-VER-04 a CU-VER-09: versiones (T5.3 del plan, Spec 3 y adenda 3 del DAF). A una version se le asignan tareas
 * del proyecto (una tarea puede estar en varias); el changelog en Markdown lista las tareas terminadas agrupadas por
 * tipo; al publicarla se elige la fecha y queda fija: no se edita, no cambian sus tareas y su changelog no cambia.
 */
class VersionCicloContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String HECHO = "a2000000-0000-4000-8000-000000000015";

    @Test
    void changelogAgrupadoPorTipoYFijoAlPublicar() {
        String proyecto = proyecto("VRA");
        String tablero = tableroPrincipal(proyecto);
        JsonNode historia = tarea(tablero, "HISTORIA", "Importar sprints");
        JsonNode bug = tarea(tablero, "BUG", "Token revocado");
        JsonNode pendiente = tarea(tablero, "TAREA", "Sin terminar");
        hecho(historia);
        hecho(bug);
        String version = crear(proyecto, "1.0").get("uuid").asString();
        accion(version, "asignar", Map.of("tareas", List.of(uuid(historia), uuid(bug), uuid(pendiente)))).esperar(200);

        String esperado = "# 1.0\n\nSin publicar.\n\n## Historia\n\n- " + clave(historia) + " Importar sprints\n\n## Bug\n\n- "
                + clave(bug) + " Token revocado\n";
        assertEquals(esperado, changelog(version));

        JsonNode publicada = accion(version, "publicar", Map.of("fecha", "2026-12-20")).esperar(200).json();
        assertEquals("PUBLICADA", publicada.get("estado").asString());
        assertEquals("2026-12-20", publicada.get("fechaDePublicacion").asString());
        hecho(pendiente);
        assertEquals(esperado.replace("Sin publicar.", "Publicada el 2026-12-20."), changelog(version));
    }

    @Test
    void unaVersionPublicadaNoCambia() {
        String proyecto = proyecto("VRB");
        String tarea = uuid(tarea(tableroPrincipal(proyecto), "TAREA", "Tarea"));
        String version = crear(proyecto, "1.0").get("uuid").asString();
        accion(version, "publicar", Map.of()).esperar(200);

        accion(version, "asignar", Map.of("tareas", List.of(tarea))).esperarError(400, "VERSION_PUBLICADA");
        accion(version, "publicar", Map.of()).esperarError(400, "VERSION_PUBLICADA");
        como(ADMIN).post("/version/guardar").json(Map.of("uuid", version, "proyectoUuid", proyecto, "nombre", "1.0.1"))
                .ejecutar().esperarError(400, "VERSION_PUBLICADA");
    }

    @Test
    void unaTareaPuedeEstarEnVariasVersionesDeSuProyecto() {
        String proyecto = proyecto("VRC");
        JsonNode tarea = tarea(tableroPrincipal(proyecto), "TAREA", "En dos versiones");
        String primera = crear(proyecto, "1.0").get("uuid").asString();
        String segunda = crear(proyecto, "1.1").get("uuid").asString();

        accion(primera, "asignar", Map.of("tareas", List.of(uuid(tarea)))).esperar(200);
        accion(segunda, "asignar", Map.of("tareas", List.of(uuid(tarea)))).esperar(200);

        JsonNode versiones = como(ADMIN).get("/tarea/uuid/{uuid}", uuid(tarea)).ejecutar().esperar(200).json().get("versiones");
        assertEquals(2, versiones.size());
        String deShiro = uuid(tarea(tableroPrincipal(VersionContratoTest.PROYECTO_SHIRO), "TAREA", "De otro proyecto"));
        accion(primera, "asignar", Map.of("tareas", List.of(deShiro))).esperarError(400, "TAREA_DE_OTRO_PROYECTO");
        accion(primera, "quitar", Map.of("tareas", List.of(uuid(tarea)))).esperar(200);
        assertEquals(1, como(ADMIN).get("/tarea/uuid/{uuid}", uuid(tarea)).ejecutar().esperar(200).json().get("versiones").size());
    }

    @Test
    void archivarYRestaurarUnaVersion() {
        String version = crear(proyecto("VRD"), "1.0").get("uuid").asString();

        assertEquals(-1, accion(version, "archivar", null).esperar(200).json().get("status").asInt());
        assertEquals(0, accion(version, "restaurar", null).esperar(200).json().get("status").asInt());
    }

    @Test
    void elGrupoClienteLeeElChangelogPeroNoAsigna() {
        como("CLIENTE").get("/version/{uuid}/changelog", VersionContratoTest.VERSION_DE_SEED).ejecutar().esperar(200);
        como("CLIENTE").post("/version/{uuid}/asignar", VersionContratoTest.VERSION_DE_SEED)
                .json(Map.of("tareas", List.of("b3000000-0000-4000-8000-000000000001"))).ejecutar().esperarError(403, "SIN_PERMISO");
    }

    private JsonNode crear(String proyecto, String nombre) {
        return como(ADMIN).post("/version/guardar").json(Map.of("proyectoUuid", proyecto, "nombre", nombre)).ejecutar()
                .esperar(200).json();
    }

    private Respuesta accion(String version, String accion, Map<String, Object> cuerpo) {
        var peticion = como(ADMIN).post("/version/{uuid}/" + accion, version);
        return (cuerpo == null ? peticion : peticion.json(cuerpo)).ejecutar();
    }

    private String changelog(String version) {
        return como(ADMIN).get("/version/{uuid}/changelog", version).ejecutar().esperar(200).texto();
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

    private JsonNode tarea(String tablero, String tipo, String titulo) {
        Map<String, Object> cuerpo = new HashMap<>(Map.of("tableroUuid", tablero, "tipoCodigo", tipo, "titulo", titulo));
        return como(ADMIN).post("/tarea/guardar").json(cuerpo).preparacion().ejecutar().esperar(200).json();
    }

    private void hecho(JsonNode tarea) {
        como(ADMIN).post("/tarea/{uuid}/mover", uuid(tarea)).json(Map.of("estadoUuid", HECHO)).preparacion().ejecutar().esperar(200);
    }

    private static String uuid(JsonNode tarea) {
        return tarea.get("uuid").asString();
    }

    private static String clave(JsonNode tarea) {
        return tarea.get("clave").asString();
    }
}
