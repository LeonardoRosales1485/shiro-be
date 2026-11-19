package com.blue.open.Code.Tarea;

import com.blue.open.Code.Usuario.UsuariosDeSeed;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-TAR-05: historial de una tarea (GET /tarea/{uuid}/historial). T1.6 del plan, Spec 1 y adenda 3 del DAF: cada
 * escritura deja su fila en la misma transaccion, con usuario, canal y tipo de accion; el alta de una subtarea queda
 * tambien en el historial del padre; se puede filtrar por canal.
 */
class TareaHistorialContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String EN_CURSO = "a2000000-0000-4000-8000-000000000012";

    private final EscenarioDeTarea escenario = new EscenarioDeTarea(this::como);

    @Test
    void cadaEscrituraDejaSuFilaConUsuarioCanalYAccion() {
        JsonNode tarea = como(ADMIN).post("/tarea/guardar")
                .json(EscenarioDeTarea.rapida(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Borrador"))
                .ejecutar().esperar(200).json();
        String uuid = tarea.get("uuid").asString();
        Map<String, Object> edicion = EscenarioDeTarea.rapida(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Definitivo");
        edicion.put("uuid", uuid);
        edicion.put("prioridad", "ALTA");
        edicion.put("fechaDeActualizacion", tarea.get("fechaDeActualizacion").asString());
        como(ADMIN).post("/tarea/guardar").json(edicion).ejecutar().esperar(200);
        como(ADMIN).post("/tarea/{uuid}/mover", uuid).json(Map.of("estadoUuid", EN_CURSO)).ejecutar().esperar(200);

        JsonNode historial = historial(uuid, null);

        assertEquals(List.of("MOVER estado", "EDITAR prioridad", "EDITAR titulo", "CREAR -"), resumen(historial));
        JsonNode movida = historial.get(0);
        assertEquals("Por hacer", movida.get("valorAnterior").asString());
        assertEquals("En curso", movida.get("valorNuevo").asString());
        JsonNode titulo = historial.get(2);
        assertEquals("Borrador", titulo.get("valorAnterior").asString());
        assertEquals("Definitivo", titulo.get("valorNuevo").asString());
        for (JsonNode fila : historial) {
            assertEquals(UsuariosDeSeed.DEMO, fila.get("usuarioUuid").asString());
            assertEquals("WEB", fila.get("canal").asString());
        }
    }

    @Test
    void elAltaDeUnaSubtareaQuedaEnElHistorialDelPadre() {
        JsonNode historia = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia");
        JsonNode subtarea = escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Subtarea",
                historia.get("uuid").asString());

        JsonNode historial = historial(historia.get("uuid").asString(), null);

        assertEquals("CREAR_SUBTAREA", historial.get(0).get("accion").asString());
        assertEquals(subtarea.get("clave").asString(), historial.get(0).get("valorNuevo").asString());
    }

    @Test
    void seFiltraPorCanal() {
        String uuid = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Filtro").get("uuid").asString();

        assertEquals(1, historial(uuid, "WEB").size());
        assertTrue(historial(uuid, "MCP").isEmpty());
    }

    @Test
    void tareaInexistenteRechazada() {
        como(ADMIN).get("/tarea/{uuid}/historial", TareasDeSeed.TABLERO_INEXISTENTE).ejecutar()
                .esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    // GET /tarea/{uuid}/historial, con el filtro de canal si no es null
    private JsonNode historial(String uuid, String canal) {
        String ruta = canal == null ? "/tarea/{uuid}/historial" : "/tarea/{uuid}/historial?canal=" + canal;
        return como(ADMIN).get(ruta, uuid).ejecutar().esperar(200).json();
    }

    // "ACCION campo" de cada fila, en el orden del historial (la mas nueva primero)
    private static List<String> resumen(JsonNode historial) {
        List<String> filas = new ArrayList<>();
        historial.forEach(fila -> filas.add(fila.get("accion").asString() + " "
                + (fila.get("campo").isNull() ? "-" : fila.get("campo").asString())));
        return filas;
    }
}
