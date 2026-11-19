package com.blue.open.Code.Proyecto;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request del caso de uso ordenar el backlog de un proyecto (el proyecto viaja en la ruta): las tareas en el orden nuevo.
 */
@Setter
@Getter
@NoArgsConstructor
public class ProyectoOrdenarBacklogRequestDTO extends EntidadBaseDTO {

    @Descripcion("Uuids de las tareas del backlog en el orden nuevo. Las que no vienen quedan después, en su orden anterior")
    @NotNull(message = "Indicá el orden de las tareas")
    private List<String> tareas;
}
