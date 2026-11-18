package com.blue.open.Code.Proyecto;

import com.blue.open.Code.Tablero.TableroDTO;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Proyeccion.Coleccion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO del ABM de proyectos: lo reciben y responden uuid, paginar y guardar. Los tableros son de solo lectura: se crean
 * con el proyecto.
 */
@Setter
@Getter
@NoArgsConstructor
public class ProyectoDTO extends EntidadBaseDTO {

    @Descripcion("Nombre visible del proyecto")
    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(max = 120, message = "El nombre del proyecto tiene como máximo 120 caracteres")
    private String nombre;

    @Descripcion("Prefijo de las claves de tarea (por ejemplo SHI en SHI-123). Se guarda en mayúsculas, no se repite entre proyectos, tampoco archivados, y no se cambia después de crear el proyecto")
    @NotBlank(message = "El prefijo es obligatorio")
    @Pattern(regexp = "^[A-Za-z]{2,6}$", message = "El prefijo tiene de 2 a 6 letras, sin números ni símbolos")
    private String prefijo;

    @Descripcion("Descripción libre del proyecto. Opcional")
    @Size(max = 2000, message = "La descripción tiene como máximo 2000 caracteres")
    private String descripcion;

    @Descripcion("Tableros del proyecto, uno por tipo, en orden de pestaña. Solo lectura: se crean con el proyecto")
    @Coleccion("tableros")
    private List<TableroDTO> tableros;
}
