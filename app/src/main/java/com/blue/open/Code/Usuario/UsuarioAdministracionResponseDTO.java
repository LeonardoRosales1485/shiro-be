package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Usuario en el listado de administracion (CU-USR-26, T6.5, prototipo "Administracion · usuarios"): nombre, correo,
 * con que ingresa, si esta archivado y sus grupos. Nunca trae la contrasena. Lo arma UsuarioRepository.paraAdministrar
 * y los grupos, UsuarioListadoDeAdministracionUseCase.
 */
@Setter
@Getter
@NoArgsConstructor
public class UsuarioAdministracionResponseDTO extends EntidadBaseDTO {

    @Descripcion("Nombre y apellido")
    private String nombre;

    @Descripcion("Nombre de usuario (en los de Google, su correo)")
    private String nombreDeUsuario;

    @Descripcion("Correo")
    private String email;

    @Descripcion("true si ingresa con cuenta de Shiro (tiene contraseña); false si ingresa con Google o es un usuario importado sin acceso")
    private boolean cuentaDeShiro;

    @Descripcion("true si está archivado: no puede ingresar")
    private boolean archivado;

    @Descripcion("Grupos del usuario, por nombre")
    private List<UsuarioAdministracionGrupoDTO> grupos = new ArrayList<>();

    // Usuario con sus datos, sin grupos (los suma el caso de uso)
    public UsuarioAdministracionResponseDTO(String uuid, String nombre, String nombreDeUsuario, String email, boolean cuentaDeShiro,
            boolean archivado) {
        setUuid(uuid);
        this.nombre = nombre;
        this.nombreDeUsuario = nombreDeUsuario;
        this.email = email;
        this.cuentaDeShiro = cuentaDeShiro;
        this.archivado = archivado;
    }
}
