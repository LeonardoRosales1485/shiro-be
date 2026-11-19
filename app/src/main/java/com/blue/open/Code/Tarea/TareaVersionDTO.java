package com.blue.open.Code.Tarea;

import com.blue.open.Code.Version.EstadoDeVersion;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Proyeccion.Columna;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Version a la que esta asignada una tarea, dentro de TareaDTO. Solo lectura: se asigna desde la version.
 */
@Setter
@Getter
@NoArgsConstructor
public class TareaVersionDTO extends EntidadBaseDTO {

    @Descripcion("Uuid de la versión")
    @Columna("version.uuid")
    private String versionUuid;

    @Descripcion("Nombre de la versión")
    @Columna("version.nombre")
    private String nombre;

    @Descripcion("Estado de la versión: SIN_PUBLICAR o PUBLICADA")
    @Columna("version.estado")
    private EstadoDeVersion estado;
}
