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
 * Vencimiento de un token de acceso personal (MJ-14 del plan de mejoras y decision D-3). Una fila por token, en una
 * tabla de Shiro: el token (AccessToken, auth_access_tokens) es del Core y no se modifica. La escribe
 * AuthTokenConSha256Service al crear el token; pasado el vencimiento el token deja de autenticar.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shr_vencimiento_de_token")
public class VencimientoDeToken extends EntidadConBorradoLogico {

    // Id del AccessToken del Core
    private Long idAccessToken;

    // Fecha y hora desde la que el token ya no autentica. Se fija al crear el token
    @Setter(AccessLevel.NONE)
    private Timestamp vence;

    // Vencimiento de un token: la fecha y hora recibida
    public VencimientoDeToken(Long idAccessToken, Timestamp vence) {
        this.idAccessToken = idAccessToken;
        this.vence = vence;
    }

    // Si el token ya vencio en el momento recibido
    public boolean vencidoEn(Timestamp momento) {
        return !momento.before(vence);
    }
}
