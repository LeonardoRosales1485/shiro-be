package com.blue.open.Code.Usuario;

import com.blue.open.Code.Autorizacion.ProteccionDeAdministradores;
import com.blue.open.Core.Identity.Service.UsuarioBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service de usuarios de Shiro sobre UsuarioBaseService (core-identity). Crear y editar usuarios lo pueden el super
 * admin y los Administradores (lo declara @Contrato); los Administradores no editan al super admin ni a otros
 * Administradores (T2.5, seccion 15 del DAF).
 * Dependencias externas: la base de datos.
 * Recibe: UsuarioDTO o uuid segun el endpoint. Responde: UsuarioDTO.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService extends UsuarioBaseService<Usuario, UsuarioDTO> {

    private final UsuarioRepository usuarioRepository;

    private final ProteccionDeAdministradores proteccion;

    // Reemplaza el control del Core (permisos USUARIO_CREAR y USUARIO_EDITAR): quien llega ya es super admin o
    // Administrador por @Contrato. Al editar, exige poder administrar al usuario. Lanza SIN_PERMISO
    @Override
    public void hookGuardarAuth(UsuarioDTO dto) {
        if (dto.getUuid() != null)
            usuarioRepository.findFirstByUuid(dto.getUuid()).ifPresent(proteccion::exigirQuePuedaAdministrar);
    }
}
