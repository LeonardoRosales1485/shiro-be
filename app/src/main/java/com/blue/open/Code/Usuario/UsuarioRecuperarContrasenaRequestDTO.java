package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Body de POST /usuario/publico/recuperar-contrasena: el email al que se envia el codigo de recuperacion.
 */
@Getter
@Setter
public class UsuarioRecuperarContrasenaRequestDTO extends EntidadBaseDTO {
    @Descripcion("Email del usuario que pide recuperar la contraseña. Si no tiene cuenta activa, la respuesta es la misma "
            + "pero no se envía ningún mail")
    @NotBlank(message = "El email es obligatorio")
    private String email;
}
