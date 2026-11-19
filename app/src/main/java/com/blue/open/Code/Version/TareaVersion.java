package com.blue.open.Code.Version;

import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Asignacion de una Tarea a una Version (Spec 1): una tarea puede estar en varias versiones. Quitarla la deja con
 * status -1 (nada se borra).
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_tarea_version")
public class TareaVersion extends EntidadConBorradoLogico {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarea")
    private Tarea tarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_version")
    private Version version;

    // Asigna la tarea a la version
    public TareaVersion(Tarea tarea, Version version) {
        this.tarea = tarea;
        this.version = version;
    }
}
