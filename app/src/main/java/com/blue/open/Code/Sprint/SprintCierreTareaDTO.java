package com.blue.open.Code.Sprint;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una tarea en el cierre de un sprint (CU-SPR-09): como estaba al cerrar y adonde fue. La arma
 * TareaDeSprintCerradoRepository.tareasDelCierre. El uuid es el de la tarea.
 */
@Setter
@Getter
@NoArgsConstructor
public class SprintCierreTareaDTO extends EntidadBaseDTO {

    @Descripcion("Clave de la tarea")
    private String clave;

    @Descripcion("Título de la tarea")
    private String titulo;

    @Descripcion("Código del tablero de la tarea: PRINCIPAL, QA, ANALISIS o DISENO")
    private String tableroCodigo;

    @Descripcion("Nombre del estado de la tarea al cerrar el sprint")
    private String estadoAlCerrar;

    @Descripcion("Si la tarea estaba terminada al cerrar el sprint")
    private boolean terminada;

    @Descripcion("Puntos de la tarea al cerrar el sprint. Vacío si no estaba estimada")
    private Integer puntos;

    @Descripcion("Adónde fue: TERMINADA (se quedó en el sprint), BACKLOG o SPRINT (pasó a otro sprint)")
    private DestinoAlCerrar destino;

    @Descripcion("Nombre del sprint al que pasó. Vacío si no pasó a otro sprint")
    private String sprintDestinoNombre;

    // Tarea del cierre con sus datos al cerrar
    public SprintCierreTareaDTO(String uuid, String clave, String titulo, String tableroCodigo, String estadoAlCerrar, boolean terminada,
            Integer puntos, DestinoAlCerrar destino, String sprintDestinoNombre) {
        setUuid(uuid);
        this.clave = clave;
        this.titulo = titulo;
        this.tableroCodigo = tableroCodigo;
        this.estadoAlCerrar = estadoAlCerrar;
        this.terminada = terminada;
        this.puntos = puntos;
        this.destino = destino;
        this.sprintDestinoNombre = sprintDestinoNombre;
    }
}
