package com.blue.open.Code.Autorizacion;

import com.blue.open.Code.Common.Mcp.Confirmacion;
import com.blue.open.Code.Common.Mcp.RespuestaDeHerramienta;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoDTO;
import com.blue.open.Code.Proyecto.ProyectoService;
import com.blue.open.Code.ReglaPermiso.CasosDeUso.ReglaPermisoArchivarUseCase;
import com.blue.open.Code.ReglaPermiso.EfectoDePermiso;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.ReglaPermiso.ReglaPermisoDTO;
import com.blue.open.Code.ReglaPermiso.ReglaPermisoService;
import com.blue.open.Code.ReglaPermiso.TipoDeRecurso;
import com.blue.open.Code.ReglaPermiso.TipoDeSujeto;
import com.blue.open.Code.Tarea.ReferenciasDeMcp;
import com.blue.open.Code.Usuario.CasosDeUso.UsuarioAsignarGruposUseCase;
import com.blue.open.Code.Usuario.GrupoDeUsuarioDTO;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioAsignarGruposRequestDTO;
import com.blue.open.Code.Usuario.UsuarioDTO;
import com.blue.open.Code.Usuario.UsuarioService;
import com.blue.open.Core.Identity.DTO.RolDTO;
import com.blue.open.Core.Identity.Entidad.Rol;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Herramientas MCP de administracion (T3.3 y Spec 4): administrar_proyecto, administrar_usuarios,
 * administrar_grupos y administrar_permisos. Solo para el super admin y los Administradores (la API lo declara en
 * @Contrato; por MCP lo exige cada herramienta). Asignar grupos, archivar grupos y cambiar reglas de permiso exigen
 * confirmar: true; sin eso responden la vista previa sin cambiar nada.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class HerramientasMcpDeAdministracion {

    private final ResolutorDePermisosDeRecurso permisos;

    private final ReferenciasDeMcp referencias;

    private final ProyectoService proyectoService;

    private final UsuarioService usuarioService;

    private final UsuarioAsignarGruposUseCase asignarGrupos;

    private final RolService rolService;

    private final RolArchivarUseCase archivarGrupo;

    private final GrupoRepository grupoRepository;

    private final ReglaPermisoService reglaPermisoService;

    private final ReglaPermisoArchivarUseCase archivarRegla;

    // Crea un proyecto (con sus cuatro tableros) o edita su nombre y descripcion
    @McpTool(name = "administrar_proyecto", description = "Crea un proyecto (accion crear: prefijo de 2 a 6 letras y nombre) o edita su nombre y descripción (accion editar). Solo administradores.")
    public RespuestaDeHerramienta administrarProyecto(
            @McpToolParam(description = "crear o editar", required = true) String accion,
            @McpToolParam(description = "Prefijo del proyecto (no se cambia después de crearlo)", required = true) String prefijo,
            @McpToolParam(description = "Nombre del proyecto (obligatorio al crear)", required = false) String nombre,
            @McpToolParam(description = "Descripción del proyecto", required = false) String descripcion) {
        exigirAdministrador();
        ProyectoDTO dto;
        if (accion(accion, "crear", "editar").equals("crear")) {
            dto = new ProyectoDTO();
            dto.setPrefijo(prefijo);
            dto.setNombre(nombre);
        } else {
            Proyecto proyecto = referencias.proyecto(prefijo);
            dto = proyectoService.obtenerPorUuid(proyecto.getUuid());
            if (nombre != null)
                dto.setNombre(nombre);
        }
        if (descripcion != null)
            dto.setDescripcion(descripcion);
        ProyectoDTO guardado = proyectoService.guardar(dto);
        return new RespuestaDeHerramienta("Proyecto " + guardado.getPrefijo() + " guardado", guardado);
    }

    /*
     * Crea o edita un usuario, o reemplaza sus grupos (asignar_grupos, con confirmar: true). Un Administrador no
     * administra al super admin ni a otros Administradores.
     */
    @McpTool(name = "administrar_usuarios", description = "Crea (crear) o edita (editar) una cuenta local, o reemplaza sus grupos (asignar_grupos, pide confirmar: true). Solo administradores.")
    @Transactional
    public RespuestaDeHerramienta administrarUsuarios(
            @McpToolParam(description = "crear, editar o asignar_grupos", required = true) String accion,
            @McpToolParam(description = "Nombre de usuario", required = true) String nombreDeUsuario,
            @McpToolParam(description = "Correo (crear y editar)", required = false) String email,
            @McpToolParam(description = "Contraseña (crear, o editar para cambiarla)", required = false) String contrasena,
            @McpToolParam(description = "Nombres de los grupos (asignar_grupos): la lista completa", required = false) List<String> grupos,
            @McpToolParam(description = "true para asignar los grupos; sin esto solo se muestra la vista previa", required = false) Boolean confirmar) {
        exigirAdministrador();
        String elegida = accion(accion, "crear", "editar", "asignar_grupos");
        if (elegida.equals("asignar_grupos")) {
            Usuario usuario = referencias.usuario(nombreDeUsuario);
            List<String> nombres = grupos == null ? List.of() : grupos;
            if (!Confirmacion.confirmada(confirmar))
                return Confirmacion.vistaPrevia("Se asignarían a " + nombreDeUsuario + " los grupos: "
                        + (nombres.isEmpty() ? "ninguno" : String.join(", ", nombres)));
            UsuarioAsignarGruposRequestDTO pedido = new UsuarioAsignarGruposRequestDTO();
            pedido.setGrupos(grupoRepository.findByNombreInAndStatus(nombres, 0).stream().map(Rol::getUuid).toList());
            if (pedido.getGrupos().size() != nombres.size())
                throw new ExcepcionDeApi(CodigoDeErrorKernel.FORMULARIO_INVALIDO, "Algún grupo no existe: " + nombres + ".",
                        "HerramientasMcpDeAdministracion | administrarUsuarios | grupos");
            List<GrupoDeUsuarioDTO> asignados = asignarGrupos.ejecutar(usuario.getUuid(), pedido);
            return new RespuestaDeHerramienta(nombreDeUsuario + " quedó con " + asignados.size() + " grupos", asignados);
        }
        UsuarioDTO dto = elegida.equals("crear") ? new UsuarioDTO()
                : usuarioService.obtenerPorUuid(referencias.usuario(nombreDeUsuario).getUuid());
        dto.setNombreDeUsuario(nombreDeUsuario);
        if (email != null)
            dto.setEmail(email);
        dto.setContrasena(contrasena);
        UsuarioDTO guardado = usuarioService.guardar(dto);
        return new RespuestaDeHerramienta("Usuario " + guardado.getNombreDeUsuario() + " guardado", guardado);
    }

    // Crea un grupo, o archiva uno (con confirmar: true)
    @McpTool(name = "administrar_grupos", description = "Crea un grupo (crear) o lo archiva (archivar, pide confirmar: true). Solo administradores.")
    @Transactional
    public RespuestaDeHerramienta administrarGrupos(
            @McpToolParam(description = "crear o archivar", required = true) String accion,
            @McpToolParam(description = "Nombre del grupo", required = true) String nombre,
            @McpToolParam(description = "true para archivar; sin esto solo se muestra la vista previa", required = false) Boolean confirmar) {
        exigirAdministrador();
        if (accion(accion, "crear", "archivar").equals("crear")) {
            RolDTO dto = new RolDTO();
            dto.setNombre(nombre);
            RolDTO guardado = rolService.guardar(dto);
            return new RespuestaDeHerramienta("Grupo " + guardado.getNombre() + " creado", guardado);
        }
        Rol grupo = grupoRepository.findByNombreInAndStatus(List.of(nombre), 0).stream().findFirst()
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorKernel.FORMULARIO_INVALIDO, "No existe el grupo " + nombre + ".",
                        "HerramientasMcpDeAdministracion | administrarGrupos"));
        if (!Confirmacion.confirmada(confirmar))
            return Confirmacion.vistaPrevia("Se archivaría el grupo " + nombre + "; sus integrantes pierden los permisos que les daba");
        RolDTO archivado = archivarGrupo.ejecutar(grupo.getUuid());
        return new RespuestaDeHerramienta("Grupo " + nombre + " archivado", archivado);
    }

    /*
     * Crea una regla de permiso o archiva una (por uuid). Siempre pide confirmar: true. El sujeto va como
     * "usuario:nombre" o "grupo:nombre" y el recurso como "SHI" (proyecto) o "SHI/QA" (tablero).
     */
    @McpTool(name = "administrar_permisos", description = "Crea una regla de permiso (crear: sujeto usuario:nombre o grupo:nombre, recurso SHI o SHI/QA, nivel VER/EDITAR/ARCHIVAR, efecto PERMITIR/NEGAR) o archiva una (archivar: uuid). Pide confirmar: true. Solo administradores.")
    @Transactional
    public RespuestaDeHerramienta administrarPermisos(
            @McpToolParam(description = "crear o archivar", required = true) String accion,
            @McpToolParam(description = "Sujeto: usuario:nombreDeUsuario o grupo:nombre (crear)", required = false) String sujeto,
            @McpToolParam(description = "Recurso: prefijo del proyecto (SHI) o proyecto/tablero (SHI/QA) (crear)", required = false) String recurso,
            @McpToolParam(description = "Nivel: VER, EDITAR o ARCHIVAR (crear)", required = false) String nivel,
            @McpToolParam(description = "Efecto: PERMITIR o NEGAR (crear)", required = false) String efecto,
            @McpToolParam(description = "Uuid de la regla (archivar)", required = false) String uuid,
            @McpToolParam(description = "true para aplicar el cambio; sin esto solo se muestra la vista previa", required = false) Boolean confirmar) {
        exigirAdministrador();
        if (accion(accion, "crear", "archivar").equals("archivar")) {
            if (!Confirmacion.confirmada(confirmar))
                return Confirmacion.vistaPrevia("Se archivaría la regla de permiso " + uuid);
            return new RespuestaDeHerramienta("Regla archivada", archivarRegla.ejecutar(uuid));
        }
        if (!Confirmacion.confirmada(confirmar))
            return Confirmacion.vistaPrevia("Se crearía la regla: " + sujeto + " " + efecto + " " + nivel + " en " + recurso);
        ReglaPermisoDTO dto = regla(sujeto, recurso, nivel, efecto);
        ReglaPermisoDTO guardada = reglaPermisoService.guardar(dto);
        return new RespuestaDeHerramienta("Regla creada: " + sujeto + " " + efecto + " " + nivel + " en " + recurso, guardada);
    }

    // Arma el DTO de una regla a partir de los textos de la herramienta
    private ReglaPermisoDTO regla(String sujeto, String recurso, String nivel, String efecto) {
        ReglaPermisoDTO dto = new ReglaPermisoDTO();
        String[] partesDelSujeto = sujeto == null ? new String[0] : sujeto.split(":", 2);
        if (partesDelSujeto.length != 2 || recurso == null || nivel == null || efecto == null)
            throw new ExcepcionDeApi(CodigoDeErrorKernel.FORMULARIO_INVALIDO,
                    "Para crear una regla hacen falta sujeto (usuario:nombre o grupo:nombre), recurso, nivel y efecto.",
                    "HerramientasMcpDeAdministracion | regla");
        if (partesDelSujeto[0].equalsIgnoreCase("usuario")) {
            dto.setTipoSujeto(TipoDeSujeto.USUARIO);
            dto.setUsuarioUuid(referencias.usuario(partesDelSujeto[1]).getUuid());
        } else {
            dto.setTipoSujeto(TipoDeSujeto.ROL);
            dto.setRolUuid(grupoRepository.findByNombreInAndStatus(List.of(partesDelSujeto[1]), 0).stream().findFirst()
                    .map(Rol::getUuid).orElse(null));
        }
        String[] partesDelRecurso = recurso.split("/", 2);
        Proyecto proyecto = referencias.proyecto(partesDelRecurso[0]);
        if (partesDelRecurso.length == 2) {
            dto.setTipoRecurso(TipoDeRecurso.TABLERO);
            dto.setTableroUuid(referencias.tablero(proyecto, partesDelRecurso[1]).getUuid());
        } else {
            dto.setTipoRecurso(TipoDeRecurso.PROYECTO);
            dto.setProyectoUuid(proyecto.getUuid());
        }
        dto.setNivel(NivelDePermiso.valueOf(nivel.trim().toUpperCase(Locale.ROOT)));
        dto.setEfecto(EfectoDePermiso.valueOf(efecto.trim().toUpperCase(Locale.ROOT)));
        return dto;
    }

    // Accion normalizada. Lanza FORMULARIO_INVALIDO si no es una de las validas
    private static String accion(String accion, String... validas) {
        String normalizada = accion == null ? "" : accion.trim().toLowerCase(Locale.ROOT);
        for (String valida : validas) {
            if (valida.equals(normalizada))
                return valida;
        }
        throw new ExcepcionDeApi(CodigoDeErrorKernel.FORMULARIO_INVALIDO,
                "La acción tiene que ser una de: " + String.join(", ", validas) + ".", "HerramientasMcpDeAdministracion | accion " + accion);
    }

    // La administracion es solo para el super admin y los Administradores. Lanza SIN_PERMISO
    private void exigirAdministrador() {
        if (!permisos.esAdministrador())
            throw new ExcepcionDeApi(CodigoDeErrorKernel.SIN_PERMISO, "Solo el super admin y los Administradores administran Shiro.",
                    "HerramientasMcpDeAdministracion | exigirAdministrador");
    }
}
