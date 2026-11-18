package com.blue.open.Code.Usuario;

import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Identity.Repository.UsuarioRepositorio;
import com.blue.open.Core.Identity.Service.CookiesDeSesion;
import com.blue.open.Core.Protocolo.Jwt.AuthJwtService;
import com.blue.open.Core.Protocolo.Jwt.JwtRefreshTokenRepository;
import com.blue.open.Core.Protocolo.Jwt.JwtTokenDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Doble de AuthJwtService (core-protocolo-jwt) para las pruebas de contrato (profile "test"): el JWT que responden
 * POST /usuario/iniciar-sesion y POST /usuario/refresh-token es un texto fijo en vez de uno firmado con la fecha actual, para que el
 * ejemplo publicado en docs/api sea igual en cada corrida (-Dcontratos.verificar). El refresh token es el real (un uuid
 * que el contrato normaliza), asi que el flujo de renovacion se prueba completo. Las pruebas se autentican con tokens
 * de acceso, no con este JWT; la que prueba la cookie access_token pide uno firmado con jwtFirmado.
 * Dependencias externas: la base de datos (usuarios y auth_jwt_refresh_tokens).
 */
@Primary
@Profile("test")
@Service
public class AuthJwtServiceDePrueba extends AuthJwtService {

    // JWT fijo de los ejemplos del contrato
    public static final String JWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvLWRlLXBydWViYSJ9.firma-de-prueba";

    private final UsuarioRepositorio usuarioRepositorio;

    // Recibe las dependencias de AuthJwtService
    public AuthJwtServiceDePrueba(UsuarioRepositorio usuarioRepositorio,
            JwtRefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
            CookiesDeSesion cookiesDeSesion) {
        super(usuarioRepositorio, refreshTokenRepository, passwordEncoder, cookiesDeSesion);
        this.usuarioRepositorio = usuarioRepositorio;
    }

    // JWT real (firmado y vigente) del usuario con el uuid recibido, para autenticar con la cookie access_token.
    // Lanza IllegalArgumentException si el usuario no existe
    public String jwtFirmado(String uuidUsuario) {
        UsuarioBase usuario = usuarioRepositorio.porUuid(uuidUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario " + uuidUsuario));
        return super.generarToken(usuario).getToken();
    }

    // Genera el par de tokens real y reemplaza el JWT por el fijo
    @Override
    public JwtTokenDTO generarToken(UsuarioBase usuario) {
        JwtTokenDTO tokens = super.generarToken(usuario);
        tokens.setToken(JWT);
        return tokens;
    }

    // Renueva el par de tokens real y reemplaza el JWT por el fijo
    @Override
    public JwtTokenDTO renovarToken(String refreshToken) {
        JwtTokenDTO tokens = super.renovarToken(refreshToken);
        tokens.setToken(JWT);
        return tokens;
    }
}
