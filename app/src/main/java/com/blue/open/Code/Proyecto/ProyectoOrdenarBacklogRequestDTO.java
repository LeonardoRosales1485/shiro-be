package com.blue.open.Code.Tarea;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request del caso de uso ordenar el backlog: el proyecto y las tareas en el orden nuevo.
 */
@Setter
@Getter
@NoArgsConstructor
public class TareaOrdenarBacklogRequestDTO extends EntidadBaseDTO {

    @Descripcion("Uuid del proyecto")
    @NotBlank(message = "Elegí el proyecto")
    private String proyectoUuid;

    @Descripcion("Uuids de las tareas del backlog en el orden nuevo. Las que no vienen quedan después, en su orden anterior")
    @NotNull(message = "Indicá el orden de las tareas")
    private List<String> tareas;
}
