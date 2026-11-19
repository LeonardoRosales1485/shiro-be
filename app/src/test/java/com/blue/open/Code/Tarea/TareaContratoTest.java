package com.blue.open.Code.Tarea;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.ContratoDeAbmTest;

import java.util.Map;

/**
 * Contrato del ABM de /tarea (CU-TAR-01 a CU-TAR-03) con sus casos comunes. No hay borrar: las tareas se archivan
 * (T1.8). Los casos propios del alta y la edicion estan en TareaGuardarContratoTest.
 */
class TareaContratoTest extends ContratoDeAbmTest {

    @Override
    protected Class<?> controlador() {
        return TareaController.class;
    }

    @Override
    protected String rol() {
        return RolBaseEnum.SYSADMIN.name();
    }

    @Override
    protected String uuidVigente() {
        return TareasDeSeed.SHI_1;
    }

    @Override
    protected Object cuerpoValido() {
        return Map.of("tableroUuid", TareasDeSeed.TABLERO_PRINCIPAL, "tipoCodigo", "TAREA",
                "titulo", "Configurar el pipeline de integración");
    }

    @Override
    protected Object cuerpoInvalido() {
        return Map.of("tableroUuid", TareasDeSeed.TABLERO_PRINCIPAL, "tipoCodigo", "TAREA");
    }
}
