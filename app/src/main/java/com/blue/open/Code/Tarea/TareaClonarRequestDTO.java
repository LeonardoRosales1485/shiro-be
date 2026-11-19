package com.blue.open.Code.Tarea;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request del caso de uso clonar tarea: el tablero de destino y que datos copiar.
 */
@Setter
@Getter
@NoArgsConstructor
public class TareaClonarRequestDTO extends EntidadBaseDTO {

    @Descripcion("Uuid del tablero de destino: cualquier tablero del mismo proyecto, principal o sub-tablero")
    @NotBlank(message = "Elegí el tablero al que se clona la tarea")
    private String tableroUuid;

    @Descripcion("Datos a copiar además del título y el tipo: DESCRIPCION, PRIORIDAD, ETIQUETAS, PUNTOS, RESPONSABLE, VERSIONES, COMENTARIOS y SUBTAREAS. Si no viene, se copian descripción, prioridad y etiquetas; una lista vacía copia solo título y tipo")
    private List<DatoAClonar> copiar;
}
