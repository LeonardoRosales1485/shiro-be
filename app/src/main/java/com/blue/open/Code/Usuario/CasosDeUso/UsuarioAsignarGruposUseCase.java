package com.blue.open.Code.Usuario.CasosDeUso;

import com.blue.open.Code.Autorizacion.GrupoRepository;
import com.blue.open.Code.Autorizacion.ProteccionDeAdministradores;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Usuario.GrupoDeUsuarioDTO;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioAsignarGruposRequestDTO;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Identity.Entidad.Rol;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import com.blue.open.Core.Monoempresa.Entidad.UsuarioRol;
import com.blue.open.Core.Monoempresa.Repository.UsuarioRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Caso de uso: reemplaza los grupos (roles de core-monoempresa) de un usuario vigente (T2.5). El Core 4.0.0 no trae
 * este endpoint en monoempresa. Los Administradores no cambian los grupos del super admin ni de otros
 * Administradores, ni asignan o quitan los grupos del sistema (SYSADMIN, ADMIN). Los grupos quitados quedan
 * archivados en auth_usuarios_roles. Deja una fila EDITAR (campo grupos) en el historial del usuario.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del usuario y la lista completa de grupos. Responde: los grupos del usuario. Lanza
 * REGISTRO_NO_ENCONTRADO, RELACION_NO_ENCONTRADA o SIN_PERMISO.
 */
@Service
@RequiredArgsConstructor
public class UsuarioAsignarGruposUseCase {

    private final UsuarioRepository usuarioRepository;

    private final UsuarioRolRepository usuarioRolRepository;

    private final GrupoRepository grupoRepository;

    private final ProteccionDeAdministradores proteccion;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final RegistradorDeHistorial historial;

    @Transactional
    public List<GrupoDeUsuarioDTO> ejecutar(String uuid, UsuarioAsignarGruposRequestDTO dto) {
        Usuario usuario = usuarioRepository.findFirstByUuidAndStatus(uuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "UsuarioAsignarGruposUseCase | ejecutar | usuario vigente " + uuid));
        proteccion.exigirQuePuedaAdministrar(usuario);
        Set<String> pedidos = new HashSet<>(dto.getGrupos());
        List<Rol> nuevos = grupoRepository.findByUuidInAndStatus(pedidos, 0);
        if (nuevos.size() != pedidos.size())
            throw new ExcepcionDeApi(CodigoDeErrorCrud.RELACION_NO_ENCONTRADA,
                    "UsuarioAsignarGruposUseCase | ejecutar | algun grupo no existe").conDato("campo", "grupos");
        List<UsuarioRol> actuales = grupoRepository.asignacionesVigentesConGrupo(usuario.getId());
        String anteriores = nombres(actuales.stream().map(UsuarioRol::getRol).toList());
        Long actual = usuarioActual.getIdUsuarioActual();
        Set<String> conservados = new HashSet<>();
        for (UsuarioRol asignacion : actuales) {
            if (pedidos.contains(asignacion.getRol().getUuid())) {
                conservados.add(asignacion.getRol().getUuid());
                continue;
            }
            proteccion.exigirQuePuedaManejar(asignacion.getRol());
            asignacion.setStatus(-1);
            asignacion.setFechaDeBorrado(new Timestamp(System.currentTimeMillis()));
            asignacion.setIdUsuarioDeBorrado(actual);
            usuarioRolRepository.save(asignacion);
        }
        for (Rol grupo : nuevos) {
            if (conservados.contains(grupo.getUuid()))
                continue;
            proteccion.exigirQuePuedaManejar(grupo);
            UsuarioRol asignacion = new UsuarioRol();
            asignacion.setIdUsuario(usuario.getId());
            asignacion.setRol(grupo);
            asignacion.setIdUsuarioDeCreacion(actual);
            usuarioRolRepository.save(asignacion);
        }
        String actualesNombres = nombres(nuevos);
        if (!anteriores.equals(actualesNombres))
            historial.registrar(Historial.USUARIO, uuid, AccionDeHistorial.EDITAR, "grupos", anteriores, actualesNombres);
        return usuarioRepository.gruposDe(usuario.getId());
    }

    // Nombres de los grupos, ordenados y separados por coma (vacio si no hay)
    private static String nombres(List<Rol> grupos) {
        return grupos.stream().map(Rol::getNombre).sorted().collect(Collectors.joining(", "));
    }
}
