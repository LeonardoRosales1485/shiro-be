package com.blue.open.Code.Configuracion;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de cada configuracion que recibe POST /configuracion/guardar. Separado de ConfiguracionDTO porque al guardar el
 * valor es obligatorio, mientras que la lectura de una clave inexistente responde valor null.
 * El guardado busca por nombre, no por uuid.
 */
@Setter
@Getter
public class ConfiguracionGuardarRequestDTO extends EntidadBaseDTO {
    @Descripcion("Clave de la configuración. Si ya existe una vigente con esa clave, se edita")
    @NotBlank(message = "El nombre de la configuracion es obligatorio")
    private String nombre;
    @Descripcion("Valor de la configuración")
    @NotBlank(message = "El valor de la configuracion es obligatorio")
    private String valor;
    @Descripcion("true: visible sin sesión; false: requiere el permiso CONFIGURACION_VER")
    private boolean global;
}
