package com.blue.open.Code.Tarea;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-TAR-04: mover una tarea a otro estado de su tablero (POST /tarea/{uuid}/mover). T1.5 del plan: una tarea pasa de
 * cualquier estado a cualquier otro del mismo tablero (seccion 4 del DAF); un estado de otro tablero se rechaza.
 */
class TareaMoverContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    // Estados del tablero principal y de QA (2026-11-18-catalogos.yaml)
    private static final String EN_CURSO = "a2000000-0000-4000-8000-000000000012";

    private static final String HECHO = "a2000000-0000-4000-8000-000000000015";

    private static final String POR_HACER = "a2000000-0000-4000-8000-000000000011";

    private static final String QA_PROBANDO = "a2000000-0000-4000-8000-000000000022";

    private final EscenarioDeTarea escenario = new EscenarioDeTarea(this::como);

    @Test
    void mueveAOtroEstadoDelMismoTablero() {
        JsonNode tarea = mover(TareasDeSeed.SHI_1, EN_CURSO).esperar(200).json();

        assertEquals("En curso", tarea.get("estadoNombre").asString());
        assertEquals(EN_CURSO, tarea.get("estadoUuid").asString());
        assertEquals(TareasDeSeed.SHI_1_CLAVE, tarea.get("clave").asString());
    }

    @Test
    void pasaDeCualquierEstadoACualquierOtro() {
        String uuid = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Ida y vuelta").get("uuid").asString();

        assertEquals("Hecho", mover(uuid, HECHO).esperar(200).json().get("estadoNombre").asString());
        assertEquals("Por hacer", mover(uuid, POR_HACER).esperar(200).json().get("estadoNombre").asString());
    }

    @Test
    void estadoDeOtroTableroRechazado() {
        JsonNode error = mover(TareasDeSeed.SHI_1, QA_PROBANDO).esperarError(400, "ESTADO_DE_OTRO_TABLERO").json();

        assertEquals("El estado no es del tablero de la tarea. Para llevarla a otro tablero, clonala.",
                error.get("mensajeParaUsuario").asString());
    }

    @Test
    void estadoInexistenteRechazado() {
        mover(TareasDeSeed.SHI_1, TareasDeSeed.TABLERO_INEXISTENTE).esperarError(400, "RELACION_NO_ENCONTRADA");
    }

    @Test
    void tareaInexistenteRechazada() {
        mover(TareasDeSeed.TABLERO_INEXISTENTE, EN_CURSO).esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    @Test
    void sinEstadoRechazado() {
        como(ADMIN).post("/tarea/{uuid}/mover", TareasDeSeed.SHI_1).json(Map.of()).ejecutar()
                .esperarError(400, "FORMULARIO_INVALIDO");
    }

    // POST /tarea/{uuid}/mover como Administrador
    private Respuesta mover(String uuid, String estadoUuid) {
        return como(ADMIN).post("/tarea/{uuid}/mover", uuid).json(Map.of("estadoUuid", estadoUuid)).ejecutar();
    }
}
