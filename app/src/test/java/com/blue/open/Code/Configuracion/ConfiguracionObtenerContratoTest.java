package com.blue.open.Code.Configuracion;

import com.blue.open.Code.Autorizacion.ActoresDelProyecto;
import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-CON-02: obtener una configuracion por su clave (GET /configuracion/obtener/{clave}). Una clave inexistente responde una
 * configuracion publica con valor null; una privada exige CONFIGURACION_VER.
 */
class ConfiguracionObtenerContratoTest extends PruebaDeContrato {

    @Test
    void obtieneUnaConfiguracionPrivadaConPermiso() {
        JsonNode configuracion = como(RolBaseEnum.SYSADMIN.name())
                .get("/configuracion/obtener/{clave}", ConfiguracionesDeSeed.PRIVADA).ejecutar().esperar(200).json();

        assertEquals(ConfiguracionesDeSeed.PRIVADA, configuracion.get("nombre").asString());
        assertFalse(configuracion.get("global").asBoolean());
        assertFalse(configuracion.has("id"));
    }

    @Test
    void claveInexistenteDevuelveUnaPublicaSinValor() {
        JsonNode configuracion = sinAutenticacion().get("/configuracion/obtener/{clave}", "clave-que-no-existe-00000000")
                .ejecutar().esperar(200).json();

        assertTrue(configuracion.get("valor").isNull());
        assertTrue(configuracion.get("global").asBoolean());
    }

    @Test
    void privadaSinSesion() {
        JsonNode error = sinAutenticacion().get("/configuracion/obtener/{clave}", ConfiguracionesDeSeed.PRIVADA).ejecutar()
                .esperarError(401, "NO_AUTENTICADO").json();

        assertEquals("Usuario no identificado", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void privadaSinPermiso() {
        como(ActoresDelProyecto.CLIENTE).get("/configuracion/obtener/{clave}", ConfiguracionesDeSeed.PRIVADA).ejecutar()
                .esperarError(403, "SIN_PERMISO");
    }
}
