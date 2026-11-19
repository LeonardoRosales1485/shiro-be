package com.blue.open.Code.Version;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Proyeccion.Columna;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO del ABM de versiones: lo usan obtener, paginar, guardar y las acciones de la version.
 */
@Setter
@Getter
@NoArgsConstructor
public class VersionDTO extends EntidadBaseDTO {

    @Descripcion("Uuid del proyecto de la versión. No cambia después de crearla")
    @NotBlank(message = "Elegí el proyecto de la versión")
    @Columna("proyecto.uuid")
    private String proyectoUuid;

    @Descripcion("Prefijo del proyecto. Solo lectura")
    @Columna("proyecto.prefijo")
    private String proyectoPrefijo;

    @Descripcion("Nombre de la versión, por ejemplo 1.4.0")
    @NotBlank(message = "Escribí el nombre de la versión")
    @Size(max = 60, message = "El nombre puede tener hasta 60 caracteres")
    private String nombre;

    @Descripcion("Estado: SIN_PUBLICAR o PUBLICADA. Solo lectura: cambia con publicar")
    private EstadoDeVersion estado;

    @Descripcion("Fecha de publicación elegida al publicar (aaaa-mm-dd). Vacía si no está publicada. Solo lectura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaDePublicacion;

    @Descripcion("Cantidad de tareas vigentes asignadas. Solo lectura")
    private Integer cantidadDeTareas;

    @Descripcion("Nombre del usuario que publicó la versión. Solo lectura; vacío si no está publicada")
    @Columna("publicadaPor.nombre")
    private String publicadaPorNombre;
}
