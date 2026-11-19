package com.blue.open.Code.Sprint;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request de asignar o quitar tareas de un sprint: los uuids de las tareas.
 */
@Setter
@Getter
@NoArgsConstructor
public class SprintTareasRequestDTO extends EntidadBaseDTO {

    @Descripcion("Uuids de las tareas (historias, tareas o bugs vigentes del proyecto del sprint)")
    @NotNull(message = "Indicá las tareas")
    private List<String> tareas;
}
