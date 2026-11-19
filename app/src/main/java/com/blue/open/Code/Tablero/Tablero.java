package com.blue.open.Code.Tablero;

import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.TipoTablero.TipoTablero;
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
 * Tablero de un proyecto: el principal o un sub-tablero (QA, Analisis, Diseno). Se crea con el proyecto, uno por
 * TipoTablero, y sus estados son los de su tipo. Pertenece a un Proyecto; las tareas viven en un tablero y no cambian
 * de tablero (se clonan).
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_tablero")
public class Tablero extends EntidadConBorradoLogico {

    // Proyecto al que pertenece el tablero
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto", nullable = false)
    private Proyecto proyecto;

    // Tipo del tablero: define su nombre y sus estados
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_tablero", nullable = false)
    private TipoTablero tipoTablero;

    // Tablero nuevo de un proyecto, del tipo recibido
    // Operacion de archivo que archivo el tablero (al archivar su proyecto); null si esta vigente
    private Long idOperacionArchivo;

    public Tablero(Proyecto proyecto, TipoTablero tipoTablero) {
        this.proyecto = proyecto;
        this.tipoTablero = tipoTablero;
    }
}
