package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Proyecto de Jira visible con unas credenciales (CU-IMP-08, propuesta 53): su clave y su nombre, para elegir cual
 * importar.
 */
@Setter
@Getter
@NoArgsConstructor
public class ImportacionProyectosDeJiraResponseDTO extends EntidadBaseDTO {

    @Descripcion("Clave del proyecto en Jira (PTR): es la que se manda al conectar")
    private String clave;

    @Descripcion("Nombre del proyecto en Jira")
    private String nombre;

    // Proyecto con su clave y su nombre
    public ImportacionProyectosDeJiraResponseDTO(String clave, String nombre) {
        this.clave = clave;
        this.nombre = nombre;
    }
}
