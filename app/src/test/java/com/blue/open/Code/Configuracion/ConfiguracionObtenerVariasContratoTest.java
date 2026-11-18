package com.blue.open.Code.Configuracion;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-CON-03: obtener varias configuraciones por sus claves (POST /configuracion/obtener-varias). Sin CONFIGURACION_VER, si
 * alguna es privada responde 403 CONFIGURACIONES_PRIVADAS indicando cuales.
 */
class ConfiguracionObtenerVariasContratoTest extends PruebaDeContrato {

    @Test
    void obtieneUnaPorClave() {
        JsonNode configuraciones = como(RolBaseEnum.SYSADMIN.name()).post("/configuracion/obtener-varias")
                .json(List.of(ConfiguracionesDeSeed.PRIVADA, "config.prueba.lista")).ejecutar().esperar(200).json();

        assertEquals(2, configuraciones.size());
        assertEquals(ConfiguracionesDeSeed.PRIVADA, configuraciones.get(0).get("nombre").asString());
        assertTrue(configuraciones.get(1).get("valor").isNull());
    }

    @Test
    void publicasSinSesion() {
        sinAutenticacion().post("/configuracion/obtener-varias").json(List.of("config.prueba.lista")).ejecutar()
                .esperar(200);
    }

    @Test
    void privadaSinSesion() {
        JsonNode error = sinAutenticacion().post("/configuracion/obtener-varias").json(List.of(ConfiguracionesDeSeed.PRIVADA))
                .ejecutar().esperarError(403, "CONFIGURACIONES_PRIVADAS").json();

        assertEquals("Las variables " + ConfiguracionesDeSeed.PRIVADA + " son privadas",
                error.get("mensajeParaUsuario").asString());
    }
}
