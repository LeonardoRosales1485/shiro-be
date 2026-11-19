package com.blue.open.Code.Sprint;

import com.blue.open.Code.Common.Archivado.ArchivadoDeEntidad;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sprint de un Proyecto (Spec 1 y T5.2): incluye tareas de todos los tableros (Tarea.sprint). Solo puede haber uno
 * activo por proyecto (indice unico parcial en la base). Al cerrarse, lo terminado queda en el sprint y lo pendiente
 * va al backlog o a otro sprint; el cierre guarda quien lo cerro y una foto de cada tarea (TareaDeSprintCerrado).
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_sprint")
public class Sprint extends EntidadConBorradoLogico {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    private String nombre;

    // Fecha de inicio planificada, sin hora
    private LocalDate inicio;

    // Fecha de fin planificada, sin hora
    private LocalDate fin;

    // Estado del sprint. Solo lo cambian iniciar y cerrar
    @Enumerated(EnumType.STRING)
    private EstadoDeSprint estado = EstadoDeSprint.PLANIFICADO;

    // Usuario que cerro el sprint; null si no esta cerrado. Solo lo asigna cerrar
    @Setter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_cierre")
    private Usuario cerradoPor;

    // Fecha y hora del cierre; null si no esta cerrado. Solo la asigna cerrar
    @Setter(AccessLevel.NONE)
    private Timestamp fechaDeCierre;

    // Operacion de archivo que lo archivo; null si esta vigente
    @Setter(AccessLevel.NONE)
    private Long idOperacionArchivo;

    // Valores antes de editarlo, para el historial. No se persiste: lo completa SprintService
    @Transient
    private Map<String, String> valoresAnteriores = new LinkedHashMap<>();

    /*
     * Cierra el sprint activo y guarda quien lo cerro y cuando (propuesta 46 resuelta el 19/11). Invariante: solo se
     * cierra un sprint activo; si no, lanza ExcepcionDeApi con SPRINT_NO_ACTIVO.
     */
    public void cerrar(Usuario quien) {
        if (estado != EstadoDeSprint.ACTIVO)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.SPRINT_NO_ACTIVO, "Sprint | cerrar | " + getUuid() + " en " + estado);
        this.estado = EstadoDeSprint.CERRADO;
        this.cerradoPor = quien;
        this.fechaDeCierre = new Timestamp(System.currentTimeMillis());
    }

    /*
     * Restaura el sprint archivado (status 0) y limpia los datos de borrado y la operacion. Invariante: solo se
     * restaura uno archivado; si no, lanza ExcepcionDeApi con ERROR_INTERNO.
     */
    public void restaurar() {
        ArchivadoDeEntidad.restaurar(this, "Sprint " + getUuid());
        this.idOperacionArchivo = null;
    }

    /*
     * Archiva la entidad (status -1) como parte de la operacion recibida, con el usuario y la fecha de borrado.
     * Invariante: solo se archiva una vigente; si no, lanza ExcepcionDeApi con ERROR_INTERNO.
     */
    public void archivar(Long idUsuario, Long idOperacion) {
        ArchivadoDeEntidad.archivar(this, idUsuario, "Sprint " + getUuid());
        this.idOperacionArchivo = idOperacion;
    }
}
