package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Token de acceso del usuario en "mis tokens" (CU-USR-25, propuesta 50 resuelta el 19/11): nombre, si esta activo,
 * cuando se creo, cuando vence (MJ-14), cuando se revoco y su ultimo uso. Nunca trae el token. Lo arma UsoDeTokenRepository.tokensDe.
 */
@Setter
@Getter
@NoArgsConstructor
public class UsuarioTokenResponseDTO extends EntidadBaseDTO {

    @Descripcion("Nombre con el que el usuario identifica el token")
    private String nombre;

    @Descripcion("Si el token sirve para entrar; false si fue revocado")
    private boolean activo;

    @Descripcion("Cuándo se revocó. Vacío si está activo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
    private Timestamp fechaDeRevocacion;

    @Descripcion("Última vez que se entró con el token (se registra a lo sumo una vez por minuto). Vacío si nunca se usó")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
    private Timestamp ultimoUso;

    @Descripcion("Desde cuándo el token deja de servir (MJ-14)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
    private Timestamp vence;

    // Token con sus datos, su ultimo uso y su vencimiento
    public UsuarioTokenResponseDTO(String uuid, String nombre, boolean activo, Timestamp fechaDeCreacion, Timestamp fechaDeRevocacion,
            Timestamp ultimoUso, Timestamp vence) {
        setUuid(uuid);
        setFechaDeCreacion(fechaDeCreacion);
        this.nombre = nombre;
        this.activo = activo;
        this.fechaDeRevocacion = activo ? null : fechaDeRevocacion;
        this.ultimoUso = ultimoUso;
        this.vence = vence;
    }
}
