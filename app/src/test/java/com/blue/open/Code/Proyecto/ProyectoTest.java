package com.blue.open.Code.Proyecto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Comportamiento de la entidad Proyecto: el contador de claves avanza de a uno desde 0. No depende de datos externos.
 */
class ProyectoTest {

    @Test
    void siguienteNumeroAvanzaElContador() {
        Proyecto proyecto = new Proyecto();

        assertEquals(1L, proyecto.siguienteNumero());
        assertEquals(2L, proyecto.siguienteNumero());
        assertEquals(2L, proyecto.getUltimoNumero());
    }
}
