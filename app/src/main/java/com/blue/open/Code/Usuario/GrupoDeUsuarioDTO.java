package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Grupo (rol) asignado a un usuario: lo responden los casos de uso de grupos de un usuario. uuid es el del grupo.
 */
@Setter
@Getter
@NoArgsConstructor
public class GrupoDeUsuarioDTO extends EntidadBaseDTO {

    @Descripcion("Nombre del grupo")
    private String nombre;

    // Constructor de la proyeccion (UsuarioRepository.gruposDe)
    public GrupoDeUsuarioDTO(String uuid, String nombre) {
        setUuid(uuid);
        this.nombre = nombre;
    }
}
