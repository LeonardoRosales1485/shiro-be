package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.Auth.PermisoBaseEnum;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * CU-PER-02: obtener un permiso por uuid (GET /permiso/uuid/{uuid}). Migrado de bruno/Permission (01 Get by UUID y
 * Get - uuid inexistente).
 */
class PermisoObtenerContratoTest extends PruebaDeContrato {

    private final EscenarioDeAutorizacion autorizacion = new EscenarioDeAutorizacion(this::como);

    @Test
    void obtieneUnPermiso() {
        String uuid = autorizacion.uuidDePermiso(PermisoBaseEnum.PERMISO_VER.getNombre());

        JsonNode permiso = obtener(uuid).esperar(200).json();

        assertEquals(uuid, permiso.get("uuid").asString());
        assertEquals(PermisoBaseEnum.PERMISO_VER.getNombre(), permiso.get("nombre").asString());
        assertEquals(0, permiso.get("status").asInt());
        assertFalse(permiso.has("id"));
    }

    @Test
    void uuidInexistente() {
        JsonNode error = obtener("uuid-que-no-existe-00000000").esperarError(400, "REGISTRO_NO_ENCONTRADO").json();

        assertEquals("No existe el registro", error.get("mensajeParaUsuario").asString());
    }

    // GET /permiso/uuid/{uuid} como SYSADMIN
    private Respuesta obtener(String uuid) {
        return como(EscenarioDeAutorizacion.SYSADMIN).get("/permiso/uuid/{uuid}", uuid)
                .ejecutar();
    }
}
