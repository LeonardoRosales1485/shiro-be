package com.blue.open.Code.Usuario.CasosDeUso;

import com.blue.open.Code.Usuario.AuthTokenConSha256Service;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioCrearTokenConVencimientoRequestDTO;
import com.blue.open.Code.Usuario.UsuarioCrearTokenConVencimientoResponseDTO;
import com.blue.open.Code.Usuario.UsuarioDeLaSesion;
import com.blue.open.Code.Usuario.VencimientoDeToken;
import com.blue.open.Code.Usuario.VencimientoDeTokenRepository;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Protocolo.ApiKey.AccessToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: crear un token de acceso personal eligiendo su vigencia (CU-USR-28, MJ-14 y decision D-3). Con sesion;
 * el token es del usuario de la sesion.
 * Dependencias externas: la base de datos.
 * Recibe: UsuarioCrearTokenConVencimientoRequestDTO (nombre y dias de vigencia). Responde:
 * UsuarioCrearTokenConVencimientoResponseDTO con el token en claro (unica vez) y su vencimiento.
 * Lanza NO_AUTENTICADO y TOKEN_NOMBRE_DUPLICADO.
 */
@Service
@RequiredArgsConstructor
public class UsuarioCrearTokenConVencimientoUseCase {

    private final UsuarioDeLaSesion usuarioDeLaSesion;

    private final AuthTokenConSha256Service tokens;

    private final VencimientoDeTokenRepository vencimientos;

    @Transactional
    public UsuarioCrearTokenConVencimientoResponseDTO ejecutar(UsuarioCrearTokenConVencimientoRequestDTO dto) {
        Usuario usuario = usuarioDeLaSesion.obtener();
        AccessToken token = tokens.crearAccessToken(usuario, dto.getNombre().trim(), dto.getDiasDeVigencia());
        String codigo = tokens.proyectar(usuario, token.getUuid()).getCodigo();
        VencimientoDeToken vencimiento = vencimientos.findFirstByIdAccessToken(token.getId())
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorKernel.ERROR_INTERNO,
                        "UsuarioCrearTokenConVencimientoUseCase | ejecutar | sin vencimiento: " + token.getUuid()));
        return new UsuarioCrearTokenConVencimientoResponseDTO(token.getUuid(), token.getNombre(), codigo, vencimiento.getVence());
    }
}
