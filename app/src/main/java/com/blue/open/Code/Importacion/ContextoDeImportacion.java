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
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Estado de una ejecucion del importador (ImportadorDeJira e ImportadorDeTareas): el proyecto de Shiro (con la fila
 * bloqueada), las credenciales, quien importa, el mapeo ya resuelto a entidades, la duracion de los sprints sin fechas, los sprints y versiones importados
 * por id de Jira, los usuarios de Shiro por correo y los importados, y el resumen que se va armando. Vive lo que dura una ejecucion.
 */
@Getter
@RequiredArgsConstructor
public class ContextoDeImportacion {

    private final Proyecto proyecto;

    private final CredencialesDeJira credenciales;

    // Clave del proyecto de Jira (la que se usa en la API de Jira; el prefijo de Shiro puede ser otro)
    private final String claveDeJira;

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

    // Sprint activo del proyecto durante la importacion (solo puede haber uno); null si no hay
    @Setter
    private Sprint sprintActivo;

    // Versiones importadas por id de Jira
    private final Map<String, Version> versiones = new HashMap<>();

    // Duracion en dias de los sprints de Jira sin fechas (la del mapeo)
    private final int duracionDeSprintEnDias;

    // Usuarios de Shiro por correo (los que ya se buscaron)
    private final Map<String, Usuario> usuariosPorCorreo = new HashMap<>();

    // Avance de la ejecucion: lo guarda SeguimientoDeImportacion a lo sumo una vez por segundo
    private final AvanceDeImportacion avance = new AvanceDeImportacion();

    // Importacion que se ejecuta y su seguimiento (avance y cancelacion); el seguimiento es null en las pruebas unitarias
    @Setter
    private Long idImportacion;

    @Setter
    private SeguimientoDeImportacion seguimiento;

    // Momento del ultimo control del avance, en milisegundos
    private long ultimoControl;

    // Usuarios creados sin acceso por id de Jira (en esta importacion o en una anterior)
    private final Map<String, Usuario> usuariosImportados = new HashMap<>();

    // Guarda el avance y mira si se pidio cancelar, a lo sumo una vez por segundo (siempre si forzar). Lanza
    // IMPORTACION_CANCELADA si un administrador la cancelo
    public void controlar(boolean forzar) {
        long ahora = System.currentTimeMillis();
        if (seguimiento == null || (!forzar && ahora - ultimoControl < 1000))
            return;
        ultimoControl = ahora;
        seguimiento.controlar(idImportacion, avance);
    }

    // Id del usuario que importa
    public Long idUsuario() {
        return usuario.getId();
    }
}
