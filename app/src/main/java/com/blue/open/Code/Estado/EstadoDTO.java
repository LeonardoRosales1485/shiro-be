package com.blue.open.Code.Estado;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de lectura de un estado de tablero. Se usa dentro de TipoTableroDTO (catalogo de tableros).
 */
@Setter
@Getter
@NoArgsConstructor
public class EstadoDTO extends EntidadBaseDTO {

    @Descripcion("Nombre visible del estado (columna del tablero)")
    private String nombre;

    @Descripcion("Posición de la columna en el tablero; 1 es la primera")
    private Integer orden;

    @Descripcion("true en el estado en el que nace una tarea del tablero")
    private boolean inicial;

    @Descripcion("true si una tarea en este estado cuenta como terminada para el changelog y el cierre de sprint")
    private boolean terminado;
}
