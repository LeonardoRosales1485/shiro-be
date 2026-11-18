package com.blue.open.Code.Usuario;

import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Usuario de Shiro, extiende {@link UsuarioBase} (core-identity) sin agregar campos propios.
 * Shiro usa core-monoempresa: los roles del usuario (los grupos de Shiro) viven en auth_usuarios_roles, sin empresas.
 */
@Setter
@Getter
@Table(name = "auth_usuarios")
@Entity()
public class Usuario extends UsuarioBase {
}
