package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Proyeccion.Columna;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de una importacion: lo responden obtener, paginar, mapear y ejecutar. Nunca lleva el token de Jira.
 */
@Setter
@Getter
@NoArgsConstructor
public class ImportacionDTO extends EntidadBaseDTO {

    @Descripcion("URL del sitio de Jira")
    private String urlJira;

    @Descripcion("Correo de la cuenta de Jira con la que se conecta")
    private String correo;

    @Descripcion("Clave del proyecto de Jira: es el prefijo del proyecto en Shiro")
    private String claveProyectoJira;

    @Descripcion("Nombre del proyecto en Jira")
    private String nombreProyectoJira;

    @Descripcion("Estado: CONECTADA, MAPEADA, EN_CURSO (corre en segundo plano), TERMINADA o FALLIDA")
    private EstadoDeImportacion estado;

    @Descripcion("Mapeo elegido, como JSON: tableros (id de Jira a uuid de tipo de tablero), estados (por tablero de Jira, id de estado de Jira a uuid de estado) y tipos (id de Jira a uuid de tipo de tarea). Null hasta mapear")
    private String mapeo;

    @Descripcion("Resumen de la última ejecución, como JSON: tareasCreadas, tareasActualizadas, tareasPorTipo, comentarios, sprints, versiones, fallas (claveJira y motivo), advertencias y usuariosSinVincular. Null hasta ejecutar")
    private String resumen;

    @Descripcion("Uuid del proyecto de Shiro con lo importado. Null hasta la primera ejecución")
    @Columna("proyecto.uuid")
    private String proyectoUuid;
}
