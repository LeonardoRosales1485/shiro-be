package com.blue.open.Code.Comentario;

import com.blue.open.Code.Common.Canal.CanalDeCambio;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Proyeccion.Columna;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * DTO del ABM de comentarios: lo usan obtener, paginar y guardar. Autor, canal y fecha los decide el servidor.
 */
@Setter
@Getter
@NoArgsConstructor
public class ComentarioDTO extends EntidadBaseDTO {

    @Descripcion("Uuid de la tarea comentada. No cambia al editar el comentario")
    @NotBlank(message = "Elegí la tarea que comentás")
    @Columna("tarea.uuid")
    private String tareaUuid;

    @Descripcion("Cuerpo del comentario en Markdown")
    @NotBlank(message = "Escribí el comentario")
    @Size(max = 20000, message = "El comentario puede tener hasta 20000 caracteres")
    private String cuerpo;

    @Descripcion("Uuid del autor: quien escribió el comentario. Solo lectura")
    @Columna("autor.uuid")
    private String autorUuid;

    @Descripcion("Nombre del autor. Solo lectura")
    @Columna("autor.nombre")
    private String autorNombre;

    @Descripcion("Canal por el que se escribió: WEB, MCP o IMPORTACION. Solo lectura")
    private CanalDeCambio canal;

    @Descripcion("Fecha del comentario; en los copiados al clonar o importados, la original. Solo lectura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
    private Timestamp fecha;
}
