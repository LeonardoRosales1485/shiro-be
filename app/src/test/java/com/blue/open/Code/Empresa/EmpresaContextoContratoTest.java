package com.blue.open.Code.Empresa;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-EMP-05: modo multi-empresa y empresa resuelta para la peticion (GET /empresa/contexto). Publico; con sesion
 * incluye la empresa activa. El proyecto corre en mono-empresa (core.multiempresa.habilitada=false).
 */
class EmpresaContextoContratoTest extends PruebaDeContrato {

    @Test
    void sinSesionInformaElModoYLaEmpresaPorDefecto() {
        JsonNode contexto = sinAutenticacion().get("/empresa/contexto")
                .ejecutar().esperar(200).json();

        assertFalse(contexto.get("multiempresaHabilitada").asBoolean());
        assertEquals(EmpresasDeSeed.EMPRESA_POR_DEFECTO, contexto.get("uuidEmpresaResuelta").asString());
        assertTrue(contexto.get("empresaActual").isNull());
    }

    @Test
    void conSesionIncluyeLaEmpresaActiva() {
        JsonNode contexto = como(RolBaseEnum.SYSADMIN.name()).get("/empresa/contexto")
                .ejecutar().esperar(200).json();

        assertEquals(EmpresasDeSeed.EMPRESA_POR_DEFECTO, contexto.get("empresaActual").get("uuid").asString().trim());
    }

    // GET /empresa/context se renombro a GET /empresa/contexto (ticket 07): ya no existe
    @Test
    void rutaViejaNoExiste() {
        sinAutenticacion().get("/empresa/context").ejecutar().esperar(404);
    }
}
