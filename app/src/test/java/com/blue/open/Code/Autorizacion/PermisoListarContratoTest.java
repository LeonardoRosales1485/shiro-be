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
 * CU-PER-01: listado paginado de permisos (POST /permiso/paginar), con PERMISO_VER. Migrado de
 * bruno/Permission (02 Paginate); los casos "sin autenticacion" y "sin permiso" los genera core-test.
 */
class PermisoListarContratoTest extends PruebaDeContrato {

    @Test
    void listaLosPermisosVigentes() {
        JsonNode pagina = paginar(Map.of("pagina", 1, "tamano", 10,
                "filtros", List.of(List.of(Map.of("campo", "status", "operacion", "=", "valor", "0"))),
                "ordenes", List.of(Map.of("campo", "id", "direccion", "desc")),
                "parametros", List.of())).esperar(200).json();

        assertTrue(pagina.get("contenido").size() > 0);
        JsonNode primero = pagina.get("contenido").get(0);
        assertTrue(primero.has("uuid"));
        assertTrue(primero.has("nombre"));
        assertFalse(primero.has("id"));
    }

    @Test
    void filtraPorNombre() {
        JsonNode pagina = paginar(Map.of("pagina", 1, "tamano", 10, "filtros", List.of(List.of(
                Map.of("campo", "nombre", "operacion", "=", "valor", PermisoBaseEnum.ROL_VER.getNombre())))))
                .esperar(200).json();

        assertEquals(1, pagina.get("totalDeElementos").asInt());
        assertEquals(PermisoBaseEnum.ROL_VER.getNombre(), pagina.get("contenido").get(0).get("nombre").asString());
    }

    // POST /permiso/paginar como SYSADMIN
    private Respuesta paginar(Map<String, Object> cuerpo) {
        return como(EscenarioDeAutorizacion.SYSADMIN).post("/permiso/paginar").json(cuerpo)
                .ejecutar();
    }
}
