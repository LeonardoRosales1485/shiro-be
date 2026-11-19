package com.blue.open.Code.Importacion;

import com.blue.open.Code.Estado.Estado;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Sprint.Sprint;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Code.TipoTarea.TipoTarea;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Version.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Estado de una ejecucion del importador (ImportadorDeJira e ImportadorDeTareas): el proyecto de Shiro (con la fila
 * bloqueada), las credenciales, quien importa, el mapeo ya resuelto a entidades, los sprints y versiones importados
 * por id de Jira, los usuarios de Shiro por correo y el resumen que se va armando. Vive lo que dura una ejecucion.
 */
@Getter
@RequiredArgsConstructor
public class ContextoDeImportacion {

    private final Proyecto proyecto;

    private final CredencialesDeJira credenciales;

    // Usuario que ejecuta la importacion: informante de las tareas nuevas y autor del historial
    private final Usuario usuario;

    private final ResumenDeImportacion resumen;

    // Tablero del proyecto por id de tablero de Jira, en el orden del mapeo
    private final Map<String, Tablero> tableros = new LinkedHashMap<>();

    // Por id de tablero de Jira, estado de Shiro por id de estado de Jira
    private final Map<String, Map<String, Estado>> estados = new HashMap<>();

    // Tipo de Shiro por id de tipo de Jira
    private final Map<String, TipoTarea> tipos = new HashMap<>();

    // Sprints importados por id de Jira
    private final Map<String, Sprint> sprints = new HashMap<>();

    // Versiones importadas por id de Jira
    private final Map<String, Version> versiones = new HashMap<>();

    // Usuarios de Shiro por correo (los que ya se buscaron)
    private final Map<String, Usuario> usuariosPorCorreo = new HashMap<>();

    // Id del usuario que importa
    public Long idUsuario() {
        return usuario.getId();
    }

    /*
     * Usuario de Shiro del usuario de Jira recibido, por correo (ya buscado con usuariosPorCorreo). Si no hay, lo
     * anota en el resumen como sin vincular y responde null.
     */
    public Usuario vincular(DatosDeJira.Usuario deJira) {
        if (deJira == null)
            return null;
        if (deJira.correo() != null && usuariosPorCorreo.containsKey(deJira.correo()))
            return usuariosPorCorreo.get(deJira.correo());
        resumen.sinVincular(deJira.identificacion());
        return null;
    }
}
