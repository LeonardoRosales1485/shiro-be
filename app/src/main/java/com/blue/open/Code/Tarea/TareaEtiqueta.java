package com.blue.open.Code.Tarea;

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
 * Etiqueta de texto libre de una Tarea. Nada se borra: una etiqueta quitada queda con status -1 y la Proyeccion de
 * la tarea solo trae las vigentes.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_tarea_etiqueta")
public class TareaEtiqueta extends EntidadConBorradoLogico {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarea")
    private Tarea tarea;

    // Texto de la etiqueta, sin espacios al principio ni al final
    private String texto;

    // Crea una etiqueta vigente de la tarea con el texto recibido
    public TareaEtiqueta(Tarea tarea, String texto) {
        this.tarea = tarea;
        this.texto = texto;
    }
}
