package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Request del caso de uso crear un token de acceso eligiendo su vigencia (CU-USR-28, MJ-14 y decision D-3): el nombre
 * del token y por cuantos dias sirve (30, 90 o 180).
 */
@Setter
@Getter
@NoArgsConstructor
public class UsuarioCrearTokenConVencimientoRequestDTO extends EntidadBaseDTO {

    // Vigencias que se pueden elegir, en dias
    private static final Set<Integer> DIAS_PERMITIDOS = Set.of(30, 90, 180);

    @Descripcion("Nombre con el que el usuario identifica el token. No se repite entre sus tokens vigentes")
    @NotBlank(message = "Escribí un nombre para el token")
    @Size(max = 255, message = "El nombre puede tener hasta 255 caracteres")
    private String nombre;

    @Descripcion("Por cuántos días sirve el token desde ahora: 30, 90 o 180")
    @NotNull(message = "Elegí la vigencia del token")
    private Integer diasDeVigencia;

    // Valida que la vigencia sea una de las permitidas (si falta, la reporta @NotNull)
    @JsonIgnore
    @AssertTrue(message = "La vigencia puede ser de 30, 90 o 180 días")
    public boolean isDiasDeVigenciaPermitidos() {
        return diasDeVigencia == null || DIAS_PERMITIDOS.contains(diasDeVigencia);
    }
}
