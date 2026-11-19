package com.blue.open.Code.Relacion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request del caso de uso crear relacion: las dos tareas y el tipo, en el sentido de origen a destino.
 */
@Setter
@Getter
@NoArgsConstructor
public class RelacionCrearRequestDTO extends EntidadBaseDTO {

    @Descripcion("Uuid de la tarea origen (la que bloquea, duplica o se relaciona)")
    @NotBlank(message = "Elegí la tarea de origen")
    private String origenUuid;

    @Descripcion("Uuid de la tarea destino (la bloqueada, duplicada o relacionada)")
    @NotBlank(message = "Elegí la tarea de destino")
    private String destinoUuid;

    @Descripcion("Tipo: BLOQUEA, RELACIONA o DUPLICA. CLONA solo la crea clonar")
    @NotNull(message = "Elegí el tipo de relación")
    private TipoDeRelacion tipo;
}
