package com.blue.open.Code.Estado;

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
 * Estado de las tareas de un tipo de tablero (una columna del tablero). Catalogo sembrado por Liquibase con los estados
 * de la seccion 4 del DAF. Pertenece a un TipoTablero; una tarea solo puede estar en un estado del tipo de su tablero.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_estado")
public class Estado extends EntidadConBorradoLogico {

    // Tipo de tablero al que pertenece el estado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_tablero", nullable = false)
    private TipoTablero tipoTablero;

    // Nombre visible del estado (columna del tablero)
    private String nombre;

    // Posicion de la columna en el tablero (1 = primera)
    private Integer orden;

    // true en el estado en el que nace una tarea del tablero (uno por tablero)
    private boolean inicial;

    // true si una tarea en este estado cuenta como terminada (changelog y cierre de sprint, adenda 3 del DAF)
    private boolean terminado;
}
