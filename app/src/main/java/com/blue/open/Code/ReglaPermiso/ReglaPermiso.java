package com.blue.open.Code.ReglaPermiso;

import com.blue.open.Code.Common.Archivado.ArchivadoDeEntidad;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Core.Identity.Entidad.Rol;
import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Regla de permiso por recurso (Spec 2): un sujeto (un Rol, que hace de grupo, o un Usuario) tiene permitido o
 * negado un nivel (ver, editar o archivar) sobre un recurso (un Proyecto o un Tablero). La resuelve
 * ResolutorDePermisosDeRecurso. Segun el tipo de sujeto se usa rol o usuario; segun el tipo de recurso, proyecto o
 * tablero.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_regla_permiso")
public class ReglaPermiso extends EntidadConBorradoLogico {

    @Enumerated(EnumType.STRING)
    private TipoDeSujeto tipoSujeto;

    // Grupo de la regla, si el sujeto es ROL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol")
    private Rol rol;

    // Usuario de la regla, si el sujeto es USUARIO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private TipoDeRecurso tipoRecurso;

    // Proyecto de la regla, si el recurso es PROYECTO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    // Tablero de la regla, si el recurso es TABLERO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tablero")
    private Tablero tablero;

    @Enumerated(EnumType.STRING)
    private NivelDePermiso nivel;

    @Enumerated(EnumType.STRING)
    private EfectoDePermiso efecto;

    // Operacion de archivo que la archivo; null si esta vigente
    @Setter(AccessLevel.NONE)
    private Long idOperacionArchivo;

    // Valores antes de editarla, para el historial. No se persiste: lo completa ReglaPermisoService
    @Transient
    private Map<String, String> valoresAnteriores = new LinkedHashMap<>();

    /*
     * Archiva la entidad (status -1) como parte de la operacion recibida, con el usuario y la fecha de borrado.
     * Invariante: solo se archiva una vigente; si no, lanza ExcepcionDeApi con ERROR_INTERNO.
     */
    public void archivar(Long idUsuario, Long idOperacion) {
        ArchivadoDeEntidad.archivar(this, idUsuario, "ReglaPermiso " + getUuid());
        this.idOperacionArchivo = idOperacion;
    }
}
