package com.blue.open.Code.Tarea;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request del caso de uso mover tarea: el estado de destino, del mismo tablero que la tarea.
 */
@Setter
@Getter
@NoArgsConstructor
public class TareaMoverRequestDTO extends EntidadBaseDTO {

    @Descripcion("Uuid del estado de destino. Tiene que ser un estado del tablero de la tarea; puede ser cualquiera")
    @NotBlank(message = "Elegí el estado al que se mueve la tarea")
    private String estadoUuid;
}
