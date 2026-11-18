package com.blue.open.Code.Usuario;

import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Identity.Repository.UsuarioRepositorio;
import com.blue.open.Core.Protocolo.ApiKey.AuthTokenService;
import com.blue.open.Core.Protocolo.ApiKey.AccessToken;
import com.blue.open.Core.Protocolo.ApiKey.AccessTokenRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Doble de AuthTokenService (core-protocolo-apikey) para las pruebas de contrato (profile "test"): el token creado por
 * POST /usuario/crear-token lleva un codigo fijo derivado de su nombre en vez de 128 caracteres aleatorios, para que el
 * ejemplo publicado en docs/api sea igual en cada corrida (-Dcontratos.verificar). Crea el token con la logica real
 * (incluida la validacion de nombre duplicado) y despues reemplaza el codigo. La autenticacion y el borrado son los del
 * componente real.
 * Dependencias externas: la base de datos (auth_access_tokens).
 */
@Primary
@Profile("test")
@Component
public class AuthTokenServiceDePrueba extends AuthTokenService {

    private final AccessTokenRepository repositorio;

    // Recibe los repositorios que usa AuthTokenService
    public AuthTokenServiceDePrueba(AccessTokenRepository repositorio, UsuarioRepositorio usuarioRepositorio) {
        super(repositorio, usuarioRepositorio);
        this.repositorio = repositorio;
    }

    // Codigo fijo del token con el nombre recibido (ej. "Token de prueba" -> "token-de-prueba-token-de-prueba")
    public static String codigo(String nombre) {
        return "token-de-prueba-" + nombre.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }

    // Crea el token con la logica real y le asigna el codigo fijo de su nombre
    @Override
    public AccessToken crearAccessToken(UsuarioBase usuario, String nombre) {
        AccessToken token = super.crearAccessToken(usuario, nombre);
        token.setCodigo(codigo(nombre));
        return repositorio.save(token);
    }
}
