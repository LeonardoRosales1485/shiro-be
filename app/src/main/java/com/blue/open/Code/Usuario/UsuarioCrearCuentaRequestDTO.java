package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Request del caso de uso crear cuenta de Shiro (CU-USR-27, propuesta 56): los datos de la persona y sus grupos. La
 * contrasena no viaja: la genera el backend.
 */
@Setter
@Getter
@NoArgsConstructor
public class UsuarioCrearCuentaRequestDTO extends EntidadBaseDTO {

    @Descripcion("Nombre y apellido de la persona")
    @NotBlank(message = "Escribí el nombre y apellido")
    @Size(max = 255, message = "El nombre puede tener hasta 255 caracteres")
    private String nombre;

    @Descripcion("Nombre de usuario con el que ingresa. No se repite")
    @NotBlank(message = "Escribí el nombre de usuario")
    @Size(max = 255, message = "El nombre de usuario puede tener hasta 255 caracteres")
    private String nombreDeUsuario;

    @Descripcion("Correo de la persona")
    @NotBlank(message = "Escribí el correo")
    @Email(message = "El correo no es válido")
    @Size(max = 255, message = "El correo puede tener hasta 255 caracteres")
    private String email;

    @Descripcion("Uuids de los grupos de la persona (puede estar vacía). Solo el super admin suma al grupo Administradores")
    @NotNull(message = "Indicá la lista de grupos (puede estar vacía)")
    private List<String> grupos = new ArrayList<>();
}
