package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Grupo de un usuario en el listado de administracion (CU-USR-26, T6.5): el grupo (uuid y nombre) y desde cuando el
 * usuario esta en el. uuid es el del grupo.
 */
@Setter
@Getter
@NoArgsConstructor
public class UsuarioAdministracionGrupoDTO extends EntidadBaseDTO {

    @Descripcion("Nombre del grupo. SYSADMIN es el super admin y ADMIN el grupo Administradores")
    private String nombre;

    @Descripcion("Desde cuándo el usuario está en el grupo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
    private Timestamp desde;

    // Grupo con su nombre y la fecha de la asignacion
    public UsuarioAdministracionGrupoDTO(String uuid, String nombre, Timestamp desde) {
        setUuid(uuid);
        this.nombre = nombre;
        this.desde = desde;
    }
}
