package com.blue.open.Code.Tarea;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Etiqueta de una tarea dentro de TareaDTO: se recibe al guardar y se responde al leer.
 */
@Setter
@Getter
@NoArgsConstructor
public class TareaEtiquetaDTO extends EntidadBaseDTO {

    @Descripcion("Texto de la etiqueta. Se guarda sin espacios al principio ni al final; las repetidas se guardan una vez")
    @NotBlank(message = "La etiqueta no puede estar vacía")
    @Size(max = 40, message = "La etiqueta puede tener hasta 40 caracteres")
    private String texto;
}
