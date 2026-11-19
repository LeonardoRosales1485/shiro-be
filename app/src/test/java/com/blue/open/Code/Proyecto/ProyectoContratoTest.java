package com.blue.open.Code.Proyecto;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.ContratoDeAbmTest;

import java.util.Map;

/**
 * Contrato del ABM de /proyecto (CU-PRY-01 a CU-PRY-03) con sus casos comunes. No hay borrar: los proyectos se
 * archivan (T1.8). Los casos propios del alta y la edicion estan en ProyectoGuardarContratoTest.
 */
class ProyectoContratoTest extends ContratoDeAbmTest {

    @Override
    protected Class<?> controlador() {
        return ProyectoController.class;
    }

    @Override
    protected String rol() {
        return RolBaseEnum.SYSADMIN.name();
    }

    @Override
    protected String uuidVigente() {
        return ProyectosDeSeed.SHIRO;
    }

    @Override
    protected Object cuerpoValido() {
        return Map.of("nombre", "Portal de Trámites", "prefijo", "PTR", "descripcion", "Trámites en línea");
    }

    @Override
    protected Object cuerpoInvalido() {
        return Map.of("descripcion", "Sin nombre ni prefijo");
    }
}
