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

    @Descripcion("Solo en los tipos: nivel que tendría en Shiro si se crea (1 épica, 2 historia, tarea o bug, 3 subtarea). Null en tableros y estados")
    private Integer nivel;

    @Descripcion("Solo en los estados: si Jira lo cuenta como terminado (categoría done). Null en tableros y tipos")
    private Boolean terminado;

    // Elemento con su id y nombre de Jira
    public ElementoDeJiraDTO(String idJira, String nombre) {
        this.idJira = idJira;
        this.nombre = nombre;
    }

    // Tipo de Jira con su nivel en Shiro
    public static ElementoDeJiraDTO tipo(String idJira, String nombre, int nivel) {
        ElementoDeJiraDTO tipo = new ElementoDeJiraDTO(idJira, nombre);
        tipo.setNivel(nivel);
        return tipo;
    }

    // Estado de Jira con si cuenta como terminado
    public static ElementoDeJiraDTO estado(String idJira, String nombre, boolean terminado) {
        ElementoDeJiraDTO estado = new ElementoDeJiraDTO(idJira, nombre);
        estado.setTerminado(terminado);
        return estado;
    }
}
