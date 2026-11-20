package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Ultimo uso de un token de acceso personal (propuesta 50 resuelta el 19/11). Una fila por token, en una tabla de
 * Shiro: el token (AccessToken, auth_access_tokens) es del Core y no se modifica. Lo escribe RegistroDeUsoDeToken al
 * autenticar con el token.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shr_uso_de_token")
public class UsoDeToken extends EntidadConBorradoLogico {

    // Id del AccessToken del Core
    private Long idAccessToken;

    // Ultima vez que se autentico con el token. Solo cambia con usar
    @Setter(AccessLevel.NONE)
    private Timestamp ultimoUso;

    // Uso de un token, con la fecha y hora recibida
    public UsoDeToken(Long idAccessToken, Timestamp ahora) {
        this.idAccessToken = idAccessToken;
        this.ultimoUso = ahora;
    }

    // Registra un uso nuevo: el ultimo uso pasa a ser la fecha y hora recibida
    public void usar(Timestamp ahora) {
        this.ultimoUso = ahora;
    }
}
