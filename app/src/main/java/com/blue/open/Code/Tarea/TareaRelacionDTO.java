package com.blue.open.Code.Tarea;

import com.blue.open.Code.Relacion.TipoDeRelacion;
import com.blue.open.Code.Relacion.VinculoDeTarea;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Relacion de una tarea vista desde ella (CU-TAR-08): el sentido con su nombre (BLOQUEADA_POR, CLONADA_DE, ...) y
 * la otra tarea, que puede estar archivada. uuid es el de la relacion (para archivarla). Lo arma
 * RelacionRepository.listarDeTarea con una proyeccion.
 */
@Setter
@Getter
@NoArgsConstructor
public class TareaRelacionDTO extends EntidadBaseDTO {

    @Descripcion("Cómo ve esta tarea la relación: BLOQUEA, BLOQUEADA_POR, RELACIONA, DUPLICA, DUPLICADA_POR, CLONADA_EN o CLONADA_DE")
    private VinculoDeTarea vinculo;

    @Descripcion("Uuid de la otra tarea")
    private String tareaUuid;

    @Descripcion("Clave de la otra tarea")
    private String tareaClave;

    @Descripcion("Título de la otra tarea")
    private String tareaTitulo;

    @Descripcion("Estado actual de la otra tarea (en un clon, el estado del original o del clon)")
    private String tareaEstadoNombre;

    @Descripcion("Si la otra tarea está archivada: el vínculo queda y la muestra archivada")
    private boolean tareaArchivada;

    // Constructor de la proyeccion: el vinculo sale del tipo guardado y de si esta tarea es el origen
    public TareaRelacionDTO(String uuid, TipoDeRelacion tipo, Boolean esOrigen, String tareaUuid, String tareaClave,
            String tareaTitulo, String tareaEstadoNombre, Integer tareaStatus) {
        setUuid(uuid);
        this.vinculo = tipo.vinculo(Boolean.TRUE.equals(esOrigen));
        this.tareaUuid = tareaUuid;
        this.tareaClave = tareaClave;
        this.tareaTitulo = tareaTitulo;
        this.tareaEstadoNombre = tareaEstadoNombre;
        this.tareaArchivada = tareaStatus != null && tareaStatus == -1;
    }
}
