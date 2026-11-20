package com.blue.open.Code.Sprint;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
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

    @Test
    void cerrarUnoActivoGuardaQuienYCuando() {
        Sprint sprint = new Sprint();
        sprint.iniciar();
        Usuario quien = new Usuario();

        sprint.cerrar(quien);

        assertEquals(EstadoDeSprint.CERRADO, sprint.getEstado());
        assertSame(quien, sprint.getCerradoPor());
        assertNotNull(sprint.getFechaDeCierre());
    }

    @Test
    void soloSeCierraUnoActivo() {
        Sprint sprint = new Sprint();

        ExcepcionDeApi error = assertThrows(ExcepcionDeApi.class, () -> sprint.cerrar(new Usuario()));
        assertEquals(CodigoDeErrorDelProyecto.SPRINT_NO_ACTIVO, error.getCodigo());
    }

    @Test
    void soloSeIniciaUnoPlanificado() {
        Sprint sprint = new Sprint();
        sprint.iniciar();

        assertEquals(EstadoDeSprint.ACTIVO, sprint.getEstado());
        ExcepcionDeApi error = assertThrows(ExcepcionDeApi.class, sprint::iniciar);
        assertEquals(CodigoDeErrorDelProyecto.SPRINT_NO_PLANIFICADO, error.getCodigo());
    }

    @Test
    void restaurarVuelveUnArchivadoAVigente() {
        Sprint sprint = new Sprint();
        sprint.setStatus(0);
        sprint.archivar(7L, 40L);

        sprint.restaurar();

        assertEquals(0, sprint.getStatus());
        assertNull(sprint.getIdOperacionArchivo());
        assertThrows(ExcepcionDeApi.class, sprint::restaurar);
    }
}
