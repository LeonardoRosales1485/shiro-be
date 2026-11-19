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
 * "texto".
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
