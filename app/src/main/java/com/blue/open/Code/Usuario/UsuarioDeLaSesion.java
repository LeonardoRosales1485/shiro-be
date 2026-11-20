package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Entidad Usuario de quien hace el pedido, para guardarlo en una relacion (informante de una tarea nueva, quien
 * cierra un sprint o publica una version).
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class UsuarioDeLaSesion {

    private final ProveedorDeUsuarioActual usuarioActual;

    private final UsuarioRepository usuarioRepository;

    // Usuario de la sesion. Lanza NO_AUTENTICADO si no hay sesion o si el usuario ya no existe
    public Usuario obtener() {
        Long id = usuarioActual.getIdUsuarioActual();
        if (id == null)
            throw new ExcepcionDeApi(CodigoDeErrorKernel.NO_AUTENTICADO, "UsuarioDeLaSesion | obtener | sin sesion");
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorKernel.NO_AUTENTICADO, "UsuarioDeLaSesion | obtener | no existe el usuario"));
    }
}
