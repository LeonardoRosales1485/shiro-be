package com.blue.open.Code.Importacion;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Avance de una ejecucion del importador (propuesta 55): cuando empezo y cuantas versiones, sprints, tareas y
 * comentarios lleva de cuantos hay. Los comentarios se conocen recien al leerlos de Jira (una lectura por tarea):
 * mientras tanto comentariosTotales es null y tareasConComentariosLeidos dice cuantas tareas se leyeron. Se guarda como
 * JSON en Importacion.avance (lo escribe SeguimientoDeImportacion).
 */
@Getter
@Setter
public class AvanceDeImportacion {

    // Fecha y hora en que empezo la ejecucion
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
    private OffsetDateTime inicio = OffsetDateTime.now();

    private int versiones;

    private Integer versionesTotales;

    private int sprints;

    private Integer sprintsTotales;

    private int tareas;

    private Integer tareasTotales;

    // Tareas de las que ya se leyeron los comentarios de Jira
    private int tareasConComentariosLeidos;

    private int comentarios;

    private Integer comentariosTotales;

    // Suma una version importada
    public void contarVersion() {
        versiones++;
    }

    // Suma un sprint importado
    public void contarSprint() {
        sprints++;
    }

    // Suma una tarea guardada (creada o actualizada)
    public void contarTarea() {
        tareas++;
    }

    // Suma una tarea de la que ya se leyeron los comentarios de Jira
    public void contarTareaConComentariosLeidos() {
        tareasConComentariosLeidos++;
    }

    // Suma un comentario procesado (nuevo o ya importado antes)
    public void contarComentario() {
        comentarios++;
    }
}
