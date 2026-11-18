package com.blue.open.Code.Configuracion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * DTO de lectura de Configuracion, compartido por los casos de uso listar, obtener, obtener-varias y la respuesta de
 * guardar. El guardado recibe ConfiguracionGuardarRequestDTO, porque una clave inexistente se responde con valor null.
 */
@Setter
@Getter
@NoArgsConstructor
public class ConfiguracionDTO extends EntidadBaseDTO {
    @Descripcion("Clave de la configuración. Única: el guardado busca por este campo, no por uuid")
    private String nombre;
    @Descripcion("Valor de la configuración. null si la clave no existe")
    private String valor;
    @Descripcion("true: visible sin sesión; false: requiere el permiso CONFIGURACION_VER")
    private boolean global;

    // Proyeccion de ConfiguracionRepository: arma el DTO directamente desde la consulta
    public ConfiguracionDTO(String uuid, Integer status, Timestamp fechaDeCreacion, String nombre, String valor,
            boolean global) {
        this.uuid = uuid;
        this.status = status;
        this.fechaDeCreacion = fechaDeCreacion;
        this.nombre = nombre;
        this.valor = valor;
        this.global = global;
    }

    // Configuracion que no existe en la base: se responde publica y con valor null
    public static ConfiguracionDTO inexistente(String nombre) {
        ConfiguracionDTO dto = new ConfiguracionDTO();
        dto.setNombre(nombre);
        dto.setGlobal(true);
        return dto;
    }
}
