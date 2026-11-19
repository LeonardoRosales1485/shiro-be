package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request de conectar con Jira (CU-IMP-01): URL, correo y token de API de Jira, y la clave del proyecto a importar.
 */
@Setter
@Getter
@NoArgsConstructor
public class ImportacionConectarRequestDTO extends EntidadBaseDTO {

    @Descripcion("URL del sitio de Jira (https://empresa.atlassian.net)")
    @NotBlank(message = "Escribí la URL de Jira")
    @Pattern(regexp = "^https?://\\S+$", message = "La URL de Jira tiene que empezar con http:// o https://")
    @Size(max = 255, message = "La URL puede tener hasta 255 caracteres")
    private String url;

    @Descripcion("Correo de la cuenta de Jira")
    @NotBlank(message = "Escribí el correo de la cuenta de Jira")
    @Email(message = "El correo de Jira no es válido")
    @Size(max = 255, message = "El correo puede tener hasta 255 caracteres")
    private String correo;

    @Descripcion("Token de API de Jira de esa cuenta. Se guarda cifrado y nunca se devuelve")
    @NotBlank(message = "Escribí el token de API de Jira")
    @Size(max = 512, message = "El token puede tener hasta 512 caracteres")
    private String token;

    @Descripcion("Clave del proyecto de Jira (JSIM). Va a ser el prefijo del proyecto en Shiro: de 2 a 6 letras")
    @NotBlank(message = "Escribí la clave del proyecto de Jira")
    @Size(max = 20, message = "La clave puede tener hasta 20 caracteres")
    private String claveProyecto;
}
