package com.blue.open.Code.Usuario.CasosDeUso;

import com.blue.open.Code.Usuario.GrupoDeUsuarioDTO;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: grupos vigentes de un usuario (vigente o archivado), por nombre (T2.5).
 * Dependencias externas: la base de datos.
 * Recibe: uuid del usuario. Responde: lista de GrupoDeUsuarioDTO. Lanza REGISTRO_NO_ENCONTRADO si no existe.
 */
@Service
@RequiredArgsConstructor
public class UsuarioGruposUseCase {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<GrupoDeUsuarioDTO> ejecutar(String uuid) {
        Usuario usuario = usuarioRepository.findFirstByUuid(uuid)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "UsuarioGruposUseCase | ejecutar | usuario " + uuid));
        return usuarioRepository.gruposDe(usuario.getId());
    }
}
