package com.blue.open.Code.Tarea;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-TAR-07: restaurar una tarea archivada (POST /tarea/{uuid}/restaurar). T1.8 del plan, Spec 7 y seccion 4 del
 * DAF: vuelve con lo que se archivo en esa misma operacion, no con lo archivado antes por separado; una tarea con el
 * padre archivado no se restaura sola.
 */
class TareaRestaurarContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String EN_CURSO = "a2000000-0000-4000-8000-000000000012";

    private final EscenarioDeTarea escenario = new EscenarioDeTarea(this::como);

    @Test
    void restaurarDevuelveSoloLoDeEsaOperacion() {
        JsonNode historia = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia");
        JsonNode archivadaAntes = escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Antes", uuid(historia));
        JsonNode conLaHistoria = escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Con la historia",
                uuid(historia));
        archivar(uuid(archivadaAntes));
        archivar(uuid(historia));

        JsonNode restaurada = restaurar(uuid(historia)).esperar(200).json();

        assertEquals(0, restaurada.get("status").asInt());
        assertEquals(List.of(conLaHistoria.get("clave").asString()), clavesDeHijas(restaurada));
        mover(uuid(conLaHistoria)).esperar(200);
        mover(uuid(archivadaAntes)).esperarError(400, "REGISTRO_NO_ENCONTRADO");
        JsonNode ultima = como(ADMIN).get("/tarea/{uuid}/historial", uuid(conLaHistoria)).ejecutar().esperar(200)
                .json().get(1);
        assertEquals("RESTAURAR", ultima.get("accion").asString());
    }

    @Test
    void subtareaConElPadreArchivadoRechazada() {
        JsonNode historia = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia");
        JsonNode subtarea = escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Subtarea", uuid(historia));
        archivar(uuid(historia));

        JsonNode error = restaurar(uuid(subtarea)).esperarError(400, "PADRE_ARCHIVADO").json();

        assertEquals("Restaurá primero " + historia.get("clave").asString() + ".",
                error.get("mensajeParaUsuario").asString());
    }

    @Test
    void unaTareaVigenteNoSeRestaura() {
        restaurar(TareasDeSeed.SHI_1).esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    // POST /tarea/{uuid}/archivar como Administrador (paso de preparacion)
    private void archivar(String uuid) {
        como(ADMIN).post("/tarea/{uuid}/archivar", uuid).preparacion().ejecutar().esperar(200);
    }

    // POST /tarea/{uuid}/restaurar como Administrador
    private Respuesta restaurar(String uuid) {
        return como(ADMIN).post("/tarea/{uuid}/restaurar", uuid).ejecutar();
    }

    // POST /tarea/{uuid}/mover a En curso: solo funciona con una tarea vigente
    private Respuesta mover(String uuid) {
        return como(ADMIN).post("/tarea/{uuid}/mover", uuid).json(Map.of("estadoUuid", EN_CURSO)).ejecutar();
    }

    // Claves de las hijas vigentes de la tarea
    private static List<String> clavesDeHijas(JsonNode tarea) {
        List<String> claves = new ArrayList<>();
        tarea.get("hijas").forEach(hija -> claves.add(hija.get("clave").asString()));
        return claves;
    }

    private static String uuid(JsonNode tarea) {
        return tarea.get("uuid").asString();
    }
}
