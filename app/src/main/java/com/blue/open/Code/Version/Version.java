package com.blue.open.Code.Version;

import com.blue.open.Code.Common.Archivado.ArchivadoDeEntidad;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Synchronize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Version de un Proyecto (Spec 1 y T5.3): agrupa tareas (TareaVersion) y genera su changelog. Al publicarse queda
 * fija: guarda la fecha de publicacion elegida y el changelog de ese momento, y no cambia mas.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_version")
// La cantidad de tareas sale de shr_tarea_version y shr_tarea: Hibernate vacia lo pendiente en esas tablas antes de leerla
@Synchronize({"shr_tarea_version", "shr_tarea"})
public class Version extends EntidadConBorradoLogico {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    private String nombre;

    // Estado. Solo lo cambia publicar
    @Enumerated(EnumType.STRING)
    private EstadoDeVersion estado = EstadoDeVersion.SIN_PUBLICAR;

    // Fecha de publicacion elegida al publicar, sin hora; null si no esta publicada
    private LocalDate fechaDePublicacion;

    // Changelog en Markdown guardado al publicar; null si no esta publicada (se arma en el momento)
    @Column(columnDefinition = "TEXT")
    private String changelog;

    // Usuario que publico la version; null si no esta publicada. Solo lo asigna publicar
    @Setter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_publicacion")
    private Usuario publicadaPor;

    // Cantidad de tareas vigentes asignadas (propuesta 47 resuelta el 19/11). Solo lectura: la calcula la base
    @Setter(AccessLevel.NONE)
    @Formula("(SELECT COUNT(*) FROM shr_tarea_version tv JOIN shr_tarea t ON t.id = tv.id_tarea "
            + "WHERE tv.id_version = id AND tv.status = 0 AND t.status = 0)")
    private Integer cantidadDeTareas;

    // Operacion de archivo que la archivo; null si esta vigente
    @Setter(AccessLevel.NONE)
    private Long idOperacionArchivo;

    /*
     * Publica la version con la fecha y el changelog recibidos y guarda quien la publico. Invariante: solo se publica
     * una sin publicar; si no, lanza ExcepcionDeApi con VERSION_PUBLICADA.
     */
    public void publicar(LocalDate fecha, String changelogFijo, Usuario quien) {
        if (estado != EstadoDeVersion.SIN_PUBLICAR)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.VERSION_PUBLICADA, "Version | publicar | " + getUuid());
        this.estado = EstadoDeVersion.PUBLICADA;
        this.fechaDePublicacion = fecha;
        this.changelog = changelogFijo;
        this.publicadaPor = quien;
    }

    /*
     * Archiva la entidad (status -1) como parte de la operacion recibida, con el usuario y la fecha de borrado.
     * Invariante: solo se archiva una vigente; si no, lanza ExcepcionDeApi con ERROR_INTERNO.
     */
    public void archivar(Long idUsuario, Long idOperacion) {
        ArchivadoDeEntidad.archivar(this, idUsuario, "Version " + getUuid());
        this.idOperacionArchivo = idOperacion;
    }

    /*
     * Restaura la entidad archivada (status 0) y limpia los datos de borrado y la operacion. Invariante: solo se
     * restaura una archivada; si no, lanza ExcepcionDeApi con ERROR_INTERNO.
     */
    public void restaurar() {
        ArchivadoDeEntidad.restaurar(this, "Version " + getUuid());
        this.idOperacionArchivo = null;
    }
}
