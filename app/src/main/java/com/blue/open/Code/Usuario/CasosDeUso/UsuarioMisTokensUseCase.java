package com.blue.open.Code.Usuario.CasosDeUso;

import com.blue.open.Code.Usuario.UsoDeTokenRepository;
import com.blue.open.Code.Usuario.UsuarioDeLaSesion;
import com.blue.open.Code.Usuario.UsuarioTokenResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: mis tokens de acceso (CU-USR-25, propuesta 50 resuelta el 19/11): todos los tokens del usuario de la
 * sesion, tambien los revocados, con su creacion, su revocacion y su ultimo uso. Con sesion.
 * Dependencias externas: la base de datos.
 * Recibe: nada (el usuario es el de la sesion). Responde: lista de UsuarioTokenResponseDTO, los mas nuevos primero.
 * Lanza NO_AUTENTICADO.
 */
@Service
@RequiredArgsConstructor
public class UsuarioMisTokensUseCase {

    private final UsuarioDeLaSesion usuarioDeLaSesion;

    private final UsoDeTokenRepository repository;

    @Transactional(readOnly = true)
    public List<UsuarioTokenResponseDTO> ejecutar() {
        return repository.tokensDe(usuarioDeLaSesion.obtener().getId());
    }
}
