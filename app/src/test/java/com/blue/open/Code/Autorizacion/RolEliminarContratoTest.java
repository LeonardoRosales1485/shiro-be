package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-ROL-04: borrado logico de un rol (DELETE /rol/borrar/{uuid}). Migrado de bruno/Role (04 Delete,
 * Delete - uuid inexistente y Delete - doble borrado).
 */
class RolEliminarContratoTest extends PruebaDeContrato {

    private final EscenarioDeAutorizacion autorizacion = new EscenarioDeAutorizacion(this::como);

    @Test
    void eliminaUnRol() {
        String uuid = autorizacion.crearRol("Rol a eliminar");

        JsonNode rol = eliminar(uuid).esperar(200).json();

        assertEquals(uuid, rol.get("uuid").asString());
        assertEquals(-1, rol.get("status").asInt());
    }

    @Test
    void uuidInexistente() {
        noExiste(eliminar("uuid-que-no-existe-00000000"));
    }

    @Test
    void dobleBorrado() {
        String uuid = autorizacion.crearRol("Rol para doble borrado");
        autorizacion.eliminarRol(uuid);

        noExiste(eliminar(uuid));
    }

    // DELETE /rol/borrar/{uuid} como SYSADMIN
    private Respuesta eliminar(String uuid) {
        return como(EscenarioDeAutorizacion.SYSADMIN).delete("/rol/borrar/{uuid}", uuid)
                .ejecutar();
    }

    // Exige 400 REGISTRO_NO_ENCONTRADO con el mensaje del borrado
    private static void noExiste(Respuesta respuesta) {
        JsonNode error = respuesta.esperarError(400, "REGISTRO_NO_ENCONTRADO").json();
        assertEquals("No existe o ya fue eliminado", error.get("mensajeParaUsuario").asString());
    }
}
