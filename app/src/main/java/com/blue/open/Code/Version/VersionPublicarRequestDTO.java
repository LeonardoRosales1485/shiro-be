package com.blue.open.Code.Version;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request de publicar una version: la fecha de publicacion, que elige quien publica.
 */
@Setter
@Getter
@NoArgsConstructor
public class VersionPublicarRequestDTO extends EntidadBaseDTO {

    @Descripcion("Fecha de publicación (aaaa-mm-dd). Si no viene, hoy (GMT-3)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fecha;
}
