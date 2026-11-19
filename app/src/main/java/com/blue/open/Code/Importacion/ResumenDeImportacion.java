package com.blue.open.Code.Importacion;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Resumen de una ejecucion del importador (Spec 6, T4.4): cuantas tareas se crearon y actualizaron (y por tipo),
 * cuantos comentarios, sprints y versiones, las fallas, las advertencias y los correos de Jira sin usuario en Shiro.
 * Se guarda como JSON en Importacion.resumen.
 */
@Getter
public class ResumenDeImportacion {

    private int tareasCreadas;

    private int tareasActualizadas;

    // Tareas creadas o actualizadas por codigo de tipo de Shiro
    private final Map<String, Integer> tareasPorTipo = new TreeMap<>();

    // Comentarios nuevos (los ya importados no se vuelven a traer)
    private int comentarios;

    // Sprints creados o actualizados
    private int sprints;

    // Versiones creadas o actualizadas
    private int versiones;

    // Tareas que no se importaron
    private final List<Falla> fallas = new ArrayList<>();

    // Elementos que se importaron con un cambio (otra clave, sin padre, sprint no activo)
    private final List<Falla> advertencias = new ArrayList<>();

    // Correos (o nombres, si Jira oculta el correo) de Jira sin usuario en Shiro, ordenados
    private final TreeSet<String> usuariosSinVincular = new TreeSet<>();

    // Cuenta una tarea creada o actualizada del tipo recibido
    public void contarTarea(boolean creada, String tipo) {
        if (creada)
            tareasCreadas++;
        else
            tareasActualizadas++;
        tareasPorTipo.merge(tipo, 1, Integer::sum);
    }

    // Cuenta un comentario nuevo
    public void contarComentario() {
        comentarios++;
    }

    // Cuenta un sprint creado o actualizado
    public void contarSprint() {
        sprints++;
    }

    // Cuenta una version creada o actualizada
    public void contarVersion() {
        versiones++;
    }

    // Agrega una falla
    public void fallo(String clave, String motivo) {
        fallas.add(new Falla(clave, motivo));
    }

    // Agrega una advertencia
    public void advertir(String clave, String motivo) {
        advertencias.add(new Falla(clave, motivo));
    }

    // Registra un usuario de Jira sin usuario en Shiro
    public void sinVincular(String usuario) {
        if (usuario != null)
            usuariosSinVincular.add(usuario);
    }
}
