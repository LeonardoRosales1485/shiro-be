package com.blue.open.Code.Tarea;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-TAR-06: archivar una tarea (POST /tarea/{uuid}/archivar). T1.8 del plan, Spec 7 y adenda 3 del DAF: nada se
 * borra; archivar una tarea archiva sus hijas en todos los niveles con el mismo id de operacion, y cada una deja su
 * fila ARCHIVAR en el historial.
 */
class TareaArchivarContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String EN_CURSO = "a2000000-0000-4000-8000-000000000012";

    private final EscenarioDeTarea escenario = new EscenarioDeTarea(this::como);

    @Test
    void archivarUnaEpicaArchivaSusHijasEnTodosLosNiveles() {
        JsonNode epica = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "EPICA", "Épica");
        JsonNode historia = escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia", uuid(epica));
        JsonNode subtarea = escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Subtarea", uuid(historia));

        JsonNode archivada = archivar(uuid(epica)).esperar(200).json();

        assertEquals(-1, archivada.get("status").asInt());
        for (JsonNode tarea : new JsonNode[] {epica, historia, subtarea}) {
            mover(uuid(tarea)).esperarError(400, "REGISTRO_NO_ENCONTRADO");
            JsonNode ultima = como(ADMIN).get("/tarea/{uuid}/historial", uuid(tarea)).ejecutar().esperar(200).json().get(0);
            assertEquals("ARCHIVAR", ultima.get("accion").asString());
            assertEquals(epica.get("clave").asString(), ultima.get("valorNuevo").asString());
        }
    }

    @Test
    void unaSubtareaNoSeCreaDentroDeUnPadreArchivado() {
        JsonNode historia = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia");
        archivar(uuid(historia)).esperar(200);

        Map<String, Object> cuerpo = EscenarioDeTarea.rapida(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Subtarea");
        cuerpo.put("padreUuid", uuid(historia));
        como(ADMIN).post("/tarea/guardar").json(cuerpo).ejecutar().esperarError(400, "RELACION_NO_ENCONTRADA");
    }

    @Test
    void archivarDosVecesRechazado() {
        String uuid = uuid(escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Tarea"));
        archivar(uuid).esperar(200);

        archivar(uuid).esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    @Test
    void tareaInexistenteRechazada() {
        archivar(TareasDeSeed.TABLERO_INEXISTENTE).esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    // POST /tarea/{uuid}/archivar como Administrador
    private Respuesta archivar(String uuid) {
        return como(ADMIN).post("/tarea/{uuid}/archivar", uuid).ejecutar();
    }

    // POST /tarea/{uuid}/mover a En curso: solo funciona con una tarea vigente
    private Respuesta mover(String uuid) {
        return como(ADMIN).post("/tarea/{uuid}/mover", uuid).json(Map.of("estadoUuid", EN_CURSO)).ejecutar();
    }

    private static String uuid(JsonNode tarea) {
        return tarea.get("uuid").asString();
    }
}
