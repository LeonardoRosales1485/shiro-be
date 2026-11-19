package com.blue.open.Code.Usuario.CasosDeUso;

import com.blue.open.Code.Common.Google.IdentidadDeGoogle;
import com.blue.open.Code.Common.Google.VerificadorDeTokenDeGoogle;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Protocolo.Jwt.AuthJwtService;
import com.blue.open.Core.Protocolo.Jwt.JwtTokenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: ingresar con Google (T2.2 y Spec 2). Valida el ID token, busca el usuario vigente por correo o lo
 * crea (sin roles: no ve nada hasta que un administrador lo asigna a un grupo) y responde el JWT del Core, igual que
 * el inicio de sesion con contrasena. Un usuario archivado no entra.
 * Dependencias externas: Google (claves publicas) y la base de datos.
 * Recibe: el ID token. Responde: JwtTokenDTO. Lanza los errores de VerificadorDeTokenDeGoogle y NO_AUTENTICADO si
 * el usuario esta archivado.
 */
@Service
@RequiredArgsConstructor
public class UsuarioIngresarConGoogleUseCase {

    private static final int VIGENTE = 0;

    private static final int ARCHIVADO = -1;

    private final VerificadorDeTokenDeGoogle verificador;

    private final UsuarioRepository usuarioRepository;

    private final AuthJwtService authJwtService;

    private final RegistradorDeHistorial historial;

    @Transactional
    public JwtTokenDTO ejecutar(String idToken) {
        IdentidadDeGoogle identidad = verificador.verificar(idToken);
        Usuario usuario = usuarioRepository.findFirstByEmailAndStatus(identidad.email(), VIGENTE)
                .orElseGet(() -> crear(identidad));
        return authJwtService.generarToken(usuario);
    }

    // Crea el usuario de Google sin roles ni contrasena. Lanza NO_AUTENTICADO si el correo es de un usuario archivado
    private Usuario crear(IdentidadDeGoogle identidad) {
        if (usuarioRepository.findFirstByEmailAndStatus(identidad.email(), ARCHIVADO).isPresent())
            throw new ExcepcionDeApi(CodigoDeErrorKernel.NO_AUTENTICADO, "Tu usuario está archivado.",
                    "UsuarioIngresarConGoogleUseCase | crear | usuario archivado");
        Usuario usuario = new Usuario();
        usuario.setNombreDeUsuario(identidad.email());
        usuario.setEmail(identidad.email());
        usuario.setNombre(identidad.nombre() == null ? identidad.email() : identidad.nombre());
        Usuario guardado = usuarioRepository.save(usuario);
        historial.registrar(Historial.USUARIO, guardado.getUuid(), AccionDeHistorial.CREAR, null, null, "Google");
        return guardado;
    }
}
