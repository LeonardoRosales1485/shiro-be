package com.blue.open.Code.Tarea;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-TAR-03, edicion simultanea (T1.7 del plan y Spec 7): guardar recibe la fecha de actualizacion que leyo el
 * cliente; si la tarea cambio desde entonces responde 409 para que recargue.
 */
class TareaEdicionSimultaneaContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String EN_CURSO = "a2000000-0000-4000-8000-000000000012";

    private final EscenarioDeTarea escenario = new EscenarioDeTarea(this::como);

    @Test
    void dosGuardadosConLaMismaFechaLeida() {
        JsonNode leida = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Original");

        guardar(edicion(leida, "Primera edición")).esperar(200);
        JsonNode error = guardar(edicion(leida, "Segunda edición")).esperarError(409, "TAREA_MODIFICADA").json();

        assertEquals("La tarea cambió mientras la editabas. Recargá y volvé a intentar.",
                error.get("mensajeParaUsuario").asString());
        assertEquals("Primera edición", escenario.obtener(leida.get("uuid").asString()).get("titulo").asString());
    }

    @Test
    void conLaFechaNuevaSePuedeVolverAEditar() {
        JsonNode leida = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Original");
        JsonNode editada = guardar(edicion(leida, "Primera edición")).esperar(200).json();

        assertEquals("Segunda edición", guardar(edicion(editada, "Segunda edición")).esperar(200).json()
                .get("titulo").asString());
    }

    @Test
    void moverTambienCambiaLaFecha() {
        JsonNode leida = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Original");
        como(ADMIN).post("/tarea/{uuid}/mover", leida.get("uuid").asString()).json(Map.of("estadoUuid", EN_CURSO))
                .ejecutar().esperar(200);

        guardar(edicion(leida, "Edición vieja")).esperarError(409, "TAREA_MODIFICADA");
    }

    @Test
    void editarSinFechaLeidaRechazado() {
        JsonNode leida = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Original");
        Map<String, Object> cuerpo = edicion(leida, "Sin fecha");
        cuerpo.remove("fechaDeActualizacion");

        guardar(cuerpo).esperarError(400, "FECHA_DE_LECTURA_OBLIGATORIA");
    }

    // POST /tarea/guardar como Administrador
    private Respuesta guardar(Map<String, Object> cuerpo) {
        return como(ADMIN).post("/tarea/guardar").json(cuerpo).ejecutar();
    }

    // Body de edicion del titulo de la tarea leida, con su fecha de actualizacion
    private static Map<String, Object> edicion(JsonNode leida, String titulo) {
        Map<String, Object> cuerpo = EscenarioDeTarea.rapida(leida.get("tableroUuid").asString(),
                leida.get("tipoCodigo").asString(), titulo);
        cuerpo.put("uuid", leida.get("uuid").asString());
        cuerpo.put("fechaDeActualizacion", leida.get("fechaDeActualizacion").asString());
        return cuerpo;
    }
}
