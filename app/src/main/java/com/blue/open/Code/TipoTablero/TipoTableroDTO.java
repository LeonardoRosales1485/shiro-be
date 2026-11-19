package com.blue.open.Code.TipoTablero;

import com.blue.open.Code.Estado.EstadoDTO;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Proyeccion.Coleccion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

/**
 * DTO de lectura de un tipo de tablero con sus estados en orden de columna. Lo responde el catalogo de tableros
 * (GET /tipo-tablero/listar).
 */
@Setter
@Getter
@NoArgsConstructor
public class TipoTableroDTO extends EntidadBaseDTO {

    @Descripcion("Código estable del tablero: PRINCIPAL, QA, ANALISIS o DISENO")
    private String codigo;

    @Descripcion("Nombre visible del tablero")
    private String nombre;

    @Descripcion("Posición del tablero en las pestañas del proyecto; 1 es el principal")
    private Integer orden;

    @Descripcion("Estados del tablero en orden de columna")
    @Coleccion(value = "estados", orden = "orden")
    private List<EstadoDTO> estados;

    // Proyeccion de TipoTableroRepository: arma el DTO directamente desde la consulta (los estados los completa el
    // Proyector con @Coleccion)
    public TipoTableroDTO(String uuid, Integer status, Timestamp fechaDeCreacion, String codigo, String nombre,
            Integer orden) {
        this.uuid = uuid;
        this.status = status;
        this.fechaDeCreacion = fechaDeCreacion;
        this.codigo = codigo;
        this.nombre = nombre;
        this.orden = orden;
    }
}
