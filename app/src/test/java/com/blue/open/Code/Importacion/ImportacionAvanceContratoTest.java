package com.blue.open.Code.Importacion;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-IMP-04 con el avance y CU-IMP-07, cancelar (resolucion de la propuesta 55): mientras corre, la importacion
 * guarda cuantas tareas, comentarios, sprints y versiones lleva de cuantas hay, y cuando empezo; al terminar el avance
 * queda completo. Cancelar una importacion en curso la marca para que se corte en el proximo control: no queda nada de
 * esa ejecucion y la importacion pasa a CANCELADA. Solo se cancela una importacion en curso.
 */
class ImportacionAvanceContratoTest extends PruebaDeContrato {

    // Importacion de los seeds en curso (2026-11-19-insert-ejemplos-auditoria-seed.yaml)
    private static final String IMPORTACION_EN_CURSO = "b8000000-0000-4000-8000-000000000002";

    private static final ObjectMapper LECTOR = new ObjectMapper();

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
    void alTerminarElAvanceQuedaCompleto() {
        JsonNode importacion = escenario.importarJsim();

        JsonNode avance = LECTOR.readTree(importacion.get("avance").asString());
        assertTrue(avance.get("tareasTotales").asInt() > 0, avance::toString);
        assertEquals(avance.get("tareasTotales").asInt(), avance.get("tareas").asInt());
        assertEquals(avance.get("comentariosTotales").asInt(), avance.get("comentarios").asInt());
        assertEquals(avance.get("sprintsTotales").asInt(), avance.get("sprints").asInt());
        assertEquals(avance.get("versionesTotales").asInt(), avance.get("versiones").asInt());
        assertFalse(avance.get("inicio").isNull());
        assertFalse(importacion.get("cancelacionPedida").asBoolean());
    }

    @Test
    void cancelarUnaImportacionEnCurso() {
        JsonNode cancelada = como("SYSADMIN").post("/importacion/{uuid}/cancelar", IMPORTACION_EN_CURSO).ejecutar().esperar(200).json();

        assertTrue(cancelada.get("cancelacionPedida").asBoolean());
        assertEquals("EN_CURSO", cancelada.get("estado").asString());
    }

    @Test
    void soloSeCancelaUnaImportacionEnCurso() {
        String conectada = escenario.conectarJsim();

        como("SYSADMIN").post("/importacion/{uuid}/cancelar", conectada).ejecutar().esperarError(409, "IMPORTACION_NO_EN_CURSO");
    }
}
