package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-24: grupos del usuario de la sesion (GET /usuario/sesion/grupos, resolucion A12 del 19/11). Lo usa el front
 * para saber si mostrar la administracion. Exige sesion pero ningun permiso.
 */
class UsuarioSesionGruposContratoTest extends PruebaDeContrato {

    @Test
    void unAdministradorLoSabe() {
        JsonNode sesion = como("ADMIN").get("/usuario/sesion/grupos").ejecutar().esperar(200).json();

        assertTrue(sesion.get("esAdministrador").asBoolean());
        assertEquals("ADMIN", sesion.get("grupos").get(0).get("nombre").asString());
    }

    @Test
    void unUsuarioSinGruposDeAdministracionNoLoEs() {
        JsonNode sesion = como("CLIENTE").get("/usuario/sesion/grupos").ejecutar().esperar(200).json();

        assertFalse(sesion.get("esAdministrador").asBoolean());
    }

    @Test
    void sinSesion() {
        sinAutenticacion().get("/usuario/sesion/grupos").ejecutar().esperarError(401, "NO_AUTENTICADO");
    }
}
