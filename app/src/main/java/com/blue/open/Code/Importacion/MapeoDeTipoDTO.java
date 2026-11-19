package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un tipo de tarea de Jira llevado a un tipo de Shiro (ImportacionMapeoRequestDTO).
 */
@Setter
@Getter
@NoArgsConstructor
public class MapeoDeTipoDTO extends EntidadBaseDTO {

    @Descripcion("Id del tipo en Jira (del catálogo)")
    @NotBlank(message = "Falta el id de Jira de un tipo")
    private String idJira;

    @Descripcion("Uuid del tipo de tarea de Shiro. Va este campo o crear, uno solo")
    private String tipoTareaUuid;

    @Descripcion("true para crear un tipo de tarea con el nombre y el nivel del de Jira (se reusa si ya hay uno con ese nombre). Queda en el catálogo de todos los proyectos")
    private boolean crear;
}
