package com.blue.open.Code.Tarea;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Proyeccion.Coleccion;
import com.blue.open.Core.Kernel.Proyeccion.Columna;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO del ABM de tareas: lo usan obtener, paginar y guardar. Para crear alcanzan tablero, tipo y titulo (modo
 * rapido); clave, proyecto y estado los decide el servidor y se ignoran si vienen.
 */
@Setter
@Getter
@NoArgsConstructor
public class TareaDTO extends EntidadBaseDTO {

    @Descripcion("Fecha de la última modificación. Al editar es obligatoria: se manda la que se leyó, y si la tarea cambió desde entonces la respuesta es 409")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
    private Timestamp fechaDeActualizacion;

    @Descripcion("Clave de la tarea (SHI-123): prefijo del proyecto y número. Única; puede tener saltos. Solo lectura")
    private String clave;

    @Descripcion("Uuid del proyecto de la tarea: el de su tablero. Solo lectura")
    @Columna("proyecto.uuid")
    private String proyectoUuid;

    @Descripcion("Uuid del tablero de la tarea: el que se está viendo al crearla. No cambia después de crearla")
    @NotBlank(message = "Elegí el tablero de la tarea")
    @Columna("tablero.uuid")
    private String tableroUuid;

    @Descripcion("Código del tipo de tablero: PRINCIPAL, QA, ANALISIS o DISENO. Solo lectura")
    @Columna("tablero.tipoTablero.codigo")
    private String tableroCodigo;

    @Descripcion("Código del tipo de tarea: EPICA, HISTORIA, TAREA, BUG o SUBTAREA")
    @NotBlank(message = "Elegí el tipo de tarea")
    @Columna("tipo.codigo")
    private String tipoCodigo;

    @Descripcion("Nombre visible del tipo de tarea. Solo lectura")
    @Columna("tipo.nombre")
    private String tipoNombre;

    @Descripcion("Uuid del estado actual. Una tarea nueva nace en el estado inicial de su tablero. Solo lectura: se cambia con mover")
    @Columna("estado.uuid")
    private String estadoUuid;

    @Descripcion("Nombre del estado actual. Solo lectura")
    @Columna("estado.nombre")
    private String estadoNombre;

    @Descripcion("Título de la tarea")
    @NotBlank(message = "Escribí el título de la tarea")
    @Size(max = 255, message = "El título puede tener hasta 255 caracteres")
    private String titulo;

    @Descripcion("Descripción en Markdown. Vacía si no tiene")
    @Size(max = 20000, message = "La descripción puede tener hasta 20000 caracteres")
    private String descripcion;

    @Descripcion("Uuid del usuario responsable. Vacío si la tarea no está asignada")
    @Columna("responsable.uuid")
    private String responsableUuid;

    @Descripcion("Nombre del usuario responsable. Solo lectura")
    @Columna("responsable.nombre")
    private String responsableNombre;

    @Descripcion("Uuid del usuario informante. Si no viene, al crear es quien crea la tarea y al editar se conserva el actual")
    @Columna("informante.uuid")
    private String informanteUuid;

    @Descripcion("Nombre del usuario informante. Solo lectura")
    @Columna("informante.nombre")
    private String informanteNombre;

    @Descripcion("Prioridad: MAS_ALTA, ALTA, MEDIA, BAJA o MAS_BAJA. Si no viene es MEDIA")
    private PrioridadDeTarea prioridad;

    @Descripcion("Estimación en puntos. Vacía si no está estimada")
    @Min(value = 0, message = "Los puntos no pueden ser negativos")
    @Max(value = 999, message = "Los puntos pueden ser hasta 999")
    private Integer puntos;

    @Descripcion("Fecha de vencimiento, sin hora (aaaa-mm-dd). Vacía si no vence")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate vencimiento;

    @Descripcion("Uuid de la tarea padre. Vacío si no tiene. El padre es del mismo proyecto y de un tipo de nivel menor (Épica, después Historia, Tarea o Bug, después Subtarea)")
    @Columna("padre.uuid")
    private String padreUuid;

    @Descripcion("Clave de la tarea padre. Solo lectura")
    @Columna("padre.clave")
    private String padreClave;

    @Descripcion("Tareas hijas vigentes, por número. Solo lectura")
    @Coleccion(value = "hijas", orden = "numero")
    private List<TareaHijaDTO> hijas = new ArrayList<>();

    @Descripcion("Etiquetas de la tarea, en orden alfabético. Al guardar reemplaza la lista completa: las que no vienen se quitan")
    @Valid
    @Size(max = 20, message = "Una tarea puede tener hasta 20 etiquetas")
    @Coleccion(value = "etiquetas", orden = "texto")
    private List<TareaEtiquetaDTO> etiquetas = new ArrayList<>();
}
