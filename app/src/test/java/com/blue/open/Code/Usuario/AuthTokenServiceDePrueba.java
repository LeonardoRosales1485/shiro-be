package com.blue.open.Code.Usuario;

import com.blue.open.Core.Identity.Repository.UsuarioRepositorio;
import com.blue.open.Core.Protocolo.ApiKey.AccessTokenRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Doble de AuthTokenConSha256Service para las pruebas de contrato (profile "test"): el token creado por
 * POST /usuario/crear-token lleva un codigo fijo derivado de su nombre en vez de 128 caracteres aleatorios, para que el
 * ejemplo publicado en docs/api sea igual en cada corrida (-Dcontratos.verificar). Todo lo demas (hash, autenticacion,
 * borrado) es la logica real.
 * Dependencias externas: la base de datos (auth_access_tokens).
 */
@Primary
@Profile("test")
@Component
public class AuthTokenServiceDePrueba extends AuthTokenConSha256Service {

    // Recibe los repositorios que usa AuthTokenConSha256Service
    public AuthTokenServiceDePrueba(AccessTokenRepository repositorio, UsuarioRepositorio usuarioRepositorio) {
        super(repositorio, usuarioRepositorio);
    }

    // Codigo fijo del token con el nombre recibido (ej. "Token de prueba" -> "token-de-prueba-token-de-prueba")
    public static String codigo(String nombre) {
        return "token-de-prueba-" + nombre.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }

    // Token en claro fijo, derivado del nombre
    @Override
    protected String nuevoCodigo(String nombre) {
        return codigo(nombre);
    }
}
