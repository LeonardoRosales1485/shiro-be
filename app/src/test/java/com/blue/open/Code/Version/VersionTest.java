package com.blue.open.Code.Version;

import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comportamiento de la entidad Version: archivar la deja con status -1, quien la archivo, cuando y la operacion de
 * archivo; solo se archiva una vigente y
 * restaurar vuelve una archivada a vigente (archivado unificado, resolucion del 19/11). No depende de datos externos.
 */
class VersionTest {

    @Test
    void archivarDejaLosDatosDeLaOperacion() {
        Version entidad = new Version();
        entidad.setStatus(0);

        entidad.archivar(7L, 40L);

        assertEquals(-1, entidad.getStatus());
        assertEquals(7L, entidad.getIdUsuarioDeBorrado());
        assertNotNull(entidad.getFechaDeBorrado());
        assertEquals(40L, entidad.getIdOperacionArchivo());
    }

    @Test
    void soloSeArchivaUnaVigente() {
        Version entidad = new Version();
        entidad.setStatus(-1);

        assertThrows(ExcepcionDeApi.class, () -> entidad.archivar(7L, 40L));
    }

    @Test
    void restaurarLimpiaLosDatosDeLaOperacion() {
        Version version = new Version();
        version.setStatus(0);
        version.archivar(7L, 40L);

        version.restaurar();

        assertEquals(0, version.getStatus());
        assertEquals(null, version.getIdUsuarioDeBorrado());
        assertEquals(null, version.getFechaDeBorrado());
        assertEquals(null, version.getIdOperacionArchivo());
        assertThrows(ExcepcionDeApi.class, version::restaurar);
    }
}
