package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request del caso de uso ingresar con Google: el ID token que el front obtuvo con Google Identity Services.
 */
@Setter
@Getter
@NoArgsConstructor
public class UsuarioIngresarConGoogleRequestDTO extends EntidadBaseDTO {

    @Descripcion("ID token (JWT) que devuelve Google Identity Services al elegir la cuenta")
    @NotBlank(message = "Falta el token de Google")
    private String idToken;
}
