package com.blue.open.Code.OperacionDeArchivo;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Una tarea en la vista previa de un archivado (CU-ARC-02): se restaura con el elemento o queda archivada. La arman
 * las consultas de OperacionDeArchivoRepository.
 */
@Setter
@Getter
@NoArgsConstructor
public class ElementoDeArchivoDTO extends EntidadBaseDTO {

    @Descripcion("Clave y título de la tarea")
    private String nombre;

    @Descripcion("Cuándo se archivó")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
    private Timestamp fechaDeBorrado;

    // Tarea con su uuid, nombre y fecha de archivo
    public ElementoDeArchivoDTO(String uuid, String nombre, Timestamp fechaDeBorrado) {
        setUuid(uuid);
        this.nombre = nombre;
        this.fechaDeBorrado = fechaDeBorrado;
    }
}
