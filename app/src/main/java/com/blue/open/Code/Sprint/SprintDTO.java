package com.blue.open.Code.Sprint;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Proyeccion.Columna;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * DTO del ABM de sprints: lo usan obtener, paginar, guardar y las acciones del ciclo del sprint.
 */
@Setter
@Getter
@NoArgsConstructor
public class SprintDTO extends EntidadBaseDTO {

    @Descripcion("Uuid del proyecto del sprint. No cambia después de crearlo")
    @NotBlank(message = "Elegí el proyecto del sprint")
    @Columna("proyecto.uuid")
    private String proyectoUuid;

    @Descripcion("Prefijo del proyecto. Solo lectura")
    @Columna("proyecto.prefijo")
    private String proyectoPrefijo;

    @Descripcion("Nombre del sprint")
    @NotBlank(message = "Escribí el nombre del sprint")
    @Size(max = 120, message = "El nombre puede tener hasta 120 caracteres")
    private String nombre;

    @Descripcion("Fecha de inicio planificada, sin hora (aaaa-mm-dd)")
    @NotNull(message = "Elegí la fecha de inicio")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate inicio;

    @Descripcion("Fecha de fin planificada, sin hora (aaaa-mm-dd). No puede ser anterior al inicio")
    @NotNull(message = "Elegí la fecha de fin")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fin;

    @Descripcion("Estado: PLANIFICADO, ACTIVO o CERRADO. Solo lectura: cambia con iniciar y cerrar")
    private EstadoDeSprint estado;

    @Descripcion("Nombre del usuario que cerró el sprint. Solo lectura; vacío si no está cerrado")
    @Columna("cerradoPor.nombre")
    private String cerradoPorNombre;

    @Descripcion("Fecha y hora del cierre. Solo lectura; vacía si no está cerrado")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
    private Timestamp fechaDeCierre;
}
