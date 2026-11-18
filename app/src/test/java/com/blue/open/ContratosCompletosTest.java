package com.blue.open;

import com.blue.open.Core.Test.VerificacionDeContratos;
import org.junit.jupiter.api.Order;

/**
 * Completitud del Contrato de endpoint: en la corrida completa, todo endpoint tiene que haber sido ejecutado por una
 * prueba de contrato. Corre ultima (@Order + junit-platform.properties).
 */
@Order(Integer.MAX_VALUE)
class ContratosCompletosTest extends VerificacionDeContratos {
}
