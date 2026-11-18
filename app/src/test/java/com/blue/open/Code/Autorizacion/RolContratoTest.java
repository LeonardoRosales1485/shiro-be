package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.ContratoDeAbmTest;

import java.util.List;
import java.util.Map;

/**
 * Contrato del ABM de /rol (CU-ROL-01 a CU-ROL-04) con sus casos comunes. Los casos propios de
 * cada caso de uso estan en Rol{CasoDeUso}ContratoTest.
 */
class RolContratoTest extends ContratoDeAbmTest {

    @Override
    protected Class<?> controlador() {
        return RolController.class;
    }

    @Override
    protected String rol() {
        return RolBaseEnum.SYSADMIN.name();
    }

    @Override
    protected String uuidVigente() {
        return RolesDeSeed.CLIENTE;
    }

    @Override
    protected Object cuerpoValido() {
        return Map.of("nombre", "Rol de prueba", "porDefecto", 0, "permisos", List.of());
    }

    @Override
    protected Object cuerpoInvalido() {
        return Map.of("porDefecto", 0);
    }

}
