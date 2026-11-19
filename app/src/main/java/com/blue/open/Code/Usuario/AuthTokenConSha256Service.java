package com.blue.open.Code.Usuario;

import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Identity.Repository.UsuarioRepositorio;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Protocolo.ApiKey.AccessToken;
import com.blue.open.Core.Protocolo.ApiKey.AccessTokenDTO;
import com.blue.open.Core.Protocolo.ApiKey.AccessTokenRepository;
import com.blue.open.Core.Protocolo.ApiKey.AuthTokenService;
import com.blue.open.Core.Protocolo.ApiKey.Excepciones.CodigoDeErrorApiKey;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * Protocolo "tokens" con hash (T2.1 y Spec 2): reemplaza a AuthTokenService de core-protocolo-apikey 4.0.0, que
 * guarda el token en texto plano. Guarda solo el SHA-256 del token, autentica buscando por ese hash y muestra el token
 * una unica vez, en la respuesta de crear-token. Los endpoints siguen siendo los del Core (AccessTokenController),
 * que reciben este bean. AuthTokenConSha256Config saca del contexto el bean original. Se retira cuando el Core
 * publique 4.1.0 con la misma logica.
 * Dependencias externas: la base de datos (auth_access_tokens).
 */
@Component
public class AuthTokenConSha256Service extends AuthTokenService {

    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final int LARGO = 128;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    // Tokens recien creados en este hilo (uuid -> token en claro), para mostrarlos una sola vez en la respuesta
    private static final ThreadLocal<Map<String, String>> RECIEN_CREADOS = ThreadLocal.withInitial(HashMap::new);

    private final AccessTokenRepository repositorio;

    private final UsuarioRepositorio usuarios;

    // Recibe los repositorios que usa AuthTokenService
    public AuthTokenConSha256Service(AccessTokenRepository repositorio, UsuarioRepositorio usuarios) {
        super(repositorio, usuarios);
        this.repositorio = repositorio;
        this.usuarios = usuarios;
    }

    // SHA-256 del texto en hexadecimal (64 caracteres): lo que guarda auth_access_tokens.codigo
    public static String sha256(String texto) {
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException e) {
            throw new ExcepcionDeApi(CodigoDeErrorKernel.ERROR_INTERNO, "AuthTokenConSha256Service | sha256 | sin SHA-256", e);
        }
    }

    /*
     * Usuario duenio del token del header Authorization (Bearer, no JWT), buscado por el SHA-256. Responde null si la
     * request no trae un token de este tipo. Lanza ACCESS_TOKEN_INVALIDO si no existe, esta desactivado o eliminado.
     */
    @Override
    public UsuarioBase getUsuario() {
        String token = tokenDelEncabezado();
        if (token == null) {
            return null;
        }
        AccessToken modelo = repositorio.findFirstByCodigo(sha256(token))
                .filter(t -> t.getIdUsuario() != null && t.getStatus() == 0 && t.getActivo() == 1)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorApiKey.ACCESS_TOKEN_INVALIDO,
                        "AuthTokenConSha256Service | getUsuario | token no encontrado o sin usuario"));
        // orElse(null): el contrato de EstrategiaDeAutenticacion.getUsuario() es devolver null si no resuelve
        return usuarios.porId(modelo.getIdUsuario()).orElse(null);
    }

    /*
     * Crea un token de 128 caracteres para el usuario y guarda solo su hash. El token en claro queda disponible para
     * la respuesta de este request (proyectar). Lanza TOKEN_NOMBRE_DUPLICADO si el usuario ya tiene uno con ese nombre.
     */
    @Override
    public AccessToken crearAccessToken(UsuarioBase usuario, String nombre) {
        if (repositorio.findFirstByNombreAndIdUsuarioAndStatus(nombre, usuario.getId(), 0).isPresent())
            throw new ExcepcionDeApi(CodigoDeErrorApiKey.TOKEN_NOMBRE_DUPLICADO, "Ya existe un token con el nombre: " + nombre,
                    "AuthTokenConSha256Service | crearAccessToken | nombre duplicado: " + nombre);
        String codigo = nuevoCodigo(nombre);
        AccessToken token = new AccessToken();
        token.setIdUsuario(usuario.getId());
        token.setNombre(nombre);
        token.setCodigo(sha256(codigo));
        token.setActivo(1);
        AccessToken guardado = repositorio.save(token);
        RECIEN_CREADOS.get().put(guardado.getUuid(), codigo);
        return guardado;
    }

    // Respuesta de crear o borrar un token: el token en claro solo si se acaba de crear en este request
    @Override
    public AccessTokenDTO proyectar(UsuarioBase usuario, String uuid) {
        AccessTokenDTO dto = super.proyectar(usuario, uuid);
        dto.setCodigo(RECIEN_CREADOS.get().remove(uuid));
        return dto;
    }

    // Tokens vigentes del usuario, sin el codigo (la base solo tiene el hash)
    @Override
    public List<AccessTokenDTO> proyectarVigentes(UsuarioBase usuario) {
        List<AccessTokenDTO> tokens = super.proyectarVigentes(usuario);
        tokens.forEach(dto -> dto.setCodigo(null));
        return tokens;
    }

    // Token en claro de un token nuevo: 128 caracteres alfanumericos aleatorios. Las pruebas lo fijan por nombre
    protected String nuevoCodigo(String nombre) {
        StringBuilder codigo = new StringBuilder(LARGO);
        for (int i = 0; i < LARGO; i++) {
            codigo.append(CARACTERES.charAt(ALEATORIO.nextInt(CARACTERES.length())));
        }
        return codigo.toString();
    }

    // Token del header Authorization si es un Bearer que no es JWT; null si no hay request o no es de este tipo
    private static String tokenDelEncabezado() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos)) {
            return null;
        }
        String encabezado = atributos.getRequest().getHeader("Authorization");
        if (encabezado == null || !encabezado.startsWith("Bearer ")) {
            return null;
        }
        String token = encabezado.substring("Bearer ".length()).trim();
        boolean esJwt = token.split("\\.", -1).length == 3;
        return token.isEmpty() || esJwt ? null : token;
    }
}
