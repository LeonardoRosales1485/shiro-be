package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-01: obtener un usuario por uuid (GET /usuario/uuid/{uuid}). Migrado de bruno/User (01 Get by UUID y
 * Get - uuid inexistente). El proyecto usa core-multiempresa: el DTO trae rolesPorEmpresa.
 */
class UsuarioObtenerContratoTest extends PruebaDeContrato {

    @Test
    void obtieneElUsuarioDemo() {
        JsonNode usuario = como("SYSADMIN").get("/usuario/uuid/{uuid}", UsuariosDeSeed.DEMO)
                .ejecutar().esperar(200).json();

        assertEquals(UsuariosDeSeed.DEMO, usuario.get("uuid").asString());
        assertEquals(UsuariosDeSeed.DEMO_NOMBRE_DE_USUARIO, usuario.get("nombreDeUsuario").asString());
        assertEquals(UsuariosDeSeed.DEMO_EMAIL, usuario.get("email").asString());
        assertEquals(UsuariosDeSeed.DEMO_TELEFONO, usuario.get("telefono").asString());
        assertFalse(usuario.hasNonNull("contrasena"));
        assertFalse(usuario.has("id"));
    }

    @Test
    void obtieneLasEmpresasYRolesDelUsuario() {
        JsonNode usuario = como("SYSADMIN").get("/usuario/uuid/{uuid}", UsuariosDeSeed.DEMO)
                .ejecutar().esperar(200).json();

        UsuariosDeSeed.verificarEmpresasYRolesDelDemo(usuario);
    }

    @Test
    void uuidInexistente() {
        JsonNode error = como("SYSADMIN").get("/usuario/uuid/{uuid}", "uuid-que-no-existe-00000000")
                .ejecutar().esperarError(400, "REGISTRO_NO_ENCONTRADO").json();

        assertEquals("No existe el registro", error.get("mensajeParaUsuario").asString());
        assertTrue(error.has("codigo"));
    }
}
