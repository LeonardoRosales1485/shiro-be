package com.blue.open.Code.Relacion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Proyeccion.Columna;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta de crear y archivar una relacion: el sentido guardado, de origen a destino.
 */
@Setter
@Getter
@NoArgsConstructor
public class RelacionDTO extends EntidadBaseDTO {

    @Descripcion("Tipo guardado: BLOQUEA, RELACIONA, DUPLICA o CLONA (de origen a destino)")
    private TipoDeRelacion tipo;

    @Descripcion("Uuid de la tarea origen")
    @Columna("origen.uuid")
    private String origenUuid;

    @Descripcion("Clave de la tarea origen")
    @Columna("origen.clave")
    private String origenClave;

    @Descripcion("Uuid de la tarea destino")
    @Columna("destino.uuid")
    private String destinoUuid;

    @Descripcion("Clave de la tarea destino")
    @Columna("destino.clave")
    private String destinoClave;
}
