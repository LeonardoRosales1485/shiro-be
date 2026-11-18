package com.blue.open.Code.Configuracion;

/**
 * Configuraciones de los seeds de Liquibase (2026-10-08-insert-configuracion-demo-seed.yaml) que usan las pruebas de contrato de
 * configuraciones.
 */
final class ConfiguracionesDeSeed {

    // Configuracion privada (global = false): solo la ve quien tiene CONFIGURACION_VER
    static final String PRIVADA = "demo.banner";

    private ConfiguracionesDeSeed() {
    }
}
