package com.blue.open.Code.Usuario;

import com.blue.open.Core.Identity.DTO.UsuarioBaseDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de usuario de Shiro. Extiende el DTO base de core-identity: con core-monoempresa los roles del usuario no
 * viajan en este DTO (la asignacion de grupos se resuelve en F2, T2.5).
 */
@Getter
@Setter
public class UsuarioDTO extends UsuarioBaseDTO {
}
