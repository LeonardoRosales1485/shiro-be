package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta de los grupos de la sesion (CU-USR-24): los grupos del usuario actual y si es administrador. La usa el
 * front para mostrar u ocultar la administracion.
 */
@Setter
@Getter
@NoArgsConstructor
public class UsuarioSesionGruposResponseDTO extends EntidadBaseDTO {

    @Descripcion("Grupos vigentes del usuario de la sesión, por nombre")
    private List<GrupoDeUsuarioDTO> grupos = new ArrayList<>();

    @Descripcion("true si es super admin o Administrador (grupos SYSADMIN o ADMIN): ve la administración")
    private boolean esAdministrador;

    // Respuesta con los grupos y si es administrador
    public UsuarioSesionGruposResponseDTO(List<GrupoDeUsuarioDTO> grupos, boolean esAdministrador) {
        this.grupos = grupos;
        this.esAdministrador = esAdministrador;
    }
}
