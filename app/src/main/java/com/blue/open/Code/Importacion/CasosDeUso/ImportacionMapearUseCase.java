package com.blue.open.Code.Importacion.CasosDeUso;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Estado.Estado;
import com.blue.open.Code.Estado.EstadoRepository;
import com.blue.open.Code.Importacion.CatalogoDeJira;
import com.blue.open.Code.Importacion.CatalogosDesdeJira;
import com.blue.open.Code.Importacion.ElementoDeJiraDTO;
import com.blue.open.Code.Importacion.EstadoDeImportacion;
import com.blue.open.Code.Importacion.Importacion;
import com.blue.open.Code.Importacion.ImportacionCatalogoDTO;
import com.blue.open.Code.Importacion.ImportacionDTO;
import com.blue.open.Code.Importacion.ImportacionMapeoRequestDTO;
import com.blue.open.Code.Importacion.ImportacionRepository;
import com.blue.open.Code.Importacion.MapeoDeEstadoDTO;
import com.blue.open.Code.Importacion.MapeoDeImportacion;
import com.blue.open.Code.Importacion.MapeoDeTableroDTO;
import com.blue.open.Code.Importacion.MapeoDeTipoDTO;
import com.blue.open.Code.TipoTablero.TipoTablero;
import com.blue.open.Code.TipoTablero.TipoTableroRepository;
import com.blue.open.Code.TipoTarea.TipoTareaRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Caso de uso: guarda el mapeo de una importacion (CU-IMP-03, T4.2 y Spec 6) y la deja MAPEADA. Valida contra el
 * catalogo de Jira (tableros, estados y tipos del proyecto) y contra Shiro: cada tablero va a un tipo de tablero
 * vigente, cada estado elegido es de ese tipo de tablero y cada tipo de Jira va a un tipo de tarea vigente. Un
 * tablero no se mapea dos veces. Un estado o tipo sin equivalente se puede crear en el catalogo (resolucion B32) y
 * el mapeo fija cuantos dias duran los sprints sin fechas (B35). Reemplaza el mapeo anterior. No es transaccional mientras espera a Jira.
 * Dependencias externas: Jira (CatalogoDeJira) y la base de datos.
 * Recibe: uuid de la importacion e ImportacionMapeoRequestDTO. Responde: ImportacionDTO. Lanza REGISTRO_NO_ENCONTRADO,
 * IMPORTACION_EN_CURSO, MAPEO_DE_JIRA_INVALIDO y los errores de Jira.
 */
@Service
@RequiredArgsConstructor
public class ImportacionMapearUseCase {

    private final ImportacionRepository importacionRepository;

    private final CatalogoDeJira catalogo;

    private final TipoTableroRepository tipoTableroRepository;

    private final EstadoRepository estadoRepository;

    private final TipoTareaRepository tipoTareaRepository;

    private final Proyector proyector;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final CatalogosDesdeJira catalogosDesdeJira;

    private final ObjectMapper lector = new ObjectMapper();

    public ImportacionDTO ejecutar(String uuid, ImportacionMapeoRequestDTO dto) {
        Importacion importacion = importacionRepository.findFirstByUuidAndStatus(uuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "ImportacionMapearUseCase | " + uuid));
        if (importacion.getEstado() == EstadoDeImportacion.EN_CURSO)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.IMPORTACION_EN_CURSO, "ImportacionMapearUseCase | " + uuid);
        ImportacionCatalogoDTO deJira = catalogo.catalogo(importacion, catalogo.credenciales(importacion));
        MapeoDeImportacion mapeo = validar(dto, deJira);
        importacion.setMapeo(lector.writeValueAsString(mapeo));
        importacion.setEstado(EstadoDeImportacion.MAPEADA);
        importacion.setIdUsuarioDeActualizacion(usuarioActual.getIdUsuarioActual());
        importacionRepository.save(importacion);
        return proyector.porUuid(Importacion.class, uuid, ImportacionDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "ImportacionMapearUseCase | " + uuid));
    }

    /*
     * Valida el mapeo contra el catalogo de Jira y los catalogos de Shiro y lo arma; crea los estados y tipos que se
     * piden crear. Cada estado y tipo se mapea a uno existente o se crea, una sola de las dos. Lanza
     * MAPEO_DE_JIRA_INVALIDO.
     */
    private MapeoDeImportacion validar(ImportacionMapeoRequestDTO dto, ImportacionCatalogoDTO deJira) {
        Map<String, ElementoDeJiraDTO> estadosDeJira = porId(deJira.getEstados());
        Map<String, ElementoDeJiraDTO> tiposDeJira = porId(deJira.getTipos());
        Set<String> tablerosDeJira = porId(deJira.getTableros()).keySet();
        Long idUsuario = usuarioActual.getIdUsuarioActual();
        LinkedHashMap<String, String> tableros = new LinkedHashMap<>();
        Map<String, Map<String, String>> estados = new HashMap<>();
        for (MapeoDeTableroDTO tablero : dto.getTableros()) {
            exigir(tablerosDeJira.contains(tablero.getIdJira()) && !tableros.containsKey(tablero.getIdJira()),
                    "El tablero " + tablero.getIdJira() + " no es del proyecto de Jira o está repetido.");
            TipoTablero tipo = tipoTableroRepository.buscarVigentePorUuid(tablero.getTipoTableroUuid())
                    .orElseThrow(() -> invalido("El tablero de Shiro elegido no existe."));
            Map<String, String> deTablero = new HashMap<>();
            for (MapeoDeEstadoDTO estado : tablero.getEstados()) {
                ElementoDeJiraDTO enJira = estadosDeJira.get(estado.getIdJira());
                exigir(enJira != null, "El estado " + estado.getIdJira() + " no es del proyecto de Jira.");
                exigir(estado.isCrear() != (estado.getEstadoUuid() != null),
                        "El estado " + enJira.getNombre() + " tiene que mapearse a uno de Shiro o crearse (una sola de las dos).");
                deTablero.put(estado.getIdJira(), estado.isCrear() ? catalogosDesdeJira.estado(tipo, enJira, idUsuario) : estadoExistente(estado, tipo));
            }
            tableros.put(tablero.getIdJira(), tablero.getTipoTableroUuid());
            estados.put(tablero.getIdJira(), deTablero);
        }
        Map<String, String> tipos = new HashMap<>();
        for (MapeoDeTipoDTO tipo : dto.getTipos()) {
            ElementoDeJiraDTO enJira = tiposDeJira.get(tipo.getIdJira());
            exigir(enJira != null, "El tipo " + tipo.getIdJira() + " no es del proyecto de Jira.");
            exigir(tipo.isCrear() != (tipo.getTipoTareaUuid() != null),
                    "El tipo " + enJira.getNombre() + " tiene que mapearse a uno de Shiro o crearse (una sola de las dos).");
            if (!tipo.isCrear())
                exigir(tipoTareaRepository.buscarVigentePorUuid(tipo.getTipoTareaUuid()).isPresent(), "Un tipo de Shiro elegido no existe.");
            tipos.put(tipo.getIdJira(), tipo.isCrear() ? catalogosDesdeJira.tipo(enJira, idUsuario) : tipo.getTipoTareaUuid());
        }
        return new MapeoDeImportacion(tableros, estados, tipos, dto.getDuracionDeSprintEnDias());
    }

    // Uuid del estado de Shiro elegido, validando que exista y sea del tipo de tablero. Lanza MAPEO_DE_JIRA_INVALIDO
    private String estadoExistente(MapeoDeEstadoDTO estado, TipoTablero tipo) {
        Estado enShiro = estadoRepository.findFirstByUuidAndStatus(estado.getEstadoUuid(), 0)
                .orElseThrow(() -> invalido("Un estado de Shiro elegido no existe."));
        exigir(Objects.equals(enShiro.getTipoTablero().getId(), tipo.getId()),
                "El estado " + enShiro.getNombre() + " no es del tablero " + tipo.getNombre() + ".");
        return enShiro.getUuid();
    }

    // Elementos del catalogo de Jira por id
    private static Map<String, ElementoDeJiraDTO> porId(List<ElementoDeJiraDTO> elementos) {
        return elementos.stream().collect(Collectors.toMap(ElementoDeJiraDTO::getIdJira, elemento -> elemento, (a, b) -> a));
    }

    // Lanza MAPEO_DE_JIRA_INVALIDO con el motivo si no se cumple la condicion
    private static void exigir(boolean condicion, String motivo) {
        if (!condicion)
            throw invalido(motivo);
    }

    // Error de mapeo con el motivo para el administrador
    private static ExcepcionDeApi invalido(String motivo) {
        return new ExcepcionDeApi(CodigoDeErrorDelProyecto.MAPEO_DE_JIRA_INVALIDO, motivo, "ImportacionMapearUseCase | " + motivo);
    }
}
