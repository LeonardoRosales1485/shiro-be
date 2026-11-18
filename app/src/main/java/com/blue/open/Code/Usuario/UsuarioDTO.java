package com.blue.open.Code.Usuario;

import com.blue.open.Core.Multiempresa.DTO.UsuarioBaseMultiempresaDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de usuario del proyecto. Extiende el DTO multi-empresa porque este proyecto usa
 * core-multiempresa: incluye las empresas del usuario y sus roles en cada una.
 */
@Getter
@Setter
public class UsuarioDTO extends UsuarioBaseMultiempresaDTO {
}
