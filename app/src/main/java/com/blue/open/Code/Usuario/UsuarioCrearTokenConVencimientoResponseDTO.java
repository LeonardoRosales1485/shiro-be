package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Respuesta de crear un token de acceso eligiendo su vigencia (CU-USR-28, MJ-14): el token en claro, que se muestra
 * esta unica vez, y cuando vence.
 */
@Setter
@Getter
@NoArgsConstructor
public class UsuarioCrearTokenConVencimientoResponseDTO extends EntidadBaseDTO {

    @Descripcion("Nombre con el que el usuario identifica el token")
    private String nombre;

    @Descripcion("Token en claro para el encabezado Authorization: Bearer. Se muestra solo en esta respuesta")
    private String codigo;

    @Descripcion("Desde cuándo el token deja de servir")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
    private Timestamp vence;

    // Token recien creado con su codigo en claro y su vencimiento
    public UsuarioCrearTokenConVencimientoResponseDTO(String uuid, String nombre, String codigo, Timestamp vence) {
        setUuid(uuid);
        this.nombre = nombre;
        this.codigo = codigo;
        this.vence = vence;
    }
}
