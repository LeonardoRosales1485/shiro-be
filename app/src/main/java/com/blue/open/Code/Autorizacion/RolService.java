package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Identity.DTO.RolDTO;
import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Identity.Entidad.Permiso;
import com.blue.open.Core.Identity.Entidad.Rol;
import com.blue.open.Core.Identity.Service.RolBaseService;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service de roles del proyecto sobre RolBaseService.
 * Dependencias externas: la base de datos.
 * Recibe: RolDTO o uuid segun el endpoint. Responde: RolDTO.
 */
@Service
public class RolService extends RolBaseService<RolDTO> {

    // Proyeccion del rol vigente por uuid. Un rol eliminado responde como inexistente
    @Override
    protected Optional<RolDTO> hookObtenerBuscarEnBase(String uuid) {
        return super.hookObtenerBuscarEnBase(uuid).filter(rol -> rol.getStatus() == 0);
    }

    /*
     * Aplica el DTO al rol resolviendo los permisos en la base antes de aplicarlo. En una edicion el rol ya esta
     * gestionado por Hibernate: los permisos se buscan antes de modificarlo para que el flush automatico de esas
     * consultas no escriba un rol a medio aplicar.
     */
    @Override
    protected Rol hookGuardarAplicarDto(RolDTO dto, Rol modelo) {
        List<Permiso> permisos = new ArrayList<>();
        if (dto.getPermisos() != null) {
            for (var permisoDto : dto.getPermisos()) {
                if (permisoDto.getUuid() != null) {
                    permisoRepository.buscarPorUuid(permisoDto.getUuid()).ifPresent(permisos::add);
                }
            }
        }
        modelo = proyector.aplicar(dto, modelo, CodigoDeErrorCrud.RELACION_NO_ENCONTRADA);
        if (dto.getPermisos() != null) {
            modelo.setPermisos(permisos);
        }
        return modelo;
    }

    /*
     * Define que roles ve un usuario en el paginado de roles: los de RolEnum son visibles para
     * cualquier usuario con ROL_VER. No habilita a asignarlos: al guardar un usuario el Core
     * sigue exigiendo que el solicitante posea todos los permisos del rol.
     */
    @Override
    public boolean puedeAsignarRol(RolDTO rol, UsuarioBase usuarioActual, Set<String> permisosDelSolicitante) {
        // Si el rol esta en RolEnum del proyecto, se permite la visibilidad/asignacion
        for (RolEnum rolEnum : RolEnum.values()) {
            if (rolEnum.name().equals(rol.getNombre())) {
                return true;
            }
        }

        // De lo contrario, aplicar la regla general del Core (debe tener todos los permisos)
        return super.puedeAsignarRol(rol, usuarioActual, permisosDelSolicitante);
    }
}
