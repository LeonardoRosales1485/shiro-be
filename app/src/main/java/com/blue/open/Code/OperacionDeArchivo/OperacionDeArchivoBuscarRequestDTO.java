package com.blue.open.Code.OperacionDeArchivo;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request de la busqueda de archivados: filtros opcionales y la pagina.
 */
@Setter
@Getter
@NoArgsConstructor
public class ArchivoPaginarRequestDTO extends EntidadBaseDTO {

    @Descripcion("Tipo: TAREA, PROYECTO, SPRINT, VERSION o USUARIO. Vacío para todos")
    private String tipo;

    @Descripcion("Texto a buscar en el nombre visible, sin distinguir mayúsculas. Vacío para todos")
    private String texto;

    @Descripcion("Número de página, desde 1")
    @Min(value = 1, message = "La página empieza en 1")
    private int pagina = 1;

    @Descripcion("Elementos por página, de 1 a 200")
    @Min(value = 1, message = "El tamaño mínimo es 1")
    @Max(value = 200, message = "El tamaño máximo es 200")
    private int tamano = 50;
}
