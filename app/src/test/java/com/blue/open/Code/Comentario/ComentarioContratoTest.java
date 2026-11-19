package com.blue.open.Code.Comentario;

import com.blue.open.Core.Test.ContratoDeAbmTest;

import java.util.Map;

/**
 * Contrato del ABM de /comentario (CU-COM-01 a CU-COM-03) con sus casos comunes. No hay borrar. Los casos propios
 * estan en ComentarioGuardarContratoTest.
 */
class ComentarioContratoTest extends ContratoDeAbmTest {

    // Comentario sembrado en SHI-1 (2026-11-18-insert-comentarios-seed.yaml) y la tarea SHI-1
    static final String COMENTARIO_DE_SEED = "b4000000-0000-4000-8000-000000000001";

    static final String SHI_1 = "b3000000-0000-4000-8000-000000000001";

    @Override
    protected Class<?> controlador() {
        return ComentarioController.class;
    }

    @Override
    protected String rol() {
        return "SYSADMIN";
    }

    @Override
    protected String uuidVigente() {
        return COMENTARIO_DE_SEED;
    }

    @Override
    protected Object cuerpoValido() {
        return Map.of("tareaUuid", SHI_1, "cuerpo", "Revisé el **tablero**: está bien.");
    }

    @Override
    protected Object cuerpoInvalido() {
        return Map.of("tareaUuid", SHI_1);
    }
}
