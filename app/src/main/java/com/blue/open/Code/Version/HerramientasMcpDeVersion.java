package com.blue.open.Code.Version;

import com.blue.open.Code.Common.Mcp.Confirmacion;
import com.blue.open.Code.Common.Mcp.RespuestaDeHerramienta;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Tarea.ReferenciasDeMcp;
import com.blue.open.Code.Version.CasosDeUso.VersionAsignarUseCase;
import com.blue.open.Code.Version.CasosDeUso.VersionChangelogUseCase;
import com.blue.open.Code.Version.CasosDeUso.VersionPublicarUseCase;
import com.blue.open.Code.Version.CasosDeUso.VersionQuitarUseCase;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Herramienta MCP gestionar_version (T5.5 y Spec 4): crear, asignar o quitar tareas, ver el changelog y publicar
 * versiones, que se identifican por proyecto y nombre. Publicar exige confirmar: true (despues la version no
 * cambia). Usa los mismos servicios y casos de uso que la API.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class HerramientasMcpDeVersion {

    private final ReferenciasDeMcp referencias;

    private final VersionRepository versionRepository;

    private final TareaVersionRepository tareaVersionRepository;

    private final VersionService versionService;

    private final TareasDeVersion tareasDeVersion;

    private final VersionAsignarUseCase asignarUseCase;

    private final VersionQuitarUseCase quitarUseCase;

    private final VersionChangelogUseCase changelogUseCase;

    private final VersionPublicarUseCase publicarUseCase;

    // Gestiona una version del proyecto. Publicar pide confirmar: true
    @McpTool(name = "gestionar_version", description = "Gestiona versiones de un proyecto: crear, asignar o quitar tareas (claves), changelog (Markdown) o publicar (fecha aaaa-mm-dd, hoy por defecto; pide confirmar: true y después no cambia).")
    @Transactional
    public RespuestaDeHerramienta gestionarVersion(
            @McpToolParam(description = "crear, asignar, quitar, changelog o publicar", required = true) String accion,
            @McpToolParam(description = "Prefijo del proyecto", required = true) String proyecto,
            @McpToolParam(description = "Nombre de la versión, por ejemplo 1.4.0", required = true) String version,
            @McpToolParam(description = "Claves de las tareas (asignar y quitar)", required = false) List<String> tareas,
            @McpToolParam(description = "Fecha de publicación aaaa-mm-dd (publicar)", required = false) String fecha,
            @McpToolParam(description = "true para publicar; sin esto solo se muestra la vista previa", required = false) Boolean confirmar) {
        Proyecto elegido = referencias.proyecto(proyecto);
        String normalizada = accion == null ? "" : accion.trim().toLowerCase(Locale.ROOT);
        if (normalizada.equals("crear")) {
            VersionDTO dto = new VersionDTO();
            dto.setProyectoUuid(elegido.getUuid());
            dto.setNombre(version);
            VersionDTO creada = versionService.guardar(dto);
            return new RespuestaDeHerramienta("Versión " + creada.getNombre() + " creada", creada);
        }
        Version elegida = versionRepository.findFirstByProyectoAndNombreAndStatus(elegido, version, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "No existe la versión " + version + " en " + elegido.getPrefijo() + ".", "HerramientasMcpDeVersion | version"));
        return switch (normalizada) {
            case "asignar" -> new RespuestaDeHerramienta("Tareas asignadas a " + version, asignarUseCase.ejecutar(elegida.getUuid(), pedido(tareas)));
            case "quitar" -> new RespuestaDeHerramienta("Tareas quitadas de " + version, quitarUseCase.ejecutar(elegida.getUuid(), pedido(tareas)));
            case "changelog" -> new RespuestaDeHerramienta("Changelog de " + version, changelogUseCase.ejecutar(elegida.getUuid()));
            case "publicar" -> publicar(elegida, fecha, confirmar);
            default -> throw new ExcepcionDeApi(CodigoDeErrorKernel.FORMULARIO_INVALIDO,
                    "La acción tiene que ser crear, asignar, quitar, changelog o publicar.", "HerramientasMcpDeVersion | accion " + accion);
        };
    }

    // Publica la version o, sin confirmar, responde cuantas tareas terminadas va a tener su changelog
    private RespuestaDeHerramienta publicar(Version version, String fecha, Boolean confirmar) {
        VersionPublicarRequestDTO pedido = new VersionPublicarRequestDTO();
        pedido.setFecha(fecha == null ? null : LocalDate.parse(fecha));
        if (!Confirmacion.confirmada(confirmar)) {
            tareasDeVersion.sinPublicar(version.getUuid());
            return Confirmacion.vistaPrevia("Se publicaría la versión " + version.getNombre() + " con "
                    + tareaVersionRepository.lineasDeChangelog(version).size() + " tareas terminadas en el changelog"
                    + (fecha == null ? ", con fecha de hoy" : ", con fecha " + fecha) + "; después no cambia");
        }
        return new RespuestaDeHerramienta("Versión " + version.getNombre() + " publicada", publicarUseCase.ejecutar(version.getUuid(), pedido));
    }

    // Pedido de tareas a partir de sus claves
    private VersionTareasRequestDTO pedido(List<String> claves) {
        VersionTareasRequestDTO pedido = new VersionTareasRequestDTO();
        pedido.setTareas(referencias.uuidsDeTareas(claves));
        return pedido;
    }
}
