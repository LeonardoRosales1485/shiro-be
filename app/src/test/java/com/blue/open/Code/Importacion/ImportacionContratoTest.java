package com.blue.open.Code.Importacion;

import com.blue.open.Core.Test.ContratoDeAbmTest;

import java.util.Map;

/**
 * Contrato del ABM de /importacion (CU-IMP-05 y CU-IMP-06): solo obtener y listar; las importaciones se crean al
 * conectar con Jira y no se borran.
 */
class ImportacionContratoTest extends ContratoDeAbmTest {

    // Importacion de los seeds: conectada a un Jira de ejemplo, sin mapear (2026-11-19-insert-importaciones-seed.yaml)
    static final String IMPORTACION_DE_SEED = "b8000000-0000-4000-8000-000000000001";

    @Override
    protected Class<?> controlador() {
        return ImportacionController.class;
    }

    @Override
    protected String rol() {
        return "SYSADMIN";
    }

    @Override
    protected String uuidVigente() {
        return IMPORTACION_DE_SEED;
    }

    @Override
    protected Object cuerpoValido() {
        return Map.of();
    }

    @Override
    protected Object cuerpoInvalido() {
        return Map.of();
    }
}
