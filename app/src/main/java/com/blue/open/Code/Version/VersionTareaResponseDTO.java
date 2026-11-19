package com.blue.open.Code.Version;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tarea asignada a una version (CU-VER-10): lo que muestra el detalle de versiones del front. La arma
 * TareaVersionRepository.tareasDe con una proyeccion.
 */
@Setter
@Getter
@NoArgsConstructor
public class VersionTareaResponseDTO extends EntidadBaseDTO {

    @Descripcion("Clave de la tarea")
    private String clave;

    @Descripcion("Título de la tarea")
    private String titulo;

    @Descripcion("Código del tipo: EPICA, HISTORIA, TAREA, BUG o SUBTAREA")
    private String tipoCodigo;

    @Descripcion("Nombre visible del tipo")
    private String tipoNombre;

    @Descripcion("Código del tablero de la tarea: PRINCIPAL, QA, ANALISIS o DISENO")
    private String tableroCodigo;

    @Descripcion("Nombre del estado actual")
    private String estadoNombre;

    @Descripcion("Si el estado actual es terminado (Hecho, Aprobado...): solo las terminadas entran en el changelog")
    private boolean terminada;

    // Tarea con sus datos para el detalle de la version
    public VersionTareaResponseDTO(String uuid, String clave, String titulo, String tipoCodigo, String tipoNombre, String tableroCodigo,
            String estadoNombre, boolean terminada) {
        setUuid(uuid);
        this.clave = clave;
        this.titulo = titulo;
        this.tipoCodigo = tipoCodigo;
        this.tipoNombre = tipoNombre;
        this.tableroCodigo = tableroCodigo;
        this.estadoNombre = estadoNombre;
        this.terminada = terminada;
    }
}
