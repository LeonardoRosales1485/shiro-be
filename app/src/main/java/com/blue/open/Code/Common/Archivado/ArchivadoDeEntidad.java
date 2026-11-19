package com.blue.open.Code.Common.Archivado;

import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;

import java.sql.Timestamp;

/**
 * Reglas comunes de archivar y restaurar una entidad con borrado logico (archivado unificado, resolucion del 19/11):
 * archivar pasa una vigente a status -1 con quien la archivo y cuando; restaurar pasa una archivada a vigente y limpia
 * esos datos. Las entidades (Tarea, Sprint, Version, ReglaPermiso, Relacion) lo llaman desde sus metodos archivar y
 * restaurar, que ademas guardan su operacion de archivo. Sin dependencias: no es un bean.
 */
public final class ArchivadoDeEntidad {

    private ArchivadoDeEntidad() {
    }

    /*
     * Archiva la entidad con el usuario recibido y la fecha actual. Invariante: solo se archiva una vigente; si no,
     * lanza ExcepcionDeApi con ERROR_INTERNO (el caso de uso lo filtra antes). La referencia identifica la entidad en
     * el mensaje para el desarrollador.
     */
    public static void archivar(EntidadConBorradoLogico entidad, Long idUsuario, String referencia) {
        if (entidad.getStatus() == null || entidad.getStatus() != 0)
            throw new ExcepcionDeApi(CodigoDeErrorKernel.ERROR_INTERNO,
                    "ArchivadoDeEntidad | archivar | " + referencia + " no esta vigente (status " + entidad.getStatus() + ")");
        entidad.setStatus(-1);
        entidad.setFechaDeBorrado(new Timestamp(System.currentTimeMillis()));
        entidad.setIdUsuarioDeBorrado(idUsuario);
    }

    /*
     * Restaura la entidad archivada y limpia la fecha y el usuario de borrado. Invariante: solo se restaura una
     * archivada; si no, lanza ExcepcionDeApi con ERROR_INTERNO.
     */
    public static void restaurar(EntidadConBorradoLogico entidad, String referencia) {
        if (entidad.getStatus() == null || entidad.getStatus() != -1)
            throw new ExcepcionDeApi(CodigoDeErrorKernel.ERROR_INTERNO,
                    "ArchivadoDeEntidad | restaurar | " + referencia + " no esta archivada (status " + entidad.getStatus() + ")");
        entidad.setStatus(0);
        entidad.setFechaDeBorrado(null);
        entidad.setIdUsuarioDeBorrado(null);
    }
}
