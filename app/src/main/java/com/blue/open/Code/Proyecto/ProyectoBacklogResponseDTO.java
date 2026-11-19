package com.blue.open.Code.Proyecto;

import com.blue.open.Code.Tarea.PrioridadDeTarea;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tarea en el backlog del proyecto (T5.1): lo que muestra la lista del backlog. Lo arma TareaRepository.backlogDe.
 */
@Setter
@Getter
@NoArgsConstructor
public class ProyectoBacklogResponseDTO extends EntidadBaseDTO {

    @Descripcion("Clave de la tarea")
    private String clave;

    @Descripcion("Título de la tarea")
    private String titulo;

    @Descripcion("Código del tipo: HISTORIA, TAREA o BUG")
    private String tipoCodigo;

    @Descripcion("Nombre del estado actual")
    private String estadoNombre;

    @Descripcion("Código del tablero: PRINCIPAL, QA, ANALISIS o DISENO")
    private String tableroCodigo;

    @Descripcion("Prioridad de la tarea")
    private PrioridadDeTarea prioridad;

    @Descripcion("Estimación en puntos. Vacía si no está estimada")
    private Integer puntos;

    @Descripcion("Nombre del responsable. Vacío si no está asignada")
    private String responsableNombre;

    // Constructor de la proyeccion (TareaRepository.backlogDe)
    public ProyectoBacklogResponseDTO(String uuid, String clave, String titulo, String tipoCodigo, String estadoNombre,
            String tableroCodigo, PrioridadDeTarea prioridad, Integer puntos, String responsableNombre) {
        setUuid(uuid);
        this.clave = clave;
        this.titulo = titulo;
        this.tipoCodigo = tipoCodigo;
        this.estadoNombre = estadoNombre;
        this.tableroCodigo = tableroCodigo;
        this.prioridad = prioridad;
        this.puntos = puntos;
        this.responsableNombre = responsableNombre;
    }
}
