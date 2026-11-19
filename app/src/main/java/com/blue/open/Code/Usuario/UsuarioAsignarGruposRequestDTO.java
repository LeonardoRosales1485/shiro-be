package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request del caso de uso asignar grupos: la lista completa de grupos del usuario (reemplaza a la anterior).
 */
@Setter
@Getter
@NoArgsConstructor
public class UsuarioAsignarGruposRequestDTO extends EntidadBaseDTO {

    @Descripcion("Uuids de los grupos que va a tener el usuario. Reemplaza la lista anterior; vacía le quita todos")
    @NotNull(message = "Indicá la lista de grupos (puede estar vacía)")
    private List<String> grupos;
}
