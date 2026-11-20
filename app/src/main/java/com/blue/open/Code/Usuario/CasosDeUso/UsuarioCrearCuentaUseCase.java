package com.blue.open.Code.Usuario.CasosDeUso;

import com.blue.open.Code.Usuario.UsuarioAsignarGruposRequestDTO;
import com.blue.open.Code.Usuario.UsuarioCrearCuentaRequestDTO;
import com.blue.open.Code.Usuario.UsuarioCrearCuentaResponseDTO;
import com.blue.open.Code.Usuario.UsuarioDTO;
import com.blue.open.Code.Usuario.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Caso de uso: crear una cuenta de Shiro (CU-USR-27, propuesta 56 resuelta el 20/11), para quien no tiene cuenta de
 * Google de la empresa. Genera una contrasena inicial al azar, crea el usuario con el alta del ABM (que la guarda
 * hasheada) y le asigna los grupos con las mismas reglas que asignar grupos. Todo o nada.
 * Dependencias externas: la base de datos.
 * Recibe: UsuarioCrearCuentaRequestDTO. Responde: UsuarioCrearCuentaResponseDTO con la contrasena, una sola vez.
 * Lanza los errores de validacion del alta y SIN_PERMISO o RELACION_NO_ENCONTRADA de asignar grupos.
 */
@Service
@RequiredArgsConstructor
public class UsuarioCrearCuentaUseCase {

    // Caracteres de la contrasena, sin los que se confunden (l, 1, I, O, 0)
    private static final String CARACTERES = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final int LARGO = 16;

    private static final SecureRandom AZAR = new SecureRandom();

    private final UsuarioService usuarioService;

    private final UsuarioAsignarGruposUseCase asignarGrupos;

    @Transactional
    public UsuarioCrearCuentaResponseDTO ejecutar(UsuarioCrearCuentaRequestDTO dto) {
        String contrasena = contrasena();
        UsuarioDTO alta = new UsuarioDTO();
        alta.setNombre(dto.getNombre().trim());
        alta.setNombreDeUsuario(dto.getNombreDeUsuario().trim());
        alta.setEmail(dto.getEmail().trim());
        alta.setContrasena(contrasena);
        UsuarioDTO creado = usuarioService.guardar(alta);
        UsuarioAsignarGruposRequestDTO grupos = new UsuarioAsignarGruposRequestDTO();
        grupos.setGrupos(dto.getGrupos());
        asignarGrupos.ejecutar(creado.getUuid(), grupos);
        UsuarioCrearCuentaResponseDTO respuesta = new UsuarioCrearCuentaResponseDTO();
        respuesta.setUuid(creado.getUuid());
        respuesta.setNombre(creado.getNombre());
        respuesta.setNombreDeUsuario(creado.getNombreDeUsuario());
        respuesta.setEmail(creado.getEmail());
        respuesta.setContrasena(contrasena);
        return respuesta;
    }

    // Contrasena inicial al azar de 16 caracteres, en bloques de 4 separados por guiones
    private static String contrasena() {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < LARGO; i++) {
            if (i > 0 && i % 4 == 0)
                texto.append('-');
            texto.append(CARACTERES.charAt(AZAR.nextInt(CARACTERES.length())));
        }
        return texto.toString();
    }
}
