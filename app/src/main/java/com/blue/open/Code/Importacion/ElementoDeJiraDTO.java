package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tablero, estado o tipo de Jira en el catalogo de una importacion (ImportacionCatalogoDTO).
 */
@Setter
@Getter
@NoArgsConstructor
public class ElementoDeJiraDTO extends EntidadBaseDTO {

    @Descripcion("Id del elemento en Jira: es el que se manda en el mapeo")
    private String idJira;

    @Descripcion("Nombre del elemento en Jira")
    private String nombre;

    // Elemento con su id y nombre de Jira
    public ElementoDeJiraDTO(String idJira, String nombre) {
        this.idJira = idJira;
        this.nombre = nombre;
    }
}
