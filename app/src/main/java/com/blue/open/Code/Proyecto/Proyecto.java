package com.blue.open.Code.Proyecto;

import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Proyecto de Shiro: unidad de trabajo con su tablero principal, sus sub-tableros, sprints y versiones (glosario del
 * DAF). Se relaciona con sus Tablero (uno por tipo, creados con el proyecto). El prefijo forma las claves de las
 * tareas (SHI-123): es unico entre todos los proyectos, tambien los archivados, y no se cambia.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_proyecto")
public class Proyecto extends EntidadConBorradoLogico {

    // Nombre visible del proyecto
    private String nombre;

    // Prefijo de las claves de tarea, en mayusculas, de 2 a 6 letras. Lo valida ProyectoService
    private String prefijo;

    // Descripcion libre del proyecto (opcional)
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Ultimo numero de clave asignado a una tarea del proyecto. Solo lo avanza el alta de tareas (T1.3), con bloqueo
    // de fila; puede tener saltos (adenda 2 del DAF)
    @Setter(AccessLevel.NONE)
    private Long ultimoNumero = 0L;

    // Valores del proyecto antes de editarlo, para el historial. No se persiste: lo completa ProyectoService
    @Transient
    private Map<String, String> valoresAnteriores = new LinkedHashMap<>();

    // Tableros del proyecto: uno por tipo de tablero
    @OneToMany(mappedBy = "proyecto", fetch = FetchType.LAZY)
    private List<Tablero> tableros = new ArrayList<>();

    /*
     * Avanza el contador de claves y devuelve el numero para la tarea nueva. Solo se llama con la fila del proyecto
     * bloqueada (ProyectoRepository.bloquearPorId): asi dos altas simultaneas nunca reciben el mismo numero.
     */
    public long siguienteNumero() {
        this.ultimoNumero = this.ultimoNumero + 1;
        return this.ultimoNumero;
    }
}
