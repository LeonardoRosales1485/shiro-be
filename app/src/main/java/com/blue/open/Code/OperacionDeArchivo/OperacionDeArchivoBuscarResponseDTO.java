package com.blue.open.Code.OperacionDeArchivo;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Elemento archivado en la busqueda de archivados (CU-ARC-01). uuid es el del elemento: con el tipo, sirve para
 * restaurarlo en su endpoint.
 */
@Setter
@Getter
@NoArgsConstructor
public class OperacionDeArchivoBuscarResponseDTO extends EntidadBaseDTO {

    @Descripcion("Tipo: TAREA, PROYECTO, SPRINT, VERSION o USUARIO")
    private String tipo;

    @Descripcion("Nombre visible: clave y título de la tarea, prefijo y nombre del proyecto, nombre del sprint o de la versión, o nombre de usuario")
    private String nombre;

    @Descripcion("Prefijo del proyecto del elemento. Vacío en los usuarios")
    private String proyectoPrefijo;

    @Descripcion("Cuándo se archivó")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
    private Timestamp fechaDeBorrado;

    @Descripcion("Nombre del usuario que lo archivó. Vacío si no se sabe")
    private String archivadoPorNombre;

    @Descripcion("Cuántas tareas se archivaron junto con el elemento (subtareas o historias de una tarea, tareas de un proyecto). Vacío en los demás tipos")
    private Integer incluidos;

    // Arma el DTO a partir del elemento archivado
    public OperacionDeArchivoBuscarResponseDTO(ElementoArchivado elemento) {
        setUuid(elemento.uuid());
        setStatus(-1);
        this.tipo = elemento.tipo();
        this.nombre = elemento.nombre();
        this.proyectoPrefijo = elemento.proyectoPrefijo();
        this.fechaDeBorrado = elemento.fechaDeBorrado();
        this.archivadoPorNombre = elemento.archivadoPorNombre();
    }
}
