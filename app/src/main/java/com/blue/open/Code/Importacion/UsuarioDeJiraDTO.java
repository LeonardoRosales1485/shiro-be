package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuario de Jira en el catalogo de una importacion, con el usuario de Shiro que tiene el mismo correo (si existe).
 */
@Setter
@Getter
@NoArgsConstructor
public class UsuarioDeJiraDTO extends EntidadBaseDTO {

    @Descripcion("Correo del usuario en Jira. Null si Jira no lo muestra (privacidad del perfil)")
    private String correo;

    @Descripcion("Nombre visible en Jira")
    private String nombre;

    @Descripcion("Uuid del usuario de Shiro con el mismo correo. Null si no existe: sus tareas quedan sin responsable")
    private String usuarioUuid;

    // Usuario de Jira con su vinculo en Shiro (o null)
    public UsuarioDeJiraDTO(String correo, String nombre, String usuarioUuid) {
        this.correo = correo;
        this.nombre = nombre;
        this.usuarioUuid = usuarioUuid;
    }
}
