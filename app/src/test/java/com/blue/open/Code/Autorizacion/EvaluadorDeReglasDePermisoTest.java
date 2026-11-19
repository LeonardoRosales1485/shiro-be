package com.blue.open.Code.Autorizacion;

import com.blue.open.Code.ReglaPermiso.EfectoDePermiso;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.blue.open.Code.ReglaPermiso.EfectoDePermiso.NEGAR;
import static com.blue.open.Code.ReglaPermiso.EfectoDePermiso.PERMITIR;
import static com.blue.open.Code.ReglaPermiso.NivelDePermiso.ARCHIVAR;
import static com.blue.open.Code.ReglaPermiso.NivelDePermiso.EDITAR;
import static com.blue.open.Code.ReglaPermiso.NivelDePermiso.VER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tabla de casos de Spec 2 (T2.3 del plan): como se resuelve un permiso de un usuario sobre el tablero T del
 * proyecto P con sus reglas y las de sus grupos. No depende de datos externos.
 */
class EvaluadorDeReglasDePermisoTest {

    private static final long P = 1L;

    private static final long T = 10L;

    private static final long OTRO_PROYECTO = 2L;

    private static final long OTRO_TABLERO = 20L;

    @Test
    void sinReglasSeNiega() {
        assertFalse(puede(List.of(), VER));
    }

    @Test
    void laReglaDelUsuarioSobreElTableroDecide() {
        assertTrue(puede(List.of(usuarioEnTablero(VER, PERMITIR)), VER));
        assertFalse(puede(List.of(usuarioEnTablero(VER, NEGAR)), VER));
    }

    @Test
    void laReglaDelUsuarioGanaALaDeSusGrupos() {
        assertFalse(puede(List.of(usuarioEnTablero(EDITAR, NEGAR), grupoEnTablero(EDITAR, PERMITIR)), EDITAR));
        assertTrue(puede(List.of(usuarioEnTablero(EDITAR, PERMITIR), grupoEnTablero(EDITAR, NEGAR)), EDITAR));
    }

    @Test
    void entreGruposGanaNegar() {
        assertFalse(puede(List.of(grupoEnTablero(VER, PERMITIR), grupoEnTablero(VER, NEGAR)), VER));
        assertTrue(puede(List.of(grupoEnTablero(VER, PERMITIR)), VER));
    }

    @Test
    void sinReglasEnElTableroValenLasDelProyecto() {
        assertTrue(puede(List.of(grupoEnProyecto(VER, PERMITIR)), VER));
        assertFalse(puede(List.of(usuarioEnProyecto(VER, NEGAR), grupoEnProyecto(VER, PERMITIR)), VER));
    }

    @Test
    void elTableroGanaAlProyecto() {
        assertFalse(puede(List.of(grupoEnProyecto(VER, PERMITIR), grupoEnTablero(VER, NEGAR)), VER));
        assertTrue(puede(List.of(usuarioEnProyecto(VER, NEGAR), grupoEnTablero(VER, PERMITIR)), VER));
    }

    @Test
    void editarYArchivarImplicanVerPeroSonIndependientesEntreSi() {
        assertTrue(puede(List.of(grupoEnProyecto(EDITAR, PERMITIR)), VER));
        assertTrue(puede(List.of(grupoEnProyecto(ARCHIVAR, PERMITIR)), VER));
        assertFalse(puede(List.of(grupoEnProyecto(EDITAR, PERMITIR)), ARCHIVAR));
        assertFalse(puede(List.of(grupoEnProyecto(ARCHIVAR, PERMITIR)), EDITAR));
    }

    @Test
    void negarEditarNoNiegaVer() {
        assertTrue(puede(List.of(grupoEnProyecto(VER, PERMITIR), usuarioEnProyecto(EDITAR, NEGAR)), VER));
        assertFalse(puede(List.of(grupoEnProyecto(VER, PERMITIR), usuarioEnProyecto(EDITAR, NEGAR)), EDITAR));
    }

    @Test
    void lasReglasDeOtrosRecursosNoCuentan() {
        assertFalse(puede(List.of(new ReglaEvaluable(true, null, OTRO_TABLERO, VER, PERMITIR),
                new ReglaEvaluable(false, OTRO_PROYECTO, null, VER, PERMITIR)), VER));
    }

    @Test
    void sobreElProyectoSoloCuentanLasReglasDelProyecto() {
        assertFalse(EvaluadorDeReglasDePermiso.puede(List.of(grupoEnTablero(VER, PERMITIR)), VER, null, P));
        assertTrue(EvaluadorDeReglasDePermiso.puede(List.of(grupoEnProyecto(VER, PERMITIR)), VER, null, P));
    }

    private static boolean puede(List<ReglaEvaluable> reglas, NivelDePermiso nivel) {
        return EvaluadorDeReglasDePermiso.puede(reglas, nivel, T, P);
    }

    private static ReglaEvaluable usuarioEnTablero(NivelDePermiso nivel, EfectoDePermiso efecto) {
        return new ReglaEvaluable(true, null, T, nivel, efecto);
    }

    private static ReglaEvaluable grupoEnTablero(NivelDePermiso nivel, EfectoDePermiso efecto) {
        return new ReglaEvaluable(false, null, T, nivel, efecto);
    }

    private static ReglaEvaluable usuarioEnProyecto(NivelDePermiso nivel, EfectoDePermiso efecto) {
        return new ReglaEvaluable(true, P, null, nivel, efecto);
    }

    private static ReglaEvaluable grupoEnProyecto(NivelDePermiso nivel, EfectoDePermiso efecto) {
        return new ReglaEvaluable(false, P, null, nivel, efecto);
    }
}
