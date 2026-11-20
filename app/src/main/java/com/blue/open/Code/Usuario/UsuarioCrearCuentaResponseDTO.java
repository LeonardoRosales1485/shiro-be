package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta de crear cuenta de Shiro (CU-USR-27, propuesta 56): la cuenta creada y su contrasena inicial, que solo
 * viaja en esta respuesta (en la base queda el hash). El campo se llama contrasena para que SanitizadorDeLogs la tape.
 */
@Setter
@Getter
@NoArgsConstructor
public class UsuarioCrearCuentaResponseDTO extends EntidadBaseDTO {

    @Descripcion("Nombre y apellido")
    private String nombre;

    @Descripcion("Nombre de usuario con el que ingresa")
    private String nombreDeUsuario;

    @Descripcion("Correo")
    private String email;

    @Descripcion("Contraseña inicial generada al azar. Se muestra una sola vez: compartila por un canal seguro")
    private String contrasena;
}
