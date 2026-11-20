package com.blue.open.Code.Tarea;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-TAR-02 con los parametros de la busqueda del front (T6.4): "version" (uuid) deja las tareas asignadas a esa
 * version y "etiqueta" las que tienen esa etiqueta (sin distinguir mayusculas). Se combinan con los filtros y con
 * "texto". Tambien prueba los filtros que usa la busqueda: tipo de tablero, tipo de tarea, estado y responsable.
 */
class TareaFiltrosContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private final EscenarioDeTarea escenario = new EscenarioDeTarea(this::como);

    @Test
    void filtraPorVersionYPorEtiqueta() {
        String tablero = escenario.crearProyectoYDevolverTablero("TFA", "PRINCIPAL");
        JsonNode conEtiqueta = como(ADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", tablero, "tipoCodigo", "TAREA",
                "titulo", "Con etiqueta", "etiquetas", List.of(Map.of("texto", "Importador")))).preparacion().ejecutar().esperar(200).json();
        JsonNode enVersion = escenario.crear(tablero, "TAREA", "En la versión");
        escenario.crear(tablero, "TAREA", "Ninguna");
        String proyecto = como(ADMIN).get("/tarea/uuid/{uuid}", enVersion.get("uuid").asString()).preparacion().ejecutar().esperar(200)
                .json().get("proyectoUuid").asString();
        String version = como(ADMIN).post("/version/guardar").json(Map.of("proyectoUuid", proyecto, "nombre", "1.0")).preparacion()
                .ejecutar().esperar(200).json().get("uuid").asString();
        como(ADMIN).post("/version/{uuid}/asignar", version).json(Map.of("tareas", List.of(enVersion.get("uuid").asString())))
                .preparacion().ejecutar().esperar(200);

        assertEquals(List.of(enVersion.get("clave").asString()), claves(proyecto, "version", version));
        assertEquals(List.of(conEtiqueta.get("clave").asString()), claves(proyecto, "etiqueta", "importador"));
        assertEquals(List.of(), claves(proyecto, "etiqueta", "otra"));
    }

    @Test
    void filtraPorTipoDeTableroTipoDeTareaEstadoYResponsable() {
        String tablero = escenario.crearProyectoYDevolverTablero("TFB", "PRINCIPAL");
        JsonNode bug = escenario.crear(tablero, "BUG", "Un bug");
        escenario.crear(tablero, "TAREA", "Una tarea");
        String proyecto = como(ADMIN).get("/tarea/uuid/{uuid}", bug.get("uuid").asString()).preparacion().ejecutar().esperar(200)
                .json().get("proyectoUuid").asString();
        String estado = como(ADMIN).get("/tarea/uuid/{uuid}", bug.get("uuid").asString()).preparacion().ejecutar().esperar(200)
                .json().get("estadoUuid").asString();

        assertEquals(List.of(bug.get("clave").asString()), clavesCon(proyecto, "tipo.codigo", "BUG"));
        assertEquals(2, clavesCon(proyecto, "tablero.tipoTablero.codigo", "PRINCIPAL").size());
        assertEquals(List.of(), clavesCon(proyecto, "tablero.tipoTablero.codigo", "QA"));
        assertEquals(2, clavesCon(proyecto, "estado.uuid", estado).size());
        assertEquals(List.of(), clavesCon(proyecto, "responsable.uuid", "no-existe"));
    }

    @Test
    void variosValoresDeUnFiltroSeCombinanConOYSinSprintEsNulo() {
        String tablero = escenario.crearProyectoYDevolverTablero("TFC", "PRINCIPAL");
        JsonNode bug = escenario.crear(tablero, "BUG", "Un bug");
        JsonNode historia = escenario.crear(tablero, "HISTORIA", "Una historia");
        escenario.crear(tablero, "TAREA", "Una tarea");
        String proyecto = como(ADMIN).get("/tarea/uuid/{uuid}", bug.get("uuid").asString()).preparacion().ejecutar().esperar(200)
                .json().get("proyectoUuid").asString();

        JsonNode pagina = como(ADMIN).post("/tarea/paginar").json(Map.of("pagina", 1, "tamano", 50, "filtros", List.of(
                List.of(Map.of("campo", "proyecto.uuid", "operacion", "=", "valor", proyecto)),
                List.of(Map.of("campo", "tipo.codigo", "operacion", "=", "valor", "BUG"), Map.of("campo", "tipo.codigo", "operacion", "=", "valor", "HISTORIA")),
                List.of(Map.of("campo", "sprint.uuid", "operacion", "is null", "valor", ""))))).ejecutar().esperar(200).json();

        List<String> claves = new ArrayList<>();
        pagina.get("contenido").forEach(tarea -> claves.add(tarea.get("clave").asString()));
        assertEquals(List.of(bug.get("clave").asString(), historia.get("clave").asString()).stream().sorted().toList(), claves.stream().sorted().toList());
    }

    // Claves de las tareas del proyecto con un filtro mas
    private List<String> clavesCon(String proyecto, String campo, String valor) {
        JsonNode pagina = como(ADMIN).post("/tarea/paginar").json(Map.of("pagina", 1, "tamano", 50,
                "filtros", List.of(List.of(Map.of("campo", "proyecto.uuid", "operacion", "=", "valor", proyecto)),
                        List.of(Map.of("campo", campo, "operacion", "=", "valor", valor))))).ejecutar().esperar(200).json();
        List<String> claves = new ArrayList<>();
        pagina.get("contenido").forEach(tarea -> claves.add(tarea.get("clave").asString()));
        return claves;
    }

    // Claves de las tareas del proyecto con el parametro recibido
    private List<String> claves(String proyecto, String parametro, String valor) {
        JsonNode pagina = como(ADMIN).post("/tarea/paginar").json(Map.of("pagina", 1, "tamano", 50,
                "filtros", List.of(List.of(Map.of("campo", "proyecto.uuid", "operacion", "=", "valor", proyecto))),
                "parametros", List.of(Map.of("nombre", parametro, "valor", valor)))).ejecutar().esperar(200).json();
        List<String> claves = new ArrayList<>();
        pagina.get("contenido").forEach(tarea -> claves.add(tarea.get("clave").asString()));
        return claves;
    }
}
