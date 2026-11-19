package com.blue.open.Code.Importacion;

/**
 * Tipo de elemento de Jira que guarda MapeoJira: el proyecto (por su clave), una tarea, un comentario, un sprint, una
 * version o un usuario creado sin acceso (por su id de Jira). Se persiste por nombre en shr_mapeo_jira.tipo_entidad.
 */
public enum TipoDeEntidadJira {
    PROYECTO,
    TAREA,
    COMENTARIO,
    SPRINT,
    VERSION,
    USUARIO
}
