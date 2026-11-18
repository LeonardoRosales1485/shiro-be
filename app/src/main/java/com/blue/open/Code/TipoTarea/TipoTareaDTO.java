package com.blue.open.Code.TipoTarea;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * DTO de lectura de un tipo de tarea. Lo responde el catalogo de tipos (GET /tipo-tarea/listar).
 */
@Setter
@Getter
@NoArgsConstructor
public class TipoTareaDTO extends EntidadBaseDTO {

    @Descripcion("Código estable del tipo: EPICA, HISTORIA, TAREA, BUG o SUBTAREA")
    private String codigo;

    @Descripcion("Nombre visible del tipo")
    private String nombre;

    @Descripcion("Nivel en la jerarquía: 1 Épica; 2 Historia, Tarea y Bug; 3 Subtarea. El padre tiene nivel menor")
    private Integer nivel;

    // Proyeccion de TipoTareaRepository: arma el DTO directamente desde la consulta
    public TipoTareaDTO(String uuid, Integer status, Timestamp fechaDeCreacion, String codigo, String nombre,
            Integer nivel) {
        this.uuid = uuid;
        this.status = status;
        this.fechaDeCreacion = fechaDeCreacion;
        this.codigo = codigo;
        this.nombre = nombre;
        this.nivel = nivel;
    }
}
