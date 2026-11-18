package com.blue.open.Code.Empresa;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * CU-EMP-03: alta o edicion de una empresa (POST /empresa/guardar). El CUIT es opcional, pero si viene se valida su
 * digito verificador (ValidadorDeCuitService). "Sin autenticacion" lo genera core-test.
 */
class EmpresaGuardarContratoTest extends PruebaDeContrato {

    @Test
    void altaConCuitValido() {
        JsonNode empresa = guardar(Map.of("nombre", "Empresa con CUIT", "ruta", "empresa-con-cuit", "codigo", "EMP-CUIT",
                "cuit", EmpresasDeSeed.CUIT_VALIDO)).esperar(200).json();

        assertEquals("Empresa con CUIT", empresa.get("nombre").asString());
        assertEquals(EmpresasDeSeed.CUIT_VALIDO, empresa.get("cuit").asString());
        assertFalse(empresa.has("id"));
    }

    @Test
    void guardarYObtenerRespondenLaMismaForma() {
        JsonNode guardada = guardar(Map.of("nombre", "Empresa misma forma", "descripcion", "Descripcion",
                "cuit", EmpresasDeSeed.CUIT_VALIDO)).esperar(200).json();

        JsonNode obtenida = como(RolBaseEnum.SYSADMIN.name())
                .get("/empresa/uuid/{uuid}", guardada.get("uuid").asString()).ejecutar().esperar(200).json();
        assertEquals(obtenida, guardada);
    }

    @Test
    void cuitConDigitoVerificadorInvalido() {
        JsonNode error = guardar(Map.of("nombre", "Empresa con CUIT invalido", "cuit", "20-12345678-0"))
                .esperarError(400, "CUIT_INVALIDO").json();

        assertEquals("CUIT inválido", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void cuitConFormatoInvalido() {
        JsonNode error = guardar(Map.of("nombre", "Empresa con CUIT mal formado", "cuit", "123"))
                .esperarError(400, "FORMULARIO_INVALIDO").json();

        assertEquals("cuit", error.get("errores").get(0).get("nombre").asString());
    }

    // POST /empresa/guardar como SYSADMIN
    private Respuesta guardar(Map<String, Object> cuerpo) {
        return como(RolBaseEnum.SYSADMIN.name()).post("/empresa/guardar").json(cuerpo)
                .ejecutar();
    }
}
