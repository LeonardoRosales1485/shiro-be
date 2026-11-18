package com.blue.open.Code.Tablero;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Proyeccion.Columna;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de lectura de un tablero de proyecto. Se usa dentro de ProyectoDTO.
 */
@Setter
@Getter
@NoArgsConstructor
public class TableroDTO extends EntidadBaseDTO {

    @Descripcion("Uuid del tipo de tablero")
    @Columna("tipoTablero.uuid")
    private String tipoTableroUuid;

    @Descripcion("Código del tipo de tablero: PRINCIPAL, QA, ANALISIS o DISENO")
    @Columna("tipoTablero.codigo")
    private String tipoTableroCodigo;

    @Descripcion("Nombre visible del tablero")
    @Columna("tipoTablero.nombre")
    private String tipoTableroNombre;
}
