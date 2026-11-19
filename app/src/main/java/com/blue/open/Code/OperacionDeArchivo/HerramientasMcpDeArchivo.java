package com.blue.open.Code.OperacionDeArchivo;

import com.blue.open.Code.Autorizacion.ProteccionDeAdministradores;
import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Common.Mcp.Confirmacion;
import com.blue.open.Code.Common.Mcp.RespuestaDeHerramienta;
import com.blue.open.Code.OperacionDeArchivo.CasosDeUso.OperacionDeArchivoBuscarUseCase;
import com.blue.open.Code.Proyecto.CasosDeUso.ProyectoArchivarUseCase;
import com.blue.open.Code.Proyecto.CasosDeUso.ProyectoRestaurarUseCase;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoDTO;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Tablero.TableroRepository;
import com.blue.open.Code.Tarea.CasosDeUso.TareaArchivarUseCase;
import com.blue.open.Code.Tarea.CasosDeUso.TareaRestaurarUseCase;
import com.blue.open.Code.Tarea.ReferenciasDeMcp;
import com.blue.open.Code.Tarea.TareaDTO;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Code.Usuario.CasosDeUso.UsuarioArchivarUseCase;
import com.blue.open.Code.Usuario.CasosDeUso.UsuarioRestaurarUseCase;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioDTO;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.DTO.Pagina;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Protocolo.ApiKey.AccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Herramientas MCP de archivo (T3.3 y Spec 4): archivar y restaurar tareas, proyectos y usuarios. Archivar un
 * proyecto o un usuario exige confirmar: true; sin eso responde la vista previa sin cambiar nada. Usan los mismos
 * casos de uso que la API, con sus permisos (ARCHIVAR en el recurso; usuarios, solo administradores).
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class HerramientasMcpDeArchivo {

    private final ReferenciasDeMcp referencias;

    private final ProyectoRepository proyectoRepository;

    private final TableroRepository tableroRepository;

    private final TareaRepository tareaRepository;

    private final UsuarioRepository usuarioRepository;

    private final AccessTokenRepository accessTokenRepository;

    private final ResolutorDePermisosDeRecurso permisos;

    private final ProteccionDeAdministradores proteccion;

    private final TareaArchivarUseCase archivarTarea;

    private final TareaRestaurarUseCase restaurarTarea;

    private final ProyectoArchivarUseCase archivarProyecto;

    private final ProyectoRestaurarUseCase restaurarProyecto;

    private final UsuarioArchivarUseCase archivarUsuario;

    private final UsuarioRestaurarUseCase restaurarUsuario;

    private final OperacionDeArchivoBuscarUseCase buscarUseCase;

    // Busca lo archivado sobre lo que el usuario tiene permiso de archivo
    @McpTool(name = "buscar_archivados", description = "Busca lo archivado (TAREA, PROYECTO, SPRINT, VERSION o USUARIO) sobre lo que tenés permiso de archivo, por tipo y texto. 50 por página.")
    public RespuestaDeHerramienta buscarArchivados(
            @McpToolParam(description = "Tipo: TAREA, PROYECTO, SPRINT, VERSION o USUARIO. Vacío para todos", required = false) String tipo,
            @McpToolParam(description = "Texto a buscar en el nombre", required = false) String texto,
            @McpToolParam(description = "Número de página, desde 1. Por defecto 1", required = false) Integer pagina) {
        OperacionDeArchivoBuscarRequestDTO pedido = new OperacionDeArchivoBuscarRequestDTO();
        pedido.setTipo(tipo);
        pedido.setTexto(texto);
        pedido.setPagina(pagina == null ? 1 : pagina);
        Pagina<OperacionDeArchivoBuscarResponseDTO> archivados = buscarUseCase.ejecutar(pedido);
        return new RespuestaDeHerramienta(archivados.getTotalDeElementos() + " archivados", archivados);
    }

    /*
     * Archiva una tarea (por clave, con sus hijas), un proyecto (por prefijo, con todo lo que contiene) o un usuario
     * (por nombre de usuario). Proyecto y usuario exigen confirmar: true; sin eso responde que se archivaria.
     */
    @McpTool(name = "archivar", description = "Archiva una TAREA (por clave, con sus subtareas), un PROYECTO (por prefijo, con todo lo que contiene) o un USUARIO (por nombre de usuario). Proyecto y usuario piden confirmar: true; sin eso devuelve qué se archivaría.")
    @Transactional
    public RespuestaDeHerramienta archivar(
            @McpToolParam(description = "Qué se archiva: TAREA, PROYECTO o USUARIO", required = true) String tipo,
            @McpToolParam(description = "Clave de la tarea, prefijo del proyecto o nombre de usuario", required = true) String identificador,
            @McpToolParam(description = "true para archivar un proyecto o un usuario; sin esto solo se muestra la vista previa", required = false) Boolean confirmar) {
        switch (tipo(tipo)) {
            case "TAREA" -> {
                TareaDTO tarea = archivarTarea.ejecutar(referencias.tarea(identificador).getUuid());
                return new RespuestaDeHerramienta("Archivada " + tarea.getClave() + " con sus subtareas", tarea);
            }
            case "PROYECTO" -> {
                Proyecto proyecto = referencias.proyecto(identificador);
                permisos.exigir(NivelDePermiso.ARCHIVAR, proyecto);
                if (!Confirmacion.confirmada(confirmar))
                    return Confirmacion.vistaPrevia("Se archivarían 1 proyecto, "
                            + tableroRepository.countByProyectoAndStatus(proyecto, 0) + " tableros y "
                            + tareaRepository.countByProyectoAndStatus(proyecto, 0) + " tareas");
                ProyectoDTO archivado = archivarProyecto.ejecutar(proyecto.getUuid());
                return new RespuestaDeHerramienta("Archivado el proyecto " + archivado.getPrefijo(), archivado);
            }
            default -> {
                Usuario usuario = referencias.usuario(identificador);
                exigirAdministrador();
                proteccion.exigirQuePuedaAdministrar(usuario);
                if (!Confirmacion.confirmada(confirmar))
                    return Confirmacion.vistaPrevia("Se archivaría el usuario " + usuario.getNombreDeUsuario()
                            + " y se desactivarían " + accessTokenRepository.findAllByIdUsuarioAndStatus(usuario.getId(), 0).size()
                            + " tokens de acceso; sus tareas conservan el responsable");
                UsuarioDTO archivado = archivarUsuario.ejecutar(usuario.getUuid());
                return new RespuestaDeHerramienta("Archivado el usuario " + archivado.getNombreDeUsuario(), archivado);
            }
        }
    }

    // Restaura una tarea (por clave), un proyecto (por prefijo) o un usuario (por nombre de usuario) archivados
    @McpTool(name = "restaurar", description = "Restaura una TAREA (por clave), un PROYECTO (por prefijo) o un USUARIO (por nombre de usuario) archivados, con lo que se archivó junto con ellos.")
    @Transactional
    public RespuestaDeHerramienta restaurar(
            @McpToolParam(description = "Qué se restaura: TAREA, PROYECTO o USUARIO", required = true) String tipo,
            @McpToolParam(description = "Clave de la tarea, prefijo del proyecto o nombre de usuario", required = true) String identificador) {
        switch (tipo(tipo)) {
            case "TAREA" -> {
                TareaDTO tarea = restaurarTarea.ejecutar(referencias.tarea(identificador).getUuid());
                return new RespuestaDeHerramienta("Restaurada " + tarea.getClave(), tarea);
            }
            case "PROYECTO" -> {
                String prefijo = identificador.trim().toUpperCase(Locale.ROOT);
                Proyecto proyecto = proyectoRepository.findFirstByPrefijoAndStatus(prefijo, -1)
                        .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                                "No hay un proyecto archivado con el prefijo " + prefijo + ".", "HerramientasMcpDeArchivo | restaurar"));
                ProyectoDTO restaurado = restaurarProyecto.ejecutar(proyecto.getUuid());
                return new RespuestaDeHerramienta("Restaurado el proyecto " + restaurado.getPrefijo(), restaurado);
            }
            default -> {
                exigirAdministrador();
                Usuario usuario = usuarioRepository.findFirstByNombreDeUsuarioAndStatus(identificador, -1)
                        .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                                "No hay un usuario archivado " + identificador + ".", "HerramientasMcpDeArchivo | restaurar"));
                UsuarioDTO restaurado = restaurarUsuario.ejecutar(usuario.getUuid());
                return new RespuestaDeHerramienta("Restaurado el usuario " + restaurado.getNombreDeUsuario(), restaurado);
            }
        }
    }

    // Tipo normalizado. Lanza FORMULARIO_INVALIDO si no es TAREA, PROYECTO ni USUARIO
    private static String tipo(String tipo) {
        String normalizado = tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
        if (!normalizado.equals("TAREA") && !normalizado.equals("PROYECTO") && !normalizado.equals("USUARIO"))
            throw new ExcepcionDeApi(CodigoDeErrorKernel.FORMULARIO_INVALIDO, "El tipo tiene que ser TAREA, PROYECTO o USUARIO.",
                    "HerramientasMcpDeArchivo | tipo " + tipo);
        return normalizado;
    }

    // Los usuarios los archivan y restauran el super admin y los Administradores. Lanza SIN_PERMISO
    private void exigirAdministrador() {
        if (!permisos.esAdministrador())
            throw new ExcepcionDeApi(CodigoDeErrorKernel.SIN_PERMISO, "Solo el super admin y los Administradores administran usuarios.",
                    "HerramientasMcpDeArchivo | usuario");
    }
}
