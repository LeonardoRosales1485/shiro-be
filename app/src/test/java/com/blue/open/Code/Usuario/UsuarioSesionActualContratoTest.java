package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * CU-USR-05: usuario de la sesion actual (GET /usuario/sesion). Migrado de bruno/User (06 Get Login). Exige sesion pero
 * ningun permiso, asi que core-test no genera el caso "sin autenticacion": se prueba aca. El login con usuario y
 * contrasena (POST /usuario/iniciar-sesion) esta en UsuarioIniciarSesionContratoTest.
 */
class UsuarioSesionActualContratoTest extends PruebaDeContrato {

    @Test
    void devuelveElUsuarioDeLaSesion() {
        JsonNode usuario = como(UsuariosDeSeed.ROL_DEMO).get("/usuario/sesion")
                .ejecutar().esperar(200).json();

        assertEquals(UsuariosDeSeed.DEMO, usuario.get("uuid").asString());
        assertEquals(UsuariosDeSeed.DEMO_NOMBRE_DE_USUARIO, usuario.get("nombreDeUsuario").asString());
        assertEquals(UsuariosDeSeed.DEMO_EMAIL, usuario.get("email").asString());
        assertFalse(usuario.hasNonNull("contrasena"));
    }

    @Test
    void sinSesion() {
        JsonNode error = sinAutenticacion().get("/usuario/sesion").ejecutar()
                .esperarError(401, "NO_AUTENTICADO").json();

        assertEquals("Usuario no identificado", error.get("mensajeParaUsuario").asString());
    }

    // GET /usuario/login se renombro a GET /usuario/sesion (ticket 06) y POST /usuario/login a POST
    // /usuario/iniciar-sesion (ticket 08): /usuario/login ya no tiene ningun handler
    @Test
    void rutaViejaNoExiste() {
        como(UsuariosDeSeed.ROL_DEMO).get("/usuario/login").ejecutar().esperar(404);
    }
}
