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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-ROL-01: listado paginado de roles (POST /rol/paginar). Migrado de bruno/Role (02 Paginate); los casos
 * "sin autenticacion" y "sin permiso" los genera core-test.
 */
class RolListarContratoTest extends PruebaDeContrato {

    private final EscenarioDeAutorizacion autorizacion = new EscenarioDeAutorizacion(this::como);

    @Test
    void listaLosRolesVigentes() {
        JsonNode pagina = paginar(Map.of("pagina", 1, "tamano", 10,
                "filtros", List.of(List.of(Map.of("campo", "status", "operacion", "=", "valor", "0"))),
                "ordenes", List.of(Map.of("campo", "id", "direccion", "desc")),
                "parametros", List.of())).esperar(200).json();

        assertTrue(pagina.get("contenido").size() > 0);
        JsonNode primero = pagina.get("contenido").get(0);
        assertTrue(primero.has("uuid"));
        assertTrue(primero.has("nombre"));
        assertTrue(primero.has("porDefecto"));
        assertEquals(0, primero.get("status").asInt());
        assertFalse(primero.has("id"));
    }

    @Test
    void filtraPorNombre() {
        JsonNode pagina = paginar(Map.of("pagina", 1, "tamano", 10, "filtros", List.of(List.of(
                Map.of("campo", "nombre", "operacion", "=", "valor", "CLIENTE"))))).esperar(200).json();

        assertEquals(1, pagina.get("totalDeElementos").asInt());
        assertEquals(RolesDeSeed.CLIENTE, pagina.get("contenido").get(0).get("uuid").asString());
    }

    @Test
    void listaLosPermisosDeCadaRol() {
        String uuid = autorizacion.crearRol("Rol listado con permisos",
                autorizacion.uuidDePermiso(PermisoBaseEnum.ROL_VER.getNombre()));

        JsonNode pagina = paginar(Map.of("pagina", 1, "tamano", 10, "filtros", List.of(List.of(
                Map.of("campo", "uuid", "operacion", "=", "valor", uuid))))).esperar(200).json();

        JsonNode permisos = pagina.get("contenido").get(0).get("permisos");
        assertEquals(1, permisos.size());
        assertEquals(PermisoBaseEnum.ROL_VER.getNombre(), permisos.get(0).get("nombre").asString());
    }

    @Test
    void unRolEliminadoNoSeLista() {
        String uuid = autorizacion.crearRol("Rol dado de baja");
        autorizacion.eliminarRol(uuid);

        JsonNode pagina = paginar(Map.of("pagina", 1, "tamano", 10, "filtros", List.of(List.of(
                Map.of("campo", "uuid", "operacion", "=", "valor", uuid))))).esperar(200).json();

        assertEquals(0, pagina.get("totalDeElementos").asInt());
    }

    // POST /rol/paginar como SYSADMIN
    private Respuesta paginar(Map<String, Object> cuerpo) {
        return como(EscenarioDeAutorizacion.SYSADMIN).post("/rol/paginar").json(cuerpo)
                .ejecutar();
    }
}
