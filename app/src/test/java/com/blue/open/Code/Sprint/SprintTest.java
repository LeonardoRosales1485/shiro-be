package com.blue.open.Code.Sprint;

import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comportamiento de la entidad Sprint: archivar la deja con status -1, quien la archivo, cuando y la operacion de
 * archivo; solo se archiva una vigente (archivado unificado, resolucion del 19/11). No depende de datos externos.
 */
class SprintTest {

    @Test
    void archivarDejaLosDatosDeLaOperacion() {
        Sprint entidad = new Sprint();
        entidad.setStatus(0);

        entidad.archivar(7L, 40L);

        assertEquals(-1, entidad.getStatus());
        assertEquals(7L, entidad.getIdUsuarioDeBorrado());
        assertNotNull(entidad.getFechaDeBorrado());
        assertEquals(40L, entidad.getIdOperacionArchivo());
    }

    @Test
    void soloSeArchivaUnaVigente() {
        Sprint entidad = new Sprint();
        entidad.setStatus(-1);

        assertThrows(ExcepcionDeApi.class, () -> entidad.archivar(7L, 40L));
    }
}
