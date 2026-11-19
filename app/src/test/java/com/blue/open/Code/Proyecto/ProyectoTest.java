package com.blue.open.Code.Proyecto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Comportamiento de la entidad Proyecto: el contador de claves avanza de a uno desde 0 y
 * el importador de Jira solo lo puede adelantar hasta la clave mas alta importada. No depende de datos externos.
 */
class ProyectoTest {

    @Test
    void siguienteNumeroAvanzaElContador() {
        Proyecto proyecto = new Proyecto();

        assertEquals(1L, proyecto.siguienteNumero());
        assertEquals(2L, proyecto.siguienteNumero());
        assertEquals(2L, proyecto.getUltimoNumero());
    }

    @Test
    void continuarDesdeSoloAdelantaElContador() {
        Proyecto proyecto = new Proyecto();

        proyecto.continuarNumeracionDesde(9);
        proyecto.continuarNumeracionDesde(4);

        assertEquals(9L, proyecto.getUltimoNumero());
        assertEquals(10L, proyecto.siguienteNumero());
    }
}
