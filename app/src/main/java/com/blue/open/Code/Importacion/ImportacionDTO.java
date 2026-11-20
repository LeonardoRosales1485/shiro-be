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

    @Descripcion("Clave del proyecto de Jira")
    private String claveProyectoJira;

    @Descripcion("Prefijo del proyecto en Shiro, elegido al conectar. Las tareas importadas quedan con clave J-PREFIJO-número de Jira (J-SIM-7) y las nuevas siguen PREFIJO-n")
    private String prefijo;

    @Descripcion("Nombre del proyecto en Jira")
    private String nombreProyectoJira;

    @Descripcion("Estado: CONECTADA, MAPEADA, EN_CURSO (corre en segundo plano), TERMINADA, FALLIDA o CANCELADA")
    private EstadoDeImportacion estado;

    @Descripcion("Mapeo elegido, como JSON: tableros (id de Jira a uuid de tipo de tablero), estados (por tablero de Jira, id de estado de Jira a uuid de estado), tipos (id de Jira a uuid de tipo de tarea) y duracionDeSprintEnDias. Null hasta mapear")
    private String mapeo;

    @Descripcion("Resumen de la última ejecución, como JSON: tareasCreadas, tareasActualizadas, tareasPorTipo, comentarios, sprints, versiones, fallas (claveJira y motivo), advertencias y usuariosImportados (usuarios creados sin acceso). Null hasta ejecutar")
    private String resumen;

    @Descripcion("Avance de la ejecución en curso o de la última, como JSON: inicio (fecha y hora en que empezó), tareas y tareasTotales, comentarios y comentariosTotales (null mientras se leen de Jira), tareasConComentariosLeidos, sprints y sprintsTotales, versiones y versionesTotales. Null hasta ejecutar")
    private String avance;

    @Descripcion("true si se pidió cancelar la ejecución en curso; se corta en el próximo control y la importación queda CANCELADA")
    private boolean cancelacionPedida;

    @Descripcion("Uuid del proyecto de Shiro con lo importado. Null hasta la primera ejecución")
    @Columna("proyecto.uuid")
    private String proyectoUuid;
}
