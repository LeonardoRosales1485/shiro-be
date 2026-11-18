package com.blue.open.Code.Empresa;

/**
 * Datos de empresas que las pruebas de contrato de /empresa toman de los seeds de Liquibase o necesitan validos.
 */
final class EmpresasDeSeed {

    // Empresa operativa por defecto de los datos requeridos (2026-10-09-insert-empresas-required-data.yaml)
    static final String EMPRESA_POR_DEFECTO = "2";

    // CUIT con digito verificador valido (mismo algoritmo que ValidadorDeCuitService)
    static final String CUIT_VALIDO = "20123456786";

    private EmpresasDeSeed() {
    }
}
