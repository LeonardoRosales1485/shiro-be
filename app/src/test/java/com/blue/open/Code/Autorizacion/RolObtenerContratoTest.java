package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.Auth.PermisoBaseEnum;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * CU-ROL-02: obtener un rol por uuid (GET /rol/uuid/{uuid}). Migrado de bruno/Role (01 Get by UUID y
 * Get - uuid inexistente).
 */
class RolObtenerContratoTest extends PruebaDeContrato {

    private final EscenarioDeAutorizacion autorizacion = new EscenarioDeAutorizacion(this::como);

    @Test
    void obtieneUnRolConSusPermisos() {
        JsonNode rol = obtener(RolesDeSeed.CLIENTE).esperar(200).json();

        assertEquals(RolesDeSeed.CLIENTE, rol.get("uuid").asString());
        assertEquals("CLIENTE", rol.get("nombre").asString());
        assertEquals(1, rol.get("porDefecto").asInt());
        assertEquals(0, rol.get("permisos").size());
        assertFalse(rol.has("id"));
    }

    @Test
    void obtieneLosPermisosQueOtorgaElRol() {
        String uuid = autorizacion.crearRol("Rol con permisos",
                autorizacion.uuidDePermiso(PermisoBaseEnum.ROL_VER.getNombre()));

        JsonNode rol = obtener(uuid).esperar(200).json();

        assertEquals(1, rol.get("permisos").size());
        assertEquals(PermisoBaseEnum.ROL_VER.getNombre(), rol.get("permisos").get(0).get("nombre").asString());
    }

    @Test
    void uuidInexistente() {
        JsonNode error = obtener("uuid-que-no-existe-00000000").esperarError(400, "REGISTRO_NO_ENCONTRADO").json();

        assertEquals("No existe el registro", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void unRolEliminadoNoSeEncuentra() {
        String uuid = autorizacion.crearRol("Rol eliminado");
        autorizacion.eliminarRol(uuid);

        obtener(uuid).esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    // GET /rol/uuid/{uuid} como SYSADMIN
    private Respuesta obtener(String uuid) {
        return como(EscenarioDeAutorizacion.SYSADMIN).get("/rol/uuid/{uuid}", uuid)
                .ejecutar();
    }
}
