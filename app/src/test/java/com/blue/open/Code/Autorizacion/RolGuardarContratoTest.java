package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.Auth.PermisoBaseEnum;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * CU-ROL-03: alta o edicion de un rol con los permisos que otorga (POST /rol/guardar). Migrado de bruno/Role
 * (03 Save or Create y Save - nombre duplicado); "Save - sin autenticacion" lo genera core-test.
 * El proyecto usa core-multiempresa (RolService extiende RolBaseMultiempresaService): el nombre de un rol no se
 * repite dentro de la misma empresa (ROL_YA_EXISTE).
 */
class RolGuardarContratoTest extends PruebaDeContrato {

    private final EscenarioDeAutorizacion autorizacion = new EscenarioDeAutorizacion(this::como);

    @Test
    void altaDeUnRol() {
        JsonNode rol = guardar(EscenarioDeAutorizacion.rol("Rol de prueba")).esperar(200).json();

        assertEquals("Rol de prueba", rol.get("nombre").asString());
        assertEquals(0, rol.get("porDefecto").asInt());
        assertEquals(0, rol.get("status").asInt());
        assertEquals(0, rol.get("permisos").size());
        assertFalse(rol.has("id"));
    }

    @Test
    void altaConPermisos() {
        String rolView = autorizacion.uuidDePermiso(PermisoBaseEnum.ROL_VER.getNombre());

        JsonNode rol = guardar(EscenarioDeAutorizacion.rol("Rol con permisos", rolView)).esperar(200).json();

        assertEquals(1, rol.get("permisos").size());
        assertEquals(rolView, rol.get("permisos").get(0).get("uuid").asString());
        assertEquals(PermisoBaseEnum.ROL_VER.getNombre(), rol.get("permisos").get(0).get("nombre").asString());
    }

    @Test
    void edicionDeUnRol() {
        String uuid = autorizacion.crearRol("Rol original");
        String rolView = autorizacion.uuidDePermiso(PermisoBaseEnum.ROL_VER.getNombre());

        Map<String, Object> cuerpo = EscenarioDeAutorizacion.rol("Rol renombrado", rolView);
        cuerpo.put("uuid", uuid);
        JsonNode rol = guardar(cuerpo).esperar(200).json();

        assertEquals(uuid, rol.get("uuid").asString());
        assertEquals("Rol renombrado", rol.get("nombre").asString());
        assertEquals(rolView, rol.get("permisos").get(0).get("uuid").asString());
    }

    @Test
    void nombreRepetidoEnLaMismaEmpresa() {
        autorizacion.crearRol("Rol repetido");

        JsonNode error = guardar(EscenarioDeAutorizacion.rol("Rol repetido")).esperarError(400, "ROL_YA_EXISTE").json();

        assertEquals("Ya existe un rol con ese nombre para su empresa", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void nombreObligatorio() {
        JsonNode error = guardar(Map.of("nombre", "", "porDefecto", 0, "permisos", List.of()))
                .esperarError(400, "FORMULARIO_INVALIDO").json();

        assertEquals("nombre", error.get("errores").get(0).get("nombre").asString());
        assertEquals("El nombre del rol es obligatorio", error.get("errores").get(0).get("mensaje").asString());
    }

    // POST /rol/guardar como SYSADMIN
    private Respuesta guardar(Map<String, Object> cuerpo) {
        return como(EscenarioDeAutorizacion.SYSADMIN).post("/rol/guardar").json(cuerpo)
                .ejecutar();
    }
}
