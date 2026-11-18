package com.blue.open.Code.Configuracion;

import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Variable de configuracion dinamica del sistema (par clave/valor), no se relaciona con otras entidades.
 * Permite cambiar parametros sin recompilar (ver ConfiguracionController y sus casos de uso).
 */
@Setter
@Getter
@Table(name = "configuraciones")
@Entity()
@AllArgsConstructor
@NoArgsConstructor
public class Configuracion extends EntidadConBorradoLogico {
    // Clave unica de la configuracion (ej. "DEBUG")
    @NotNull
    private String nombre;

    @NotNull
    @Column(name = "valor", columnDefinition = "TEXT")
    private String valor;

    // true = visible por cualquier usuario sin autenticar; false = solo con CONFIGURACION_VER o desde el backend
    @NotNull
    private boolean global;
}
