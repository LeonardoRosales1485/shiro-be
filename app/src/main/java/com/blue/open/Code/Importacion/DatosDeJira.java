package com.blue.open.Code.Importacion;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Lo que el importador lee de la API de Jira (ClienteDeJira), ya separado de su JSON. Records sin logica.
 */
public final class DatosDeJira {

    private DatosDeJira() {
    }

    /**
     * Proyecto de Jira.
     *
     * @param id     id de Jira
     * @param clave  clave (JSIM)
     * @param nombre nombre visible
     */
    public record Proyecto(String id, String clave, String nombre) {
    }

    /**
     * Usuario de Jira (asignable, responsable o autor).
     *
     * @param id     accountId de Jira (null si no viene)
     * @param correo correo (null si Jira lo oculta)
     * @param nombre nombre visible
     */
    public record Usuario(String id, String correo, String nombre) {

        // Identificador estable para registrar el usuario importado: el accountId, o el correo, o el nombre
        public String identificacion() {
            if (id != null)
                return id;
            return correo != null ? correo : nombre;
        }
    }

    /**
     * Incidencia (tarea) de Jira.
     *
     * @param id          id de Jira
     * @param clave       clave (JSIM-12)
     * @param titulo      summary
     * @param descripcion descripcion ya pasada a texto
     * @param tipoId      id del tipo
     * @param estadoId    id del estado
     * @param prioridad   nombre de la prioridad (Highest, High, Medium, Low, Lowest) o null
     * @param padreClave  clave del padre o null
     * @param responsable responsable o null
     * @param etiquetas   labels
     * @param sprintId    id del sprint actual o null
     * @param versionIds  ids de las fix versions
     */
    public record Incidencia(String id, String clave, String titulo, String descripcion, String tipoId, String estadoId,
            String prioridad, String padreClave, Usuario responsable, List<String> etiquetas, String sprintId,
            List<String> versionIds) {
    }

    /**
     * Sprint de un tablero.
     *
     * @param id     id de Jira
     * @param nombre nombre
     * @param estado future, active o closed
     * @param inicio fecha de inicio o null
     * @param fin    fecha de fin o null
     */
    public record Sprint(String id, String nombre, String estado, LocalDate inicio, LocalDate fin) {
    }

    /**
     * Version del proyecto.
     *
     * @param id        id de Jira
     * @param nombre    nombre
     * @param publicada released
     * @param fecha     releaseDate o null
     */
    public record Version(String id, String nombre, boolean publicada, LocalDate fecha) {
    }

    /**
     * Comentario de un incidencia.
     *
     * @param id     id de Jira
     * @param autor  autor
     * @param fecha  fecha de creacion
     * @param cuerpo cuerpo ya pasado a texto
     */
    public record Comentario(String id, Usuario autor, OffsetDateTime fecha, String cuerpo) {
    }
}
