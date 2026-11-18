package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Body de POST /usuario/publico/verificar: el uuid del usuario pendiente de verificacion (heredado de EntidadBaseDTO)
 * y el codigo que se le envio por email al registrarse.
 */
@Descripcion("Verificación de un usuario pendiente: uuid es el uuid del usuario y codigo el que recibió por email")
@Getter
@Setter
public class UsuarioVerificarCodigoRequestDTO extends EntidadBaseDTO {
    @Descripcion("Código de verificación recibido por email al registrarse")
    @NotBlank(message = "El codigo es obligatorio")
    private String codigo;

    // Uuid del usuario pendiente. Se redeclara el getter solo para exigirlo: el campo es de EntidadBaseDTO
    @Override
    @NotBlank(message = "El uuid del usuario es obligatorio")
    public String getUuid() {
        return super.getUuid();
    }
}
