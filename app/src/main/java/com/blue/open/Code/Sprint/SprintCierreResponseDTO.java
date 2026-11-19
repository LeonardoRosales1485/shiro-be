package com.blue.open.Code.Sprint;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Cierre de un sprint (CU-SPR-09, propuesta 46 resuelta el 19/11): quien lo cerro, cuando y que paso con cada tarea.
 * De un sprint sin cerrar viene sin fecha y sin tareas. Lo arma SprintCierreUseCase.
 */
@Setter
@Getter
@NoArgsConstructor
public class SprintCierreResponseDTO extends EntidadBaseDTO {

    @Descripcion("Nombre del usuario que cerró el sprint. Vacío si no está cerrado")
    private String cerradoPorNombre;

    @Descripcion("Fecha y hora del cierre. Vacía si no está cerrado")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
    private Timestamp fechaDeCierre;

    @Descripcion("Tareas que tenía el sprint al cerrarse, por número, con su estado al cerrar y su destino")
    private List<SprintCierreTareaDTO> tareas = new ArrayList<>();
}
