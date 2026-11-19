package com.blue.open.Code.Common.Google;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Set;

/**
 * Valida el ID token que el front obtiene con Google Identity Services (T2.2 y Spec 2): firma RS256 con las claves
 * publicas de Google, emisor de Google, audiencia igual al client id configurado, vencimiento, email_verified y hd
 * igual al dominio configurado. Responde el correo y el nombre.
 * Dependencias externas: Google (claves publicas, via ClavesPublicasDeGoogle).
 */
@Component
@RequiredArgsConstructor
public class VerificadorDeTokenDeGoogle {

    private static final Set<String> EMISORES = Set.of("https://accounts.google.com", "accounts.google.com");

    private final ClavesPublicasDeGoogle claves;

    // Client id OAuth de Shiro en Google; vacio deshabilita el ingreso con Google
    @Value("${shiro.google.client-id:}")
    private String clientId;

    // Dominio de Google Workspace permitido (claim hd); vacio deshabilita el ingreso con Google
    @Value("${shiro.google.dominio:}")
    private String dominio;

    /*
     * Valida el ID token y responde la identidad. Lanza GOOGLE_NO_CONFIGURADO si falta el client id o el dominio,
     * TOKEN_DE_GOOGLE_INVALIDO si la firma, el emisor, la audiencia, el vencimiento o el correo no verifican, y
     * DOMINIO_DE_GOOGLE_NO_PERMITIDO si la cuenta no es del dominio configurado.
     */
    public IdentidadDeGoogle verificar(String idToken) {
        if (clientId == null || clientId.isBlank() || dominio == null || dominio.isBlank())
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.GOOGLE_NO_CONFIGURADO,
                    "VerificadorDeTokenDeGoogle | verificar | falta shiro.google.client-id o shiro.google.dominio");
        Claims claims = validarFirma(idToken);
        if (!EMISORES.contains(claims.getIssuer()) || !Boolean.TRUE.equals(claims.get("email_verified", Boolean.class))
                || claims.get("email", String.class) == null)
            throw invalido("emisor " + claims.getIssuer() + " o correo sin verificar");
        if (!dominio.equalsIgnoreCase(claims.get("hd", String.class)))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.DOMINIO_DE_GOOGLE_NO_PERMITIDO,
                    "VerificadorDeTokenDeGoogle | verificar | hd " + claims.get("hd", String.class));
        return new IdentidadDeGoogle(claims.get("email", String.class), claims.get("name", String.class));
    }

    // Verifica firma, audiencia y vencimiento con jjwt. Lanza TOKEN_DE_GOOGLE_INVALIDO si algo no verifica
    private Claims validarFirma(String idToken) {
        try {
            return Jwts.parserBuilder().requireAudience(clientId).setSigningKeyResolver(new SigningKeyResolverAdapter() {
                @Override
                public Key resolveSigningKey(JwsHeader encabezado, Claims claims) {
                    return claves.clave(encabezado.getKeyId())
                            .orElseThrow(() -> new JwtException("kid desconocido: " + encabezado.getKeyId()));
                }
            }).build().parseClaimsJws(idToken).getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw invalido(e.getClass().getSimpleName());
        }
    }

    // Error de token invalido, sin datos del token en el mensaje
    private static ExcepcionDeApi invalido(String motivo) {
        return new ExcepcionDeApi(CodigoDeErrorDelProyecto.TOKEN_DE_GOOGLE_INVALIDO,
                "VerificadorDeTokenDeGoogle | verificar | " + motivo);
    }
}
