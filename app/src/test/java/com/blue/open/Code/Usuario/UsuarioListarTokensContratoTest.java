package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-TOK-02: listar los tokens de acceso vigentes del usuario de la sesion (GET /usuario/tokens, AccessTokenController
 * de core-protocolo-apikey). Migrado de bruno/User (09 Get Tokens). Exige sesion pero ningun permiso, asi que el caso
 * "sin sesion" se prueba aca.
 */
class UsuarioListarTokensContratoTest extends PruebaDeContrato {

    @Test
    void listaLosTokensPropios() {
        JsonNode tokens = como(UsuariosDeSeed.ROL_DEMO).get("/usuario/tokens")
                .ejecutar().esperar(200).json();

        assertTrue(tokens.isArray());
        JsonNode demo = null;
        for (JsonNode token : tokens) {
            assertEquals(UsuariosDeSeed.DEMO, token.get("usuarioUuid").asString());
            if (UsuariosDeSeed.TOKEN_DEMO.equals(token.get("uuid").asString())) {
                demo = token;
            }
        }
        assertNotNull(demo, "Falta el token del seed: " + tokens);
        assertEquals(UsuariosDeSeed.TOKEN_DEMO_NOMBRE, demo.get("nombre").asString());
        // El token se muestra solo al crearlo (T2.1): el listado no lo trae
        assertTrue(demo.get("codigo").isNull());
        assertEquals(1, demo.get("activo").asInt());
    }

    @Test
    void sinSesion() {
        sinAutenticacion().get("/usuario/tokens").ejecutar()
                .esperarError(401, "NO_AUTENTICADO");
    }
}
