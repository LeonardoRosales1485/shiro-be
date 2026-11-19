package com.blue.open.Code.Sprint;

import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Foto de una tarea al cerrar su Sprint (propuesta 46 resuelta el 19/11): estado y puntos en ese momento, si estaba
 * terminada y adonde fue (DestinoAlCerrar; si paso a otro Sprint, cual). Se escribe una vez al cerrar y no se edita.
 * Se relaciona con Sprint (el cerrado y el de destino) y con Tarea.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shr_sprint_cierre_tarea")
public class TareaDeSprintCerrado extends EntidadConBorradoLogico {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sprint")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarea")
    private Tarea tarea;

    // Nombre del estado de la tarea al cerrar el sprint
    private String estadoAlCerrar;

    // Si el estado al cerrar era terminado
    private boolean terminada;

    // Puntos de la tarea al cerrar; null si no estaba estimada
    private Integer puntos;

    @Enumerated(EnumType.STRING)
    private DestinoAlCerrar destino;

    // Sprint al que paso la tarea; null si se quedo (terminada) o volvio al backlog
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sprint_destino")
    private Sprint sprintDestino;

    // Operacion de archivo que la archivo; null si esta vigente
    @Setter(AccessLevel.NONE)
    private Long idOperacionArchivo;

    // Foto de la tarea al cerrar el sprint, con su destino (sprintDestino solo si el destino es SPRINT)
    public TareaDeSprintCerrado(Sprint sprint, Tarea tarea, DestinoAlCerrar destino, Sprint sprintDestino) {
        this.sprint = sprint;
        this.tarea = tarea;
        this.estadoAlCerrar = tarea.getEstado().getNombre();
        this.terminada = tarea.getEstado().isTerminado();
        this.puntos = tarea.getPuntos();
        this.destino = destino;
        this.sprintDestino = destino == DestinoAlCerrar.SPRINT ? sprintDestino : null;
    }
}
