package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta de conectar y de leer el catalogo (CU-IMP-01 y CU-IMP-02): lo que hay en el proyecto de Jira para armar
 * el mapeo.
 */
@Setter
@Getter
@NoArgsConstructor
public class ImportacionCatalogoDTO extends EntidadBaseDTO {

    @Descripcion("Uuid de la importación")
    private String importacionUuid;

    @Descripcion("Nombre del proyecto en Jira")
    private String nombreProyectoJira;

    @Descripcion("Tableros del proyecto en Jira: cada uno se mapea a un tipo de tablero de Shiro (principal o sub-tablero)")
    private List<ElementoDeJiraDTO> tableros = new ArrayList<>();

    @Descripcion("Estados del proyecto en Jira: en cada tablero mapeado, cada uno se mapea a un estado de ese tipo de tablero")
    private List<ElementoDeJiraDTO> estados = new ArrayList<>();

    @Descripcion("Tipos de tarea del proyecto en Jira: cada uno se mapea a un tipo de Shiro")
    private List<ElementoDeJiraDTO> tipos = new ArrayList<>();

    @Descripcion("Usuarios asignables del proyecto en Jira, vinculados por correo con los de Shiro")
    private List<UsuarioDeJiraDTO> usuarios = new ArrayList<>();
}
