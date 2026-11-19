package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Request de mapear una importacion (CU-IMP-03). Solo se importan las tareas de los tableros mapeados, en estados y
 * tipos mapeados. Reemplaza el mapeo anterior.
 */
@Setter
@Getter
@NoArgsConstructor
public class ImportacionMapeoRequestDTO extends EntidadBaseDTO {

    @Descripcion("Tableros de Jira a importar, cada uno con su tipo de tablero de Shiro y sus estados")
    @NotEmpty(message = "Elegí al menos un tablero de Jira")
    @Valid
    private List<MapeoDeTableroDTO> tableros = new ArrayList<>();

    @Descripcion("Tipos de Jira y su tipo de Shiro. Una tarea de un tipo sin mapear no se importa (queda en las fallas)")
    @NotNull(message = "Faltan los tipos")
    @Valid
    private List<MapeoDeTipoDTO> tipos = new ArrayList<>();
}
