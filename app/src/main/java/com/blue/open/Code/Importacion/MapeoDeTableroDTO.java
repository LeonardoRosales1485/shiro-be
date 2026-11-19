package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Un tablero de Jira llevado a un tipo de tablero de Shiro, con sus estados (ImportacionMapeoRequestDTO).
 */
@Setter
@Getter
@NoArgsConstructor
public class MapeoDeTableroDTO extends EntidadBaseDTO {

    @Descripcion("Id del tablero en Jira (del catálogo)")
    @NotBlank(message = "Falta el id de Jira de un tablero")
    private String idJira;

    @Descripcion("Uuid del tipo de tablero de Shiro: el principal o un sub-tablero. Varios tableros de Jira pueden ir al mismo")
    @NotBlank(message = "Elegí el tablero de Shiro de cada tablero de Jira")
    private String tipoTableroUuid;

    @Descripcion("Estados de Jira de ese tablero y su estado de Shiro. Una tarea en un estado sin mapear no se importa (queda en las fallas)")
    @NotNull(message = "Faltan los estados del tablero")
    @Valid
    private List<MapeoDeEstadoDTO> estados = new ArrayList<>();
}
