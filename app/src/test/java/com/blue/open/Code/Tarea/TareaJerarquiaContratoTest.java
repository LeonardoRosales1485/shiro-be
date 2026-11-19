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
 * CU-TAR-03, jerarquia (T1.4 del plan y Spec 7): Epica, despues Historia, Tarea o Bug, y despues Subtarea. El padre
 * tiene un tipo de nivel menor y es del mismo proyecto. No hay subtareas de una subtarea ni Epicas con padre.
 */
class TareaJerarquiaContratoTest extends PruebaDeContrato {

    private static final String SYSADMIN = "SYSADMIN";

    private final EscenarioDeTarea escenario = new EscenarioDeTarea(this::como);

    @Test
    void unaHistoriaDentroDeUnaEpicaYUnaSubtareaDentroDeLaHistoria() {
        JsonNode epica = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "EPICA", "Importador");
        JsonNode historia = guardar(conPadre("HISTORIA", "Importar sprints", epica)).esperar(200).json();
        JsonNode subtarea = guardar(conPadre("SUBTAREA", "Mapear estados", historia)).esperar(200).json();

        assertEquals(epica.get("uuid").asString(), historia.get("padreUuid").asString());
        assertEquals(epica.get("clave").asString(), historia.get("padreClave").asString());
        assertEquals(historia.get("clave").asString(), subtarea.get("padreClave").asString());
        assertEquals(List.of(historia.get("clave").asString()), clavesDeHijas(escenario.obtener(epica.get("uuid").asString())));
    }

    @Test
    void subtareaDeUnaSubtareaRechazada() {
        JsonNode historia = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia");
        JsonNode subtarea = escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Subtarea",
                historia.get("uuid").asString());

        guardar(conPadre("SUBTAREA", "Nieta", subtarea)).esperarError(400, "JERARQUIA_DE_TAREA_INVALIDA");
    }

    @Test
    void epicaConPadreRechazada() {
        JsonNode epica = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "EPICA", "Épica");

        JsonNode error = guardar(conPadre("EPICA", "Otra épica", epica))
                .esperarError(400, "JERARQUIA_DE_TAREA_INVALIDA").json();

        assertEquals("Una Épica no puede ir dentro de una Épica.", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void padreDelMismoNivelRechazado() {
        JsonNode historia = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia");

        guardar(conPadre("BUG", "Bug dentro de historia", historia)).esperarError(400, "JERARQUIA_DE_TAREA_INVALIDA");
    }

    @Test
    void padreDeOtroProyectoRechazado() {
        String tableroAjeno = escenario.crearProyectoYDevolverTablero("AJE", "PRINCIPAL");
        JsonNode epicaAjena = escenario.crear(tableroAjeno, "EPICA", "Épica ajena");

        Map<String, Object> cuerpo = EscenarioDeTarea.rapida(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia");
        cuerpo.put("padreUuid", epicaAjena.get("uuid").asString());

        guardar(cuerpo).esperarError(400, "PADRE_DE_OTRO_PROYECTO");
    }

    @Test
    void padreInexistenteRechazado() {
        Map<String, Object> cuerpo = EscenarioDeTarea.rapida(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia");
        cuerpo.put("padreUuid", TareasDeSeed.TABLERO_INEXISTENTE);

        guardar(cuerpo).esperarError(400, "RELACION_NO_ENCONTRADA");
    }

    @Test
    void cambiarElTipoNoPuedeRomperLaJerarquiaDeSusHijas() {
        JsonNode historia = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia");
        escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Subtarea", historia.get("uuid").asString());

        Map<String, Object> edicion = EscenarioDeTarea.rapida(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Historia");
        edicion.put("uuid", historia.get("uuid").asString());
        edicion.put("fechaDeActualizacion", historia.get("fechaDeActualizacion").asString());

        guardar(edicion).esperarError(400, "JERARQUIA_DE_TAREA_INVALIDA");
    }

    @Test
    void unaSubtareaSinPadreRechazada() {
        JsonNode error = guardar(EscenarioDeTarea.rapida(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Suelta"))
                .esperarError(400, "JERARQUIA_DE_TAREA_INVALIDA").json();

        assertEquals("Una Subtarea necesita una tarea padre.", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void unaSubtareaNoQuedaSinPadreAlEditar() {
        JsonNode historia = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia");
        JsonNode subtarea = escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Subtarea",
                historia.get("uuid").asString());
        Map<String, Object> edicion = EscenarioDeTarea.rapida(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Sin padre");
        edicion.put("uuid", subtarea.get("uuid").asString());
        edicion.put("fechaDeActualizacion", subtarea.get("fechaDeActualizacion").asString());

        guardar(edicion).esperarError(400, "JERARQUIA_DE_TAREA_INVALIDA");
    }

    @Test
    void elPadrePuedeEstarEnOtroTableroDelMismoProyecto() {
        JsonNode historia = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia");

        JsonNode subtarea = escenario.crearHija(TareasDeSeed.TABLERO_QA, "SUBTAREA", "Probar la historia",
                historia.get("uuid").asString());

        assertEquals(historia.get("clave").asString(), subtarea.get("padreClave").asString());
    }

    // POST /tarea/guardar como SYSADMIN
    private Respuesta guardar(Map<String, Object> cuerpo) {
        return como(SYSADMIN).post("/tarea/guardar").json(cuerpo).ejecutar();
    }

    // Body del modo rapido en el tablero del padre, hija del padre recibido
    private static Map<String, Object> conPadre(String tipoCodigo, String titulo, JsonNode padre) {
        Map<String, Object> cuerpo = EscenarioDeTarea.rapida(padre.get("tableroUuid").asString(), tipoCodigo, titulo);
        cuerpo.put("padreUuid", padre.get("uuid").asString());
        return cuerpo;
    }

    // Claves de las tareas hijas de una tarea, en orden
    private static List<String> clavesDeHijas(JsonNode tarea) {
        List<String> claves = new ArrayList<>();
        tarea.get("hijas").forEach(hija -> claves.add(hija.get("clave").asString()));
        return claves;
    }
}
