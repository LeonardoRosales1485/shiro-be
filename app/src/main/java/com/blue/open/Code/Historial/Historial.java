package com.blue.open.Code.Historial;

import com.blue.open.Code.Common.Canal.CanalDeCambio;
import com.blue.open.Core.Kernel.Entidad.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Types;

/**
 * Fila del historial de cambios (Spec 1 y T1.6): que entidad cambio, que campo, de que valor a cual, quien
 * (idUsuarioDeCreacion), cuando (fechaDeCreacion), por que canal y con que accion. Solo se inserta: el usuario de
 * la aplicacion no tiene UPDATE ni DELETE sobre shr_historial. Por eso hereda de EntidadBase, sin status. No tiene
 * relaciones JPA: la entidad se identifica por su nombre y su uuid, para que sirva a proyectos, tareas y lo que
 * venga despues.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_historial")
public class Historial extends EntidadBase {

    // Nombres de entidad del historial
    public static final String TAREA = "TAREA";

    public static final String PROYECTO = "PROYECTO";

    public static final String USUARIO = "USUARIO";

    public static final String REGLA_PERMISO = "REGLA_PERMISO";

    public static final String SPRINT = "SPRINT";

    // Entidad que cambio: TAREA, PROYECTO, ...
    private String entidad;

    // Uuid del registro que cambio
    @Column(length = 36)
    @JdbcTypeCode(Types.CHAR)
    private String uuidEntidad;

    @Enumerated(EnumType.STRING)
    private AccionDeHistorial accion;

    // Campo que cambio; null en las acciones que no son de un campo (CREAR, ARCHIVAR, ...)
    private String campo;

    // Valor anterior, como texto; null si no tenia
    @Column(columnDefinition = "TEXT")
    private String valorAnterior;

    // Valor nuevo, como texto; null si quedo vacio
    @Column(columnDefinition = "TEXT")
    private String valorNuevo;

    @Enumerated(EnumType.STRING)
    private CanalDeCambio canal;

    // Operacion de archivo que produjo la fila (T1.8); null en el resto
    private Long idOperacion;
}
