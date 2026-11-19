package com.blue.open.Code.Sprint;

import com.blue.open.Core.Test.ContratoDeAbmTest;

import java.util.Map;

/**
 * Contrato del ABM de /sprint (CU-SPR-01 a CU-SPR-03) con sus casos comunes. No hay borrar: se archiva.
 */
class SprintContratoTest extends ContratoDeAbmTest {

    // Sprint de los seeds: Sprint 1 del proyecto Shiro, planificado (2026-11-19-insert-sprints-seed.yaml)
    static final String SPRINT_DE_SEED = "b6000000-0000-4000-8000-000000000001";

    static final String PROYECTO_SHIRO = "b1000000-0000-4000-8000-000000000001";

    @Override
    protected Class<?> controlador() {
        return SprintController.class;
    }

    @Override
    protected String rol() {
        return "SYSADMIN";
    }

    @Override
    protected String uuidVigente() {
        return SPRINT_DE_SEED;
    }

    @Override
    protected Object cuerpoValido() {
        return Map.of("proyectoUuid", PROYECTO_SHIRO, "nombre", "Sprint de contrato", "inicio", "2026-12-01",
                "fin", "2026-12-14");
    }

    @Override
    protected Object cuerpoInvalido() {
        return Map.of("proyectoUuid", PROYECTO_SHIRO);
    }
}
