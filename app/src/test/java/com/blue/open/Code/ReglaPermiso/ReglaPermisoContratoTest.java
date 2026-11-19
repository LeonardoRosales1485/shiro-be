package com.blue.open.Code.ReglaPermiso;

import com.blue.open.Core.Test.ContratoDeAbmTest;

import java.util.Map;

/**
 * Contrato del ABM de /regla-permiso (CU-RPE-01 a CU-RPE-03) con sus casos comunes. No hay borrar: se archiva. Los
 * casos propios estan en ReglaPermisoGuardarContratoTest.
 */
class ReglaPermisoContratoTest extends ContratoDeAbmTest {

    // Regla de los seeds: el grupo CLIENTE ve el proyecto Shiro (2026-11-19-insert-reglas-de-permiso-seed.yaml)
    static final String REGLA_DE_SEED = "b5000000-0000-4000-8000-000000000001";

    static final String ROL_CLIENTE = "90000000-0000-4000-8000-000000000009";

    static final String PROYECTO_SHIRO = "b1000000-0000-4000-8000-000000000001";

    static final String TABLERO_QA = "b2000000-0000-4000-8000-000000000002";

    @Override
    protected Class<?> controlador() {
        return ReglaPermisoController.class;
    }

    @Override
    protected String rol() {
        return "SYSADMIN";
    }

    @Override
    protected String uuidVigente() {
        return REGLA_DE_SEED;
    }

    @Override
    protected Object cuerpoValido() {
        return Map.of("tipoSujeto", "ROL", "rolUuid", ROL_CLIENTE, "tipoRecurso", "TABLERO", "tableroUuid", TABLERO_QA,
                "nivel", "EDITAR", "efecto", "PERMITIR");
    }

    @Override
    protected Object cuerpoInvalido() {
        return Map.of("tipoSujeto", "ROL", "rolUuid", ROL_CLIENTE, "tipoRecurso", "TABLERO");
    }
}
