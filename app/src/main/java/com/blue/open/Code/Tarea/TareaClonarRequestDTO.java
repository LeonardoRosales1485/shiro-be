package com.blue.open.Code.Tarea;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request del caso de uso clonar tarea: el tablero de destino, que datos copiar y, si es una Subtarea, si el clon
 * queda como tarea propia (tipoCodigo) o como subtarea de otra tarea (padreUuid).
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

    @Descripcion("Solo al clonar una Subtarea, si el clon queda como tarea propia: su tipo, HISTORIA, TAREA o BUG (nivel 2), sin padre. Va este campo o padreUuid, uno solo. En otros tipos se ignora")
    private String tipoCodigo;

    @Descripcion("Solo al clonar una Subtarea, si el clon queda como subtarea de otra tarea existente: el uuid de esa tarea, del mismo proyecto y de nivel menor. Va este campo o tipoCodigo, uno solo. En otros tipos se ignora")
    private String padreUuid;
}
