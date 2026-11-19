package com.blue.open.Code.ReglaPermiso;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Proyeccion.Columna;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO del ABM de reglas de permiso: lo usan obtener, paginar y guardar. Segun tipoSujeto va rolUuid o usuarioUuid,
 * y segun tipoRecurso va proyectoUuid o tableroUuid; el otro queda vacio.
 */
@Setter
@Getter
@NoArgsConstructor
public class ReglaPermisoDTO extends EntidadBaseDTO {

    @Descripcion("A quién se aplica: ROL (un grupo) o USUARIO")
    @NotNull(message = "Elegí si la regla es de un grupo o de un usuario")
    private TipoDeSujeto tipoSujeto;

    @Descripcion("Uuid del grupo (rol) si tipoSujeto es ROL; vacío si no")
    @Columna("rol.uuid")
    private String rolUuid;

    @Descripcion("Nombre del grupo. Solo lectura")
    @Columna("rol.nombre")
    private String rolNombre;

    @Descripcion("Uuid del usuario si tipoSujeto es USUARIO; vacío si no")
    @Columna("usuario.uuid")
    private String usuarioUuid;

    @Descripcion("Nombre de usuario. Solo lectura")
    @Columna("usuario.nombreDeUsuario")
    private String usuarioNombreDeUsuario;

    @Descripcion("Sobre qué se aplica: PROYECTO (y todos sus tableros) o TABLERO")
    @NotNull(message = "Elegí si la regla es sobre un proyecto o sobre un tablero")
    private TipoDeRecurso tipoRecurso;

    @Descripcion("Uuid del proyecto si tipoRecurso es PROYECTO; vacío si no")
    @Columna("proyecto.uuid")
    private String proyectoUuid;

    @Descripcion("Prefijo del proyecto. Solo lectura")
    @Columna("proyecto.prefijo")
    private String proyectoPrefijo;

    @Descripcion("Uuid del tablero si tipoRecurso es TABLERO; vacío si no")
    @Columna("tablero.uuid")
    private String tableroUuid;

    @Descripcion("Código del tipo de tablero (PRINCIPAL, QA, ANALISIS o DISENO). Solo lectura")
    @Columna("tablero.tipoTablero.codigo")
    private String tableroCodigo;

    @Descripcion("Prefijo del proyecto del tablero. Solo lectura")
    @Columna("tablero.proyecto.prefijo")
    private String tableroProyectoPrefijo;

    @Descripcion("Nivel: VER, EDITAR o ARCHIVAR. EDITAR y ARCHIVAR permitidos implican VER")
    @NotNull(message = "Elegí el nivel de la regla")
    private NivelDePermiso nivel;

    @Descripcion("Efecto: PERMITIR o NEGAR")
    @NotNull(message = "Elegí si la regla permite o niega")
    private EfectoDePermiso efecto;
}
