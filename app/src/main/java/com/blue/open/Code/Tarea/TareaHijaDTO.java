package com.blue.open.Code.Tarea;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Proyeccion.Columna;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resumen de una tarea hija dentro de TareaDTO. Solo lectura.
 */
@Setter
@Getter
@NoArgsConstructor
public class TareaHijaDTO extends EntidadBaseDTO {

    @Descripcion("Clave de la tarea hija (SHI-123)")
    private String clave;

    @Descripcion("Título de la tarea hija")
    private String titulo;

    @Descripcion("Código del tipo de la tarea hija")
    @Columna("tipo.codigo")
    private String tipoCodigo;

    @Descripcion("Nombre del estado actual de la tarea hija")
    @Columna("estado.nombre")
    private String estadoNombre;
}
