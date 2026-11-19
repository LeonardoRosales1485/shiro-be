package com.blue.open.Code.Version;

import com.blue.open.Core.Test.ContratoDeAbmTest;

import java.util.Map;

/**
 * Contrato del ABM de /version (CU-VER-01 a CU-VER-03) con sus casos comunes. No hay borrar: se archiva.
 */
class VersionContratoTest extends ContratoDeAbmTest {

    // Version de los seeds: 1.0 del proyecto Shiro, sin publicar (2026-11-19-insert-versiones-seed.yaml)
    static final String VERSION_DE_SEED = "b7000000-0000-4000-8000-000000000001";

    static final String PROYECTO_SHIRO = "b1000000-0000-4000-8000-000000000001";

    @Override
    protected Class<?> controlador() {
        return VersionController.class;
    }

    @Override
    protected String rol() {
        return "SYSADMIN";
    }

    @Override
    protected String uuidVigente() {
        return VERSION_DE_SEED;
    }

    @Override
    protected Object cuerpoValido() {
        return Map.of("proyectoUuid", PROYECTO_SHIRO, "nombre", "2.0");
    }

    @Override
    protected Object cuerpoInvalido() {
        return Map.of("proyectoUuid", PROYECTO_SHIRO);
    }
}
