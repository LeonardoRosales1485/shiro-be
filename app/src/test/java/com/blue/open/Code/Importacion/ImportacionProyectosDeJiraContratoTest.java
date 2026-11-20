package com.blue.open.Code.Importacion;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-IMP-08: proyectos de Jira visibles con unas credenciales (POST /importacion/proyectos-de-jira, resolucion de la
 * propuesta 53): el asistente los lista para elegir cual importar («Probar conexion»). No guarda nada: el token se usa
 * solo para esa lectura. Solo el super admin y los Administradores.
 */
class ImportacionProyectosDeJiraContratoTest extends PruebaDeContrato {

    private static JiraSimulado jira;

    @BeforeAll
    static void iniciarJira() throws IOException {
        jira = JiraSimulado.iniciar();
    }

    @AfterAll
    static void detenerJira() {
        jira.close();
    }

    @Test
    void listaLosProyectosVisibles() {
        JsonNode proyectos = como("SYSADMIN").post("/importacion/proyectos-de-jira")
                .json(Map.of("url", jira.url(), "correo", JiraSimulado.CORREO, "token", JiraSimulado.TOKEN)).ejecutar().esperar(200).json();

        assertEquals(2, proyectos.size());
        assertEquals("JSIM", proyectos.get(0).get("clave").asString());
        assertEquals("Proyecto simulado", proyectos.get(0).get("nombre").asString());
    }

    @Test
    void credencialesInvalidas() {
        como("SYSADMIN").post("/importacion/proyectos-de-jira").json(Map.of("url", jira.url(), "correo", JiraSimulado.CORREO, "token", "otro"))
                .ejecutar().esperarError(400, "JIRA_CREDENCIALES_INVALIDAS");
    }

    @Test
    void urlQueNoEsDeJiraCloud() {
        como("SYSADMIN").post("/importacion/proyectos-de-jira").json(Map.of("url", "https://ejemplo.com", "correo", JiraSimulado.CORREO,
                "token", JiraSimulado.TOKEN)).ejecutar().esperarError(400, "JIRA_URL_NO_PERMITIDA");
    }
}
