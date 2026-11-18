package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-USR-04: borrado logico de un usuario (DELETE /usuario/borrar/{uuid}). Migrado de bruno/User (04 Delete,
 * Delete - uuid inexistente y Delete - doble borrado).
 */
class UsuarioEliminarContratoTest extends PruebaDeContrato {

    private final EscenarioDeUsuario usuarios = new EscenarioDeUsuario(this::como);

    @Test
    void eliminaUnUsuario() {
        String uuid = usuarios.crearUuid("usuario.eliminar.contrato");

        JsonNode eliminado = eliminar(uuid).esperar(200).json();

        assertEquals(uuid, eliminado.get("uuid").asString());
        assertEquals(-1, eliminado.get("status").asInt());
    }

    @Test
    void uuidInexistente() {
        noExiste(eliminar("uuid-que-no-existe-00000000"));
    }

    @Test
    void dobleBorrado() {
        String uuid = usuarios.crearUuid("usuario.doble.borrado");
        usuarios.eliminar(uuid);

        noExiste(eliminar(uuid));
    }

    // DELETE /usuario/borrar/{uuid} como SYSADMIN
    private Respuesta eliminar(String uuid) {
        return como("SYSADMIN").delete("/usuario/borrar/{uuid}", uuid).ejecutar();
    }

    // Exige 400 REGISTRO_NO_ENCONTRADO con el mensaje del borrado
    private static void noExiste(Respuesta respuesta) {
        JsonNode error = respuesta.esperarError(400, "REGISTRO_NO_ENCONTRADO").json();
        assertEquals("No existe o ya fue eliminado", error.get("mensajeParaUsuario").asString());
    }
}
