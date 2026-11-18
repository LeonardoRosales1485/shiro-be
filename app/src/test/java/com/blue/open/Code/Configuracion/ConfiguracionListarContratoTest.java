package com.blue.open.Code.Configuracion;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-CON-01: listado de configuraciones (GET /configuracion/listar). Con CONFIGURACION_VER responde todas; sin sesion o
 * sin el permiso, solo las publicas.
 */
class ConfiguracionListarContratoTest extends PruebaDeContrato {

    @Test
    void conPermisoIncluyeLasPrivadas() {
        JsonNode configuraciones = como(RolBaseEnum.SYSADMIN.name()).get("/configuracion/listar").ejecutar()
                .esperar(200).json();

        assertTrue(contiene(configuraciones, ConfiguracionesDeSeed.PRIVADA));
    }

    @Test
    void sinSesionSoloLasPublicas() {
        JsonNode configuraciones = sinAutenticacion().get("/configuracion/listar").ejecutar().esperar(200).json();

        configuraciones.forEach(c -> assertTrue(c.get("global").asBoolean(), () -> "Configuracion privada: " + c));
        assertFalse(contiene(configuraciones, ConfiguracionesDeSeed.PRIVADA));
    }

    // La lista tiene una configuracion con esa clave
    private static boolean contiene(JsonNode configuraciones, String clave) {
        for (JsonNode c : configuraciones) {
            if (clave.equals(c.get("nombre").asString())) {
                return true;
            }
        }
        return false;
    }
}
