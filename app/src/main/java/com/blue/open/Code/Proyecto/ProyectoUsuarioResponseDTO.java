package com.blue.open.Code.Proyecto;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuario con acceso a un proyecto (CU-PRY-06): lo que necesita el front para elegir el responsable de una tarea.
 * Lo arma ProyectoUsuariosUseCase.
 */
@Setter
@Getter
@NoArgsConstructor
public class ProyectoUsuarioResponseDTO extends EntidadBaseDTO {

    @Descripcion("Nombre de usuario (para ingresar)")
    private String nombreDeUsuario;

    @Descripcion("Nombre visible del usuario. Vacío si no lo cargó")
    private String nombre;

    // Usuario con su uuid, nombre de usuario y nombre visible
    public ProyectoUsuarioResponseDTO(String uuid, String nombreDeUsuario, String nombre) {
        setUuid(uuid);
        this.nombreDeUsuario = nombreDeUsuario;
        this.nombre = nombre;
    }
}
