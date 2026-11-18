package com.blue.open.Code.Empresa;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.ContratoDeAbmTest;

import java.util.Map;

/**
 * Contrato del ABM de /empresa (CU-EMP-01 a CU-EMP-04) con sus casos comunes. Los casos propios
 * de la entidad estan en EmpresaGuardarContratoTest.
 */
class EmpresaContratoTest extends ContratoDeAbmTest {

    @Override
    protected Class<?> controlador() {
        return EmpresaController.class;
    }

    @Override
    protected String rol() {
        return RolBaseEnum.SYSADMIN.name();
    }

    @Override
    protected String uuidVigente() {
        return EmpresasDeSeed.EMPRESA_POR_DEFECTO;
    }

    @Override
    protected Object cuerpoValido() {
        return Map.of("nombre", "Empresa de prueba", "descripcion", "Empresa creada por la prueba de contrato",
                "ruta", "empresa-de-prueba", "codigo", "EMP-PRUEBA", "cuit", EmpresasDeSeed.CUIT_VALIDO);
    }

    @Override
    protected Object cuerpoInvalido() {
        return Map.of("descripcion", "Sin nombre");
    }

}
