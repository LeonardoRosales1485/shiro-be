package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-TOK-03: eliminar un token de acceso propio (DELETE /usuario/borrar-token/{uuid}, AccessTokenController de
 * core-protocolo-apikey). Migrado de bruno/User (10 Delete Token). Exige sesion pero ningun permiso, asi que el caso
 * "sin sesion" se prueba aca. El token se crea antes con crear-token (paso de preparacion).
 */
class UsuarioEliminarTokenContratoTest extends PruebaDeContrato {

    private final EscenarioDeUsuario usuarios = new EscenarioDeUsuario(this::como);

    @Test
    void eliminaUnTokenPropio() {
        String uuid = usuarios.crearToken(UsuariosDeSeed.ROL_DEMO, "Token a eliminar").get("uuid").asString();

        JsonNode token = eliminar(UsuariosDeSeed.ROL_DEMO, uuid).esperar(200).json();

        assertEquals(uuid, token.get("uuid").asString());
        assertEquals(-1, token.get("status").asInt());
        assertEquals(0, token.get("activo").asInt());
    }

    @Test
    void tokenInexistente() {
        eliminar(UsuariosDeSeed.ROL_DEMO, UsuariosDeSeed.INEXISTENTE).esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    @Test
    void tokenDeOtroUsuario() {
        eliminar("SYSADMIN", UsuariosDeSeed.TOKEN_DEMO).esperarError(403, "SIN_PERMISO");
    }

    @Test
    void sinSesion() {
        sinAutenticacion().delete("/usuario/borrar-token/{uuid}", UsuariosDeSeed.TOKEN_DEMO)
                .ejecutar().esperarError(401, "NO_AUTENTICADO");
    }

    // La ruta en ingles se renombro a la del glosario (ticket 08): ya no existe
    @Test
    void rutaViejaNoExiste() {
        String uuid = usuarios.crearToken(UsuariosDeSeed.ROL_DEMO, "Token ruta vieja").get("uuid").asString();

        como(UsuariosDeSeed.ROL_DEMO).delete("/usuario/delete-token/{uuid}", uuid).ejecutar().esperar(404);
    }

    // DELETE /usuario/borrar-token/{uuid} como el rol recibido
    private Respuesta eliminar(String rol, String uuid) {
        return como(rol).delete("/usuario/borrar-token/{uuid}", uuid).ejecutar();
    }
}
