package com.blue.open.Code.Usuario.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Code.Usuario.UsuarioSesionGruposResponseDTO;
import com.blue.open.Core.Identity.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: grupos del usuario de la sesion y si es administrador (CU-USR-24, resolucion A12 del 19/11). Solo
 * exige sesion: cada usuario puede ver sus propios grupos.
 * Dependencias externas: la base de datos.
 * Recibe: nada (el usuario sale de la sesion). Responde: UsuarioSesionGruposResponseDTO. Lanza NO_AUTENTICADO sin
 * sesion (lo resuelve AuthService).
 */
@Service
@RequiredArgsConstructor
public class UsuarioSesionGruposUseCase {

    private final AuthService authService;

    private final UsuarioRepository usuarioRepository;

    private final ResolutorDePermisosDeRecurso permisos;

    @Transactional(readOnly = true)
    public UsuarioSesionGruposResponseDTO ejecutar() {
        Long idUsuario = authService.autenticar().getId();
        return new UsuarioSesionGruposResponseDTO(usuarioRepository.gruposDe(idUsuario), permisos.esAdministrador());
    }
}
