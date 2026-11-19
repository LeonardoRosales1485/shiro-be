package com.blue.open.Code.Autorizacion;

import com.blue.open.Code.ReglaPermiso.EfectoDePermiso;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;

import java.util.List;
import java.util.Objects;

/**
 * Resolucion de un permiso con las reglas de un usuario y de sus grupos (Spec 2), sin acceso a datos:
 * 1. Sobre el tablero: si hay reglas del usuario, gana negar entre ellas; si no, entre las de sus grupos gana negar,
 * y si alguna permite, permitido. 2. Si el tablero no decide, lo mismo sobre el proyecto. 3. Sin reglas, negado.
 * EDITAR y ARCHIVAR permitidos implican VER; negar EDITAR o ARCHIVAR no niega VER. El super admin y los
 * Administradores no pasan por aca: los resuelve ResolutorDePermisosDeRecurso.
 */
public final class EvaluadorDeReglasDePermiso {

    private EvaluadorDeReglasDePermiso() {
    }

    // Si las reglas permiten el nivel sobre el tablero (null para evaluar solo el proyecto) del proyecto recibido
    public static boolean puede(List<ReglaEvaluable> reglas, NivelDePermiso nivel, Long idTablero, Long idProyecto) {
        if (idTablero != null) {
            Boolean decision = decidir(reglas.stream().filter(r -> idTablero.equals(r.idTablero())).toList(), nivel);
            if (decision != null) {
                return decision;
            }
        }
        Boolean decision = decidir(reglas.stream().filter(r -> Objects.equals(idProyecto, r.idProyecto())
                && r.idTablero() == null).toList(), nivel);
        return Boolean.TRUE.equals(decision);
    }

    // Decision con las reglas de un mismo recurso: las del usuario antes que las de sus grupos. Null si ninguna aplica
    private static Boolean decidir(List<ReglaEvaluable> reglasDelRecurso, NivelDePermiso nivel) {
        List<ReglaEvaluable> aplicables = reglasDelRecurso.stream().filter(r -> aplica(r, nivel)).toList();
        List<ReglaEvaluable> delUsuario = aplicables.stream().filter(r -> Boolean.TRUE.equals(r.esDelUsuario())).toList();
        List<ReglaEvaluable> decisivas = delUsuario.isEmpty() ? aplicables : delUsuario;
        if (decisivas.isEmpty()) {
            return null;
        }
        return decisivas.stream().noneMatch(r -> r.efecto() == EfectoDePermiso.NEGAR);
    }

    // Una regla aplica a su propio nivel; para VER tambien aplican EDITAR y ARCHIVAR, solo cuando permiten
    private static boolean aplica(ReglaEvaluable regla, NivelDePermiso nivel) {
        return regla.nivel() == nivel
                || (nivel == NivelDePermiso.VER && regla.efecto() == EfectoDePermiso.PERMITIR);
    }
}
