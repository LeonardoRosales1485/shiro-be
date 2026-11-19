package com.blue.open.Code.Tarea;

import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comportamiento de la entidad Tarea: la clave se arma con el prefijo y el numero, y no cambia una vez asignada.
 * No depende de datos externos.
 */
class TareaTest {

    @Test
    void numerarArmaLaClaveConPrefijoYNumero() {
        Tarea tarea = new Tarea();

        tarea.numerar("SHI", 123);

        assertEquals(123L, tarea.getNumero());
        assertEquals("SHI-123", tarea.getClave());
    }

    @Test
    void laClaveNoCambiaUnaVezAsignada() {
        Tarea tarea = new Tarea();
        tarea.numerar("SHI", 1);

        assertThrows(ExcepcionDeApi.class, () -> tarea.numerar("SHI", 2));
        assertEquals("SHI-1", tarea.getClave());
    }
}
