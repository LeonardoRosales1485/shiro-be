package com.blue.open.Code.Relacion;

import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Relacion entre dos Tarea (Spec 1): un sentido guardado, de origen a destino, con su TipoDeRelacion. El inverso se
 * muestra al leer. Si una de las tareas se archiva, la relacion queda.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_relacion")
public class Relacion extends EntidadConBorradoLogico {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_origen")
    private Tarea origen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_destino")
    private Tarea destino;

    @Enumerated(EnumType.STRING)
    private TipoDeRelacion tipo;

    // Operacion de archivo que la archivo; null si esta vigente
    private Long idOperacionArchivo;

    // Crea una relacion vigente del tipo recibido, de origen a destino
    public Relacion(Tarea origen, Tarea destino, TipoDeRelacion tipo) {
        this.origen = origen;
        this.destino = destino;
        this.tipo = tipo;
    }
}
