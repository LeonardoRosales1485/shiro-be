package com.blue.open.Code.Importacion;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-IMP-03 y CU-IMP-04: mapear y ejecutar una importacion (T4.2 a T4.4 del plan, Spec 6 y Spec 7). El mapeo lleva
 * cada tablero de Jira a un tipo de tablero de Shiro con sus estados, y cada tipo de Jira a un tipo de Shiro. La
 * ejecucion crea el proyecto con la clave de Jira como prefijo, trae tareas, subtareas, comentarios, sprints y
 * versiones conservando las claves, escribe el historial con canal IMPORTACION y deja un resumen. Reimportar con el
 * mismo mapeo actualiza sin duplicar. En las pruebas la ejecucion corre en el mismo hilo
 * (shiro.importacion.sincronica).
 */
class ImportacionEjecutarContratoTest extends PruebaDeContrato {

    private static JiraSimulado jira;

    private EscenarioDeImportacion escenario;

    @BeforeAll
    static void iniciarJira() throws IOException {
        jira = JiraSimulado.iniciar();
    }

    @AfterAll
    static void detenerJira() {
        jira.close();
    }

    @BeforeEach
    void armarEscenario() {
        escenario = new EscenarioDeImportacion(this::como, jira);
    }

    @Test
    void mapearDejaLaImportacionLista() {
        String importacion = escenario.conectarJsim();

        JsonNode mapeada = escenario.mapear(importacion, EscenarioDeImportacion.mapeo(EscenarioDeImportacion.TIPOS))
                .esperar(200).json();

        assertEquals("MAPEADA", mapeada.get("estado").asString());
    }

    @Test
    void importarTraeTodoConservandoLasClaves() {
        JsonNode importacion = escenario.importarJsim();

        assertEquals("TERMINADA", importacion.get("estado").asString());
        JsonNode resumen = EscenarioDeImportacion.resumen(importacion);
        assertEquals(5, resumen.get("tareasCreadas").asInt());
        assertEquals(0, resumen.get("tareasActualizadas").asInt());
        assertEquals(1, resumen.get("comentarios").asInt());
        assertEquals(1, resumen.get("sprints").asInt());
        assertEquals(1, resumen.get("versiones").asInt());
        assertEquals(0, resumen.get("fallas").size());
        assertEquals("nadie@example.com", resumen.get("usuariosSinVincular").get(0).asString());
        assertEquals(1, resumen.get("tareasPorTipo").get("BUG").asInt());

        JsonNode epica = escenario.tareaPorClave("JSIM-1");
        JsonNode historia = escenario.tareaPorClave("JSIM-2");
        JsonNode subtarea = escenario.tareaPorClave("JSIM-7");
        JsonNode deQa = escenario.tareaPorClave("JSIM-9");
        assertEquals(importacion.get("proyectoUuid").asString(), historia.get("proyectoUuid").asString());
        assertEquals("EPICA", epica.get("tipoCodigo").asString());
        assertEquals(epica.get("uuid").asString(), historia.get("padreUuid").asString());
        assertEquals(historia.get("uuid").asString(), subtarea.get("padreUuid").asString());
        assertEquals(EscenarioDeImportacion.HECHO, historia.get("estadoUuid").asString());
        assertEquals("ALTA", historia.get("prioridad").asString());
        assertEquals("Descripción de JSIM-2", historia.get("descripcion").asString());
        assertFalse(historia.get("responsableUuid").isNull());
        assertTrue(escenario.tareaPorClave("JSIM-4").get("responsableUuid").isNull());
        assertEquals("Sprint simulado", historia.get("sprintNombre").asString());
        assertEquals("0.9", historia.get("versiones").get(0).get("nombre").asString());
        assertEquals("importada", historia.get("etiquetas").get(0).get("texto").asString());
        assertEquals("QA", deQa.get("tableroCodigo").asString());

        JsonNode historial = como("SYSADMIN").get("/tarea/{uuid}/historial", historia.get("uuid").asString())
                .ejecutar().esperar(200).json();
        assertEquals("IMPORTACION", historial.get(historial.size() - 1).get("canal").asString());
    }

    @Test
    void elContadorSigueDesdeLaClaveMasAlta() {
        JsonNode importacion = escenario.importarJsim();
        JsonNode proyecto = como("SYSADMIN").get("/proyecto/uuid/{uuid}", importacion.get("proyectoUuid").asString())
                .ejecutar().esperar(200).json();
        String principal = null;
        for (JsonNode tablero : proyecto.get("tableros")) {
            if ("PRINCIPAL".equals(tablero.get("tipoTableroCodigo").asString())) {
                principal = tablero.get("uuid").asString();
            }
        }

        JsonNode nueva = como("SYSADMIN").post("/tarea/guardar").json(Map.of("tableroUuid", principal,
                "tipoCodigo", "TAREA", "titulo", "Despues de importar")).ejecutar().esperar(200).json();

        assertEquals("JSIM", proyecto.get("prefijo").asString());
        assertEquals("JSIM-10", nueva.get("clave").asString());
    }

    @Test
    void reimportarNoDuplica() {
        JsonNode primera = escenario.importarJsim();

        JsonNode segunda = escenario.ejecutar(primera.get("uuid").asString()).esperar(200).json();

        JsonNode resumen = EscenarioDeImportacion.resumen(segunda);
        assertEquals(0, resumen.get("tareasCreadas").asInt());
        assertEquals(5, resumen.get("tareasActualizadas").asInt());
        assertEquals(0, resumen.get("comentarios").asInt());
        assertEquals(primera.get("proyectoUuid").asString(), segunda.get("proyectoUuid").asString());
    }

    @Test
    void unaNuevaConexionReusaElProyectoImportado() {
        JsonNode primera = escenario.importarJsim();

        JsonNode segunda = escenario.importarJsim();

        assertEquals(primera.get("proyectoUuid").asString(), segunda.get("proyectoUuid").asString());
        assertEquals(5, EscenarioDeImportacion.resumen(segunda).get("tareasActualizadas").asInt());
    }

    @Test
    void unTipoSinMapearQuedaEnLasFallas() {
        String importacion = escenario.conectarJsim();
        Map<String, String> sinBug = new HashMap<>(EscenarioDeImportacion.TIPOS);
        sinBug.remove("10003");
        escenario.mapear(importacion, EscenarioDeImportacion.mapeo(sinBug)).esperar(200);

        JsonNode resumen = EscenarioDeImportacion.resumen(escenario.ejecutar(importacion).esperar(200).json());

        assertEquals(4, resumen.get("tareasCreadas").asInt());
        assertEquals("JSIM-4", resumen.get("fallas").get(0).get("claveJira").asString());
        assertNotNull(resumen.get("fallas").get(0).get("motivo").asString());
    }

    @Test
    void unEstadoDeOtroTipoDeTableroNoSeMapea() {
        String importacion = escenario.conectarJsim();
        Map<String, Object> mapeo = Map.of(
                "tableros", List.of(Map.of("idJira", JiraSimulado.TABLERO_DESARROLLO,
                        "tipoTableroUuid", EscenarioDeImportacion.TIPO_TABLERO_PRINCIPAL,
                        "estados", List.of(Map.of("idJira", "1", "estadoUuid", EscenarioDeImportacion.APROBADO)))),
                "tipos", List.of());

        escenario.mapear(importacion, mapeo).esperarError(400, "MAPEO_DE_JIRA_INVALIDO");
    }

    @Test
    void unTableroQueNoEsDeJiraNoSeMapea() {
        String importacion = escenario.conectarJsim();
        Map<String, Object> mapeo = Map.of(
                "tableros", List.of(Map.of("idJira", "999",
                        "tipoTableroUuid", EscenarioDeImportacion.TIPO_TABLERO_PRINCIPAL, "estados", List.of())),
                "tipos", List.of());

        escenario.mapear(importacion, mapeo).esperarError(400, "MAPEO_DE_JIRA_INVALIDO");
    }

    @Test
    void sinMapeoNoSeEjecuta() {
        String importacion = escenario.conectarJsim();

        escenario.ejecutar(importacion).esperarError(400, "IMPORTACION_SIN_MAPEO");
    }

    @Test
    void importacionInexistente() {
        escenario.ejecutar("00000000-0000-4000-8000-000000000000").esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }
}
