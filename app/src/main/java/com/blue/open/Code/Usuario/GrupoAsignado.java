package com.blue.open.Code.Usuario;

import java.sql.Timestamp;

/**
 * Asignacion vigente de un grupo a un usuario, leida en una sola consulta para el listado de administracion
 * (UsuarioRepository.gruposAsignados): el uuid del usuario, el del grupo, su nombre y desde cuando.
 */
public record GrupoAsignado(String usuarioUuid, String grupoUuid, String nombre, Timestamp desde) {
}
