package com.blue.open.Code.Usuario;

import com.blue.open.Core.Identity.Service.UsuarioBaseService;
import org.springframework.stereotype.Service;

/**
 * Service de usuarios de Shiro sobre UsuarioBaseService (core-identity), con los hooks por defecto del ABM.
 * Dependencias externas: la base de datos.
 * Recibe: UsuarioDTO o uuid segun el endpoint. Responde: UsuarioDTO.
 */
@Service
public class UsuarioService extends UsuarioBaseService<Usuario, UsuarioDTO> {
    // Sin overrides: Shiro no tiene registro publico ni recuperacion de contrasena por mail
}
