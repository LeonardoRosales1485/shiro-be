package com.blue.open.Code.TipoTarea;

import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tipo de tarea: Epica, Historia, Tarea, Bug o Subtarea (seccion 4 del DAF). Catalogo sembrado por Liquibase para poder
 * sumar tipos propios despues de la v1. El nivel define la jerarquia: el padre de una tarea tiene nivel menor.
 * No se relaciona con otras entidades del catalogo.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_tipo_tarea")
public class TipoTarea extends EntidadConBorradoLogico {

    // Codigo estable del tipo: EPICA, HISTORIA, TAREA, BUG o SUBTAREA
    private String codigo;

    // Nombre visible del tipo
    private String nombre;

    // Nivel en la jerarquia: 1 Epica; 2 Historia, Tarea y Bug; 3 Subtarea
    private Integer nivel;

    // Posicion del tipo en los selectores
    private Integer orden;
}
