package com.blue.open.Code.Usuario;

import com.blue.open.Core.Identity.DTO.UsuarioBaseDTO;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de usuario de Shiro. Extiende el DTO base de core-identity: con core-monoempresa los roles del usuario no
 * viajan en este DTO (los grupos se asignan con POST /usuario/{uuid}/grupos). Suma el nombre y apellido, que la
 * administracion pide al crear una cuenta de Shiro (T6.5).
 */
@Getter
@Setter
public class UsuarioDTO extends UsuarioBaseDTO {

    @Descripcion("Nombre y apellido de la persona. Los de Google lo toman de su cuenta al ingresar")
    @Size(max = 255, message = "El nombre puede tener hasta 255 caracteres")
    private String nombre;
}
