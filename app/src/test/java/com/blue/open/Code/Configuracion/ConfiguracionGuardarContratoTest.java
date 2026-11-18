package com.blue.open.Code.Configuracion;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * CU-CON-04: alta o edicion de configuraciones (POST /configuracion/guardar), con CONFIGURACION_CREAR. Cada una se busca
 * por su clave: si existe se edita. Los casos "sin autenticacion" y "sin permiso" los genera core-test.
 */
class ConfiguracionGuardarContratoTest extends PruebaDeContrato {

    @Test
    void creaUnaConfiguracionPublica() {
        JsonNode guardadas = guardar(List.of(Map.of("nombre", "config.prueba", "valor", "valor de prueba", "global", true)))
                .esperar(200).json();

        assertEquals(1, guardadas.size());
        assertEquals("config.prueba", guardadas.get(0).get("nombre").asString());
        assertEquals("valor de prueba", guardadas.get(0).get("valor").asString());
    }

    @Test
    void creaUnaConfiguracionPrivada() {
        JsonNode guardadas = guardar(List.of(Map.of("nombre", "config.privada.caso", "valor", "valor privado",
                "global", false))).esperar(200).json();

        assertFalse(guardadas.get(0).get("global").asBoolean());
    }

    @Test
    void editaUnaExistentePorSuClave() {
        JsonNode guardadas = guardar(List.of(Map.of("nombre", ConfiguracionesDeSeed.PRIVADA, "valor", "Otro banner",
                "global", false))).esperar(200).json();

        assertEquals("Otro banner", guardadas.get(0).get("valor").asString());
    }

    @Test
    void valorObligatorio() {
        guardar(List.of(Map.of("nombre", "config.sin.valor", "valor", "", "global", true)))
                .esperarError(400, "FORMULARIO_INVALIDO");
    }

    // POST /configuracion/guardar como SYSADMIN
    private Respuesta guardar(Object cuerpo) {
        return como(RolBaseEnum.SYSADMIN.name()).post("/configuracion/guardar").json(cuerpo).ejecutar();
    }
}
