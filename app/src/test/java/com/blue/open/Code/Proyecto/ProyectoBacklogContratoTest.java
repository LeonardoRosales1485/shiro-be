package com.blue.open.Code.Proyecto;

import com.blue.open.Code.Tarea.EscenarioDeTarea;
import com.blue.open.Code.Tarea.TareasDeSeed;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CU-BKL-01 y CU-BKL-02: backlog del proyecto (GET /proyecto/{uuid}/backlog) y su orden (POST /proyecto/{uuid}/ordenar-backlog).
 * T5.1 del plan y seccion 4 del DAF: el backlog tiene las tareas sin sprint de todos los tableros (todos los tipos: epicas, historias, tareas, bugs y subtareas, resolucion B21 del 19/11; antes sin epicas ni
 * subtareas) con un unico orden, el mismo para todos. Leer exige VER en el proyecto y ordenar, EDITAR.
 */
class ProyectoBacklogContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String PROYECTO_SHIRO = "b1000000-0000-4000-8000-000000000001";

    private final EscenarioDeTarea escenario = new EscenarioDeTarea(this::como);

    @Test
    void elOrdenSeConservaEntreUsuarios() {
        String primera = clave(escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Primera del backlog"));
        String segunda = clave(escenario.crear(TareasDeSeed.TABLERO_QA, "BUG", "Segunda del backlog"));
        String tercera = clave(escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Tercera del backlog"));
        List<String> nuevas = List.of(primera, segunda, tercera);
        assertEquals(nuevas, delBacklog(backlog(ADMIN), nuevas));

        ordenar(List.of(uuid(tercera), uuid(primera))).esperar(200);

        assertEquals(List.of(tercera, primera, segunda), delBacklog(backlog(ADMIN), nuevas));
        assertEquals(List.of(tercera, primera, segunda), delBacklog(backlog("CLIENTE"), nuevas));
    }

    @Test
    void tieneTodosLosTipos() {
        JsonNode epica = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "EPICA", "Épica en el backlog");
        JsonNode historia = escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia en el backlog",
                epica.get("uuid").asString());
        JsonNode subtarea = escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Subtarea en el backlog",
                historia.get("uuid").asString());

        List<String> claves = claves(backlog(ADMIN));

        assertTrue(claves.contains(clave(epica)));
        assertTrue(claves.contains(clave(subtarea)));
        assertTrue(claves.contains(clave(historia)));
    }

    @Test
    void unaTareaDeOtroProyectoNoSeOrdena() {
        String tableroAjeno = escenario.crearProyectoYDevolverTablero("BKA", "PRINCIPAL");
        String ajena = escenario.crear(tableroAjeno, "TAREA", "Ajena").get("uuid").asString();

        ordenar(List.of(ajena)).esperarError(400, "TAREA_FUERA_DEL_BACKLOG");
    }

    @Test
    void sinEditarElProyectoNoSeOrdena() {
        como("CLIENTE").post("/proyecto/{uuid}/ordenar-backlog", PROYECTO_SHIRO).json(Map.of("tareas",
                List.of(TareasDeSeed.SHI_1))).ejecutar().esperarError(403, "SIN_PERMISO");
    }

    @Test
    void proyectoInexistente() {
        como(ADMIN).get("/proyecto/{uuid}/backlog", TareasDeSeed.TABLERO_INEXISTENTE).ejecutar()
                .esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    // GET /proyecto/{uuid}/backlog de Shiro como el actor del rol
    private JsonNode backlog(String rol) {
        return como(rol).get("/proyecto/{uuid}/backlog", PROYECTO_SHIRO).ejecutar().esperar(200).json();
    }

    // POST /proyecto/{uuid}/ordenar-backlog de Shiro como Administrador
    private Respuesta ordenar(List<String> tareas) {
        return como(ADMIN).post("/proyecto/{uuid}/ordenar-backlog", PROYECTO_SHIRO).json(Map.of("tareas", tareas)).ejecutar();
    }

    // Claves del backlog que estan en la lista recibida, en el orden del backlog
    private static List<String> delBacklog(JsonNode backlog, List<String> buscadas) {
        return claves(backlog).stream().filter(buscadas::contains).toList();
    }

    private static List<String> claves(JsonNode backlog) {
        List<String> claves = new ArrayList<>();
        backlog.forEach(tarea -> claves.add(tarea.get("clave").asString()));
        return claves;
    }

    private String uuid(String clave) {
        for (JsonNode tarea : backlog(ADMIN)) {
            if (clave.equals(tarea.get("clave").asString()))
                return tarea.get("uuid").asString();
        }
        throw new IllegalStateException("No esta en el backlog: " + clave);
    }

    private static String clave(JsonNode tarea) {
        return tarea.get("clave").asString();
    }
}
