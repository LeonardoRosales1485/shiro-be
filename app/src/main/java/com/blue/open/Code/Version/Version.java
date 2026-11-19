package com.blue.open.Code.Version;

import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Version de un Proyecto (Spec 1 y T5.3): agrupa tareas (TareaVersion) y genera su changelog. Al publicarse queda
 * fija: guarda la fecha de publicacion elegida y el changelog de ese momento, y no cambia mas.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_version")
public class Version extends EntidadConBorradoLogico {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    private String nombre;

    // Estado. Solo lo cambia publicar
    @Enumerated(EnumType.STRING)
    private EstadoDeVersion estado = EstadoDeVersion.SIN_PUBLICAR;

    // Fecha de publicacion elegida al publicar, sin hora; null si no esta publicada
    private LocalDate fechaDePublicacion;

    // Changelog en Markdown guardado al publicar; null si no esta publicada (se arma en el momento)
    @Column(columnDefinition = "TEXT")
    private String changelog;

    // Operacion de archivo que la archivo; null si esta vigente
    private Long idOperacionArchivo;
}
