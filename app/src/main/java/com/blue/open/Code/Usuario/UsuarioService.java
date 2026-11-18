package com.blue.open.Code.Usuario;

import com.blue.open.Code.Autorizacion.RolEnum;
import com.blue.open.Code.Common.Configuracion.UrlsDelFrontProperties;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Empresa.EmpresaDTO;
import com.blue.open.Code.Empresa.ValidadorDeCuitService;
import com.blue.open.Code.Usuario.Notificaciones.RecuperarContrasenaMail;
import com.blue.open.Code.Usuario.Notificaciones.RegistroMail;
import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Identity.Entidad.Rol;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Notificacion.NotificacionPorMail;
import com.blue.open.Core.Multiempresa.DTO.EmpresaBaseDTO;
import com.blue.open.Core.Multiempresa.Service.UsuarioBaseMultiempresaService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service de usuarios del proyecto sobre UsuarioBaseMultiempresaService: agrega las validaciones del
 * registro publico (email y CUIT) y las plantillas de email propias.
 * Dependencias externas: la base de datos y core-mail.
 * Recibe: UsuarioDTO o uuid segun el endpoint. Responde: UsuarioDTO.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService extends UsuarioBaseMultiempresaService<Usuario, UsuarioDTO> {

    private final UrlsDelFrontProperties urlsDelFront;

    private final ValidadorDeCuitService validadorDeCuitService;

    private final UsuarioRepository usuarioRepository;

    /*
     * Hook para permitir la asignacion de roles
     *
     * Los roles de RolEnum viven en la empresa de sistema y el Core por defecto no deja asignar
     * roles de sistema. Este hook los habilita; el Core sigue validando que el solicitante posea
     * todos los permisos del rol y que la relacion sea en su propia empresa.
     *
     * Si no se desea que esto pase se debe eliminar este metodo
     */
    @Override
    protected boolean hookPuedeAsignarRol(Rol rol, UsuarioBase usuario, String uuidDeEmpresa) {
        for (RolEnum rolEnum : RolEnum.values()) {
            if (rolEnum.name().equals(rol.getNombre())) {
                return true;
            }
        }

        return super.hookPuedeAsignarRol(rol, usuario, uuidDeEmpresa);
    }

    // Registro publico: rechaza con 400 un email ya usado por un usuario activo o pendiente, y un CUIT invalido
    @Override
    protected void hookRegistroPublicoAntesDeCrearEmpresa(UsuarioDTO usuarioDTO, EmpresaBaseDTO empresaDTO) {
        // 1. Validar que el email no este en uso por un usuario activo (0) o pendiente (-2)
        if (usuarioRepository.findFirstByEmailAndStatusNot(usuarioDTO.getEmail(), -1).isPresent()) {
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.EMAIL_YA_REGISTRADO,
                    "UsuarioService | hookRegistroPublicoAntesDeCrearEmpresa | email en uso por un usuario activo o pendiente");
        }

        // 2. Validar algoritmo del CUIT de la empresa
        if (empresaDTO instanceof EmpresaDTO empresaConCuit && empresaConCuit.getCuit() != null
                && !validadorDeCuitService.esValido(empresaConCuit.getCuit())) {
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CUIT_INVALIDO,
                    "UsuarioService | hookRegistroPublicoAntesDeCrearEmpresa | digito verificador invalido: "
                            + empresaConCuit.getCuit());
        }
    }

    // Plantillas de email propias del proyecto para recuperacion de contrasena y registro
    @Override
    protected NotificacionPorMail hookObtenerNotificacionPorMail(String proposito, UsuarioBase usuario, String codigo) {
        String urlDelFront = urlsDelFront.getUrlDeMail();
        if (PROPOSITO_RECUPERAR_CONTRASENA.equals(proposito)) {
            return new RecuperarContrasenaMail(usuario, codigo, urlDelFront);
        }
        if (PROPOSITO_REGISTRO.equals(proposito)) {
            return new RegistroMail(usuario, codigo, urlDelFront);
        }
        return super.hookObtenerNotificacionPorMail(proposito, usuario, codigo);
    }
}
