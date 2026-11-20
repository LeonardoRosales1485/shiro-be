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
 * Request de los proyectos de Jira visibles (CU-IMP-08, propuesta 53): las credenciales, que se usan solo para esa
 * lectura y no se guardan.
 */
@Setter
@Getter
@NoArgsConstructor
public class ImportacionProyectosRequestDTO extends EntidadBaseDTO {

    @Descripcion("URL del sitio de Jira Cloud: solo https://{sitio}.atlassian.net")
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
}
