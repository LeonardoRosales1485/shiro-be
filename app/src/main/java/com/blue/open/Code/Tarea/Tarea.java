package com.blue.open.Code.Tarea;

import com.blue.open.Code.Estado.Estado;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Code.TipoTarea.TipoTarea;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tarea de un tablero (seccion 4 del DAF y Spec 3). Pertenece a un Proyecto y a un Tablero, que no cambia; tiene un
 * TipoTarea, un Estado del tipo de su tablero, un Usuario responsable (opcional) y un Usuario informante, y sus
 * TareaEtiqueta, y una Tarea padre opcional con sus Tarea hijas. La clave (SHI-123) es unica: se arma con el prefijo del proyecto y el numero que da su contador.
 * Sprint, versiones, busqueda e id de Jira se suman en las tareas siguientes del plan.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_tarea")
public class Tarea extends EntidadConBorradoLogico {

    // Proyecto de la tarea: el de su tablero. Lo asigna TareaService
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    // Tablero de la tarea. No cambia: para llevarla a otro tablero se clona
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tablero")
    private Tablero tablero;

    // Numero de la tarea dentro del proyecto. Solo lo asigna numerar
    @Setter(AccessLevel.NONE)
    private Long numero;

    // Clave unica de la tarea: prefijo del proyecto, guion y numero (SHI-123). Solo la asigna numerar
    @Setter(AccessLevel.NONE)
    private String clave;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_tarea")
    private TipoTarea tipo;

    // Estado actual, del tipo de su tablero. Nace en el estado inicial; lo cambia mover (T1.5)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado")
    private Estado estado;

    private String titulo;

    // Descripcion en Markdown (opcional)
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Usuario asignado (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable")
    private Usuario responsable;

    // Usuario que reporto la tarea: por defecto, quien la crea
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_informante")
    private Usuario informante;

    @Enumerated(EnumType.STRING)
    private PrioridadDeTarea prioridad = PrioridadDeTarea.MEDIA;

    // Estimacion en puntos (opcional)
    private Integer puntos;

    // Fecha de vencimiento (opcional), sin hora
    private LocalDate vencimiento;

    // Tarea padre (opcional): su tipo tiene un nivel menor y es del mismo proyecto. Lo valida TareaService
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_padre")
    private Tarea padre;

    // Tareas hijas (las que tienen a esta como padre)
    @OneToMany(mappedBy = "padre", fetch = FetchType.LAZY)
    private List<Tarea> hijas = new ArrayList<>();

    // Etiquetas de la tarea; las quitadas quedan con status -1
    @OneToMany(mappedBy = "tarea", fetch = FetchType.LAZY)
    private List<TareaEtiqueta> etiquetas = new ArrayList<>();

    // Textos de etiquetas recibidos al guardar. No se persiste: TareaService los sincroniza con shr_tarea_etiqueta
    // despues de guardar la tarea, porque una tarea nueva necesita su id antes
    @Transient
    private List<String> etiquetasPedidas = new ArrayList<>();

    /*
     * Asigna el numero y la clave de una tarea nueva. Recibe el prefijo del proyecto y el numero que dio su contador
     * (Proyecto.siguienteNumero). Invariante: la clave no cambia; si la tarea ya tiene clave lanza ExcepcionDeApi
     * con ERROR_INTERNO, porque es un error del backend.
     */
    public void numerar(String prefijo, long numero) {
        if (this.clave != null)
            throw new ExcepcionDeApi(CodigoDeErrorKernel.ERROR_INTERNO,
                    "Tarea | numerar | la tarea " + this.clave + " ya tiene clave");
        this.numero = numero;
        this.clave = prefijo + "-" + numero;
    }
}
