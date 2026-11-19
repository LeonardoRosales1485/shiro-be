package com.blue.open.Code.Importacion;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-IMP-01 y CU-IMP-02: conectar con Jira y leer su catalogo (T4.1 del plan, Spec 6 y Spec 7). El administrador
 * manda la URL, el correo y el token de Jira (se guardan cifrados y nunca vuelven) y la clave del proyecto; Shiro
 * responde los tableros, estados, tipos y usuarios de Jira, con el usuario de Shiro que tiene el mismo correo. La
 * clave de Jira va a ser el prefijo del proyecto: si no sirve como prefijo o ya la usa otro proyecto, se corta.
 */
class ImportacionConectarContratoTest extends PruebaDeContrato {

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
    void conectarRespondeElCatalogoDeJira() {
        JsonNode catalogo = escenario.conectar(escenario.credenciales("JSIM")).esperar(200).json();

        assertEquals("Proyecto simulado", catalogo.get("nombreProyectoJira").asString());
        assertEquals(2, catalogo.get("tableros").size());
        assertEquals("Desarrollo", catalogo.get("tableros").get(0).get("nombre").asString());
        assertEquals(3, catalogo.get("estados").size());
        assertEquals(5, catalogo.get("tipos").size());
        JsonNode usuarios = catalogo.get("usuarios");
        assertEquals("demo@example.local", usuarios.get(0).get("correo").asString());
        assertFalse(usuarios.get(0).get("usuarioUuid").isNull());
        assertTrue(usuarios.get(1).get("usuarioUuid").isNull());
        assertFalse(catalogo.toString().contains(JiraSimulado.TOKEN));

        JsonNode importacion = como("SYSADMIN").get("/importacion/uuid/{uuid}", catalogo.get("importacionUuid").asString())
                .ejecutar().esperar(200).json();
        assertEquals("CONECTADA", importacion.get("estado").asString());
        assertEquals("JSIM", importacion.get("claveProyectoJira").asString());
        assertFalse(importacion.toString().contains(JiraSimulado.TOKEN));
    }

    @Test
    void elCatalogoSeVuelveALeer() {
        String importacion = escenario.conectarJsim();

        JsonNode catalogo = como("SYSADMIN").get("/importacion/{uuid}/catalogo", importacion).ejecutar().esperar(200).json();

        assertEquals(importacion, catalogo.get("importacionUuid").asString());
        assertEquals(2, catalogo.get("tableros").size());
    }

    @Test
    void credencialesInvalidas() {
        Map<String, Object> cuerpo = new HashMap<>(escenario.credenciales("JSIM"));
        cuerpo.put("token", "otro-token");

        escenario.conectar(cuerpo).esperarError(400, "JIRA_CREDENCIALES_INVALIDAS");
    }

    @Test
    void proyectoInexistenteEnJira() {
        escenario.conectar(escenario.credenciales("NOPE")).esperarError(400, "JIRA_PROYECTO_NO_ENCONTRADO");
    }

    @Test
    void jiraNoResponde() {
        Map<String, Object> cuerpo = new HashMap<>(escenario.credenciales("JSIM"));
        cuerpo.put("url", "http://127.0.0.1:1");

        escenario.conectar(cuerpo).esperarError(503, "JIRA_NO_DISPONIBLE");
    }

    @Test
    void laClaveNoSirveComoPrefijo() {
        escenario.conectar(escenario.credenciales("JS1M")).esperarError(400, "CLAVE_DE_JIRA_NO_ES_PREFIJO");
    }

    @Test
    void laClaveYaLaUsaOtroProyecto() {
        como("SYSADMIN").post("/proyecto/guardar").json(Map.of("nombre", "Ya existe", "prefijo", "JSIM"))
                .preparacion().ejecutar().esperar(200);

        escenario.conectar(escenario.credenciales("JSIM")).esperarError(400, "PREFIJO_DE_PROYECTO_EN_USO");
    }

    @Test
    void cuerpoInvalido() {
        escenario.conectar(Map.of("claveProyecto", "JSIM")).esperarError(400, "FORMULARIO_INVALIDO");
    }
}
