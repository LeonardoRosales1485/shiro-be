package com.blue.open.Code.Version;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request de asignar o quitar tareas de una version: los uuids de las tareas.
 */
@Setter
@Getter
@NoArgsConstructor
public class VersionTareasRequestDTO extends EntidadBaseDTO {

    @Descripcion("Uuids de las tareas vigentes del proyecto de la versión")
    @NotNull(message = "Indicá las tareas")
    private List<String> tareas;
}
