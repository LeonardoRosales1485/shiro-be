package com.blue.open.Code.Usuario.CasosDeUso;

import com.blue.open.Code.Autorizacion.ProteccionDeAdministradores;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioDTO;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: restaura un usuario archivado (T2.5). Vuelve a ingresar; sus tokens anteriores siguen desactivados
 * (crea otros). Deja una fila RESTAURAR en su historial.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del usuario. Responde: UsuarioDTO. Lanza REGISTRO_NO_ENCONTRADO o SIN_PERMISO.
 */
@Service
@RequiredArgsConstructor
public class UsuarioRestaurarUseCase {

    private final UsuarioRepository usuarioRepository;

    private final ProteccionDeAdministradores proteccion;

    private final RegistradorDeHistorial historial;

    private final Proyector proyector;

    @Transactional
    public UsuarioDTO ejecutar(String uuid) {
        Usuario usuario = usuarioRepository.findFirstByUuidAndStatus(uuid, -1)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "UsuarioRestaurarUseCase | ejecutar | usuario archivado " + uuid));
        proteccion.exigirQuePuedaAdministrar(usuario);
        usuario.setStatus(0);
        usuario.setFechaDeBorrado(null);
        usuario.setIdUsuarioDeBorrado(null);
        usuarioRepository.save(usuario);
        historial.registrar(Historial.USUARIO, uuid, AccionDeHistorial.RESTAURAR, null, null, usuario.getNombreDeUsuario());
        return proyector.porUuid(Usuario.class, uuid, UsuarioDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "UsuarioRestaurarUseCase | ejecutar | no se pudo proyectar " + uuid));
    }
}
