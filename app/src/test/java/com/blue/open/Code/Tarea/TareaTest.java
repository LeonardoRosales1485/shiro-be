package com.blue.open.Code.Tarea;

import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void archivarYRestaurarCambianElStatusYLaOperacion() {
        Tarea tarea = new Tarea();

        tarea.archivar(7L, 40L);

        assertEquals(-1, tarea.getStatus());
        assertEquals(7L, tarea.getIdUsuarioDeBorrado());
        assertEquals(40L, tarea.getIdOperacionArchivo());
        assertNotNull(tarea.getFechaDeBorrado());

        tarea.restaurar();

        assertEquals(0, tarea.getStatus());
        assertNull(tarea.getIdUsuarioDeBorrado());
        assertNull(tarea.getIdOperacionArchivo());
        assertNull(tarea.getFechaDeBorrado());
    }

    @Test
    void soloSeArchivaLoVigenteYSoloSeRestauraLoArchivado() {
        Tarea tarea = new Tarea();

        assertThrows(ExcepcionDeApi.class, tarea::restaurar);
        tarea.archivar(7L, 40L);
        assertThrows(ExcepcionDeApi.class, () -> tarea.archivar(7L, 41L));
        assertEquals(40L, tarea.getIdOperacionArchivo());
    }
}
