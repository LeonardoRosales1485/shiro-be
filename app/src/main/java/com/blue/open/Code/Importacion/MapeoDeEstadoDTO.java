package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un estado de Jira llevado a un estado de Shiro, dentro del mapeo de un tablero (MapeoDeTableroDTO).
 */
@Setter
@Getter
@NoArgsConstructor
public class MapeoDeEstadoDTO extends EntidadBaseDTO {

    @Descripcion("Id del estado en Jira")
    @NotBlank(message = "Falta el id de Jira de un estado")
    private String idJira;

    @Descripcion("Uuid del estado de Shiro. Tiene que ser del tipo de tablero elegido para ese tablero")
    @NotBlank(message = "Elegí el estado de Shiro de cada estado de Jira")
    private String estadoUuid;
}
