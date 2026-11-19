package com.blue.open.Code.Sprint;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request de cerrar un sprint: a donde van las tareas sin terminar.
 */
@Setter
@Getter
@NoArgsConstructor
public class SprintCerrarRequestDTO extends EntidadBaseDTO {

    @Descripcion("Destino de las tareas sin terminar: BACKLOG o SPRINT")
    @NotNull(message = "Elegí a dónde van las tareas sin terminar")
    private DestinoDeCierre destino;

    @Descripcion("Uuid del sprint planificado de destino, si destino es SPRINT")
    private String sprintDestinoUuid;
}
