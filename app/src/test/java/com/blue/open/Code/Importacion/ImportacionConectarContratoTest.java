package com.blue.open.Code.Importacion;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.blue.open.Code.Common.Cifrado.CifradorDeCredenciales;
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
 * responde los tableros, estados (con si cuentan como terminados), tipos (con su nivel en Shiro) y usuarios de Jira, con
 * el usuario de Shiro que tiene el mismo correo. El administrador elige el prefijo del proyecto en Shiro (resolucion A3
 * del 19/11): si ya lo usa otro proyecto, se corta. Solo se aceptan sitios https de Atlassian (resolucion A4).
 */
class ImportacionConectarContratoTest extends PruebaDeContrato {

    private static JiraSimulado jira;

    private EscenarioDeImportacion escenario;

    @Autowired
    private CifradorDeCredenciales cifrador;

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
        assertTrue(catalogo.get("estados").get(1).get("terminado").asBoolean());
        assertEquals(5, catalogo.get("tipos").size());
        assertEquals(1, catalogo.get("tipos").get(0).get("nivel").asInt());
        assertEquals(3, catalogo.get("tipos").get(4).get("nivel").asInt());
        JsonNode usuarios = catalogo.get("usuarios");
        assertEquals("demo@example.local", usuarios.get(0).get("correo").asString());
        assertFalse(usuarios.get(0).get("usuarioUuid").isNull());
        assertTrue(usuarios.get(1).get("usuarioUuid").isNull());
        assertFalse(catalogo.toString().contains(JiraSimulado.TOKEN));

        JsonNode importacion = como("SYSADMIN").get("/importacion/uuid/{uuid}", catalogo.get("importacionUuid").asString())
                .ejecutar().esperar(200).json();
        assertEquals("CONECTADA", importacion.get("estado").asString());
        assertEquals("JSIM", importacion.get("claveProyectoJira").asString());
        assertEquals("SIM", importacion.get("prefijo").asString());
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
    void elPrefijoTieneDeDosASeisLetras() {
        Map<String, Object> cuerpo = new HashMap<>(escenario.credenciales("JSIM"));
        cuerpo.put("prefijo", "S1");

        escenario.conectar(cuerpo).esperarError(400, "FORMULARIO_INVALIDO");
    }

    @Test
    void elPrefijoYaLoUsaOtroProyecto() {
        como("SYSADMIN").post("/proyecto/guardar").json(Map.of("nombre", "Ya existe", "prefijo", "SIM"))
                .preparacion().ejecutar().esperar(200);

        escenario.conectar(escenario.credenciales("JSIM")).esperarError(400, "PREFIJO_DE_PROYECTO_EN_USO");
    }

    @Test
    void soloSitiosDeAtlassian() {
        Map<String, Object> cuerpo = new HashMap<>(escenario.credenciales("JSIM"));
        cuerpo.put("url", "https://jira.example.com");
        escenario.conectar(cuerpo).esperarError(400, "JIRA_URL_NO_PERMITIDA");

        cuerpo.put("url", "http://empresa.atlassian.net");
        escenario.conectar(cuerpo).esperarError(400, "JIRA_URL_NO_PERMITIDA");
    }

    @Test
    void sinFraseDeCifrado() {
        Object frase = ReflectionTestUtils.getField(cifrador, "frase");
        ReflectionTestUtils.setField(cifrador, "frase", "");
        try {
            escenario.conectar(escenario.credenciales("JSIM")).esperarError(503, "CIFRADO_NO_CONFIGURADO");
        } finally {
            ReflectionTestUtils.setField(cifrador, "frase", frase);
        }
    }

    @Test
    void credencialesQueYaNoSeDescifran() {
        como("SYSADMIN").get("/importacion/{uuid}/catalogo", ImportacionContratoTest.IMPORTACION_DE_SEED).ejecutar()
                .esperarError(400, "CREDENCIAL_NO_DESCIFRABLE");
    }

    @Test
    void cuerpoInvalido() {
        escenario.conectar(Map.of("claveProyecto", "JSIM")).esperarError(400, "FORMULARIO_INVALIDO");
    }
}
