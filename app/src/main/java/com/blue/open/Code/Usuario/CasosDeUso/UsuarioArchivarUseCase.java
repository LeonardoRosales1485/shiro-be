package com.blue.open.Code.Usuario.CasosDeUso;

import com.blue.open.Code.Autorizacion.ProteccionDeAdministradores;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioDTO;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import com.blue.open.Core.Protocolo.ApiKey.AccessToken;
import com.blue.open.Core.Protocolo.ApiKey.AccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

/**
 * Caso de uso: archiva un usuario (T2.5 y Spec 7). Ya no ingresa, sus tokens de acceso se desactivan y sus tareas
 * conservan el responsable. Nadie se archiva a si mismo, y los Administradores no archivan al super admin ni a otros
 * Administradores. Deja una fila ARCHIVAR en el historial del usuario.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del usuario. Responde: UsuarioDTO. Lanza REGISTRO_NO_ENCONTRADO, USUARIO_NO_SE_ARCHIVA_A_SI_MISMO o
 * SIN_PERMISO.
 */
@Service
@RequiredArgsConstructor
public class UsuarioArchivarUseCase {

    private final UsuarioRepository usuarioRepository;

    private final AccessTokenRepository accessTokenRepository;

    private final ProteccionDeAdministradores proteccion;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final RegistradorDeHistorial historial;

    private final Proyector proyector;

    @Transactional
    public UsuarioDTO ejecutar(String uuid) {
        Usuario usuario = usuarioRepository.findFirstByUuidAndStatus(uuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "UsuarioArchivarUseCase | ejecutar | usuario vigente " + uuid));
        Long actual = usuarioActual.getIdUsuarioActual();
        if (usuario.getId().equals(actual))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.USUARIO_NO_SE_ARCHIVA_A_SI_MISMO,
                    "UsuarioArchivarUseCase | ejecutar | " + uuid);
        proteccion.exigirQuePuedaAdministrar(usuario);
        usuario.setStatus(-1);
        usuario.setFechaDeBorrado(new Timestamp(System.currentTimeMillis()));
        usuario.setIdUsuarioDeBorrado(actual);
        usuarioRepository.save(usuario);
        // Un usuario archivado no ingresa: sus tokens de acceso dejan de autenticar
        for (AccessToken token : accessTokenRepository.findAllByIdUsuarioAndStatus(usuario.getId(), 0)) {
            token.setActivo(0);
            accessTokenRepository.save(token);
        }
        historial.registrar(Historial.USUARIO, uuid, AccionDeHistorial.ARCHIVAR, null, null, usuario.getNombreDeUsuario());
        return proyector.porUuid(Usuario.class, uuid, UsuarioDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "UsuarioArchivarUseCase | ejecutar | no se pudo proyectar " + uuid));
    }
}
