package com.blue.open.Code.Usuario;

import com.blue.open.Core.Multiempresa.Entidad.UsuarioBaseMultiempresa;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Usuario del proyecto, extiende {@link UsuarioBaseMultiempresa} (core-multiempresa) sin agregar campos propios.
 * Sus empresas y roles viven en auth_usuarios_empresas: la coleccion de solo lectura rolesPorEmpresa que hereda es la
 * que lee la Proyeccion de UsuarioDTO.
 */
@Setter
@Getter
@Table(name = "auth_usuarios")
@Entity()
public class Usuario extends UsuarioBaseMultiempresa {
}
