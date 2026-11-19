package com.blue.open.Code.Importacion;

import com.blue.open.Code.Estado.Estado;
import com.blue.open.Code.Estado.EstadoRepository;
import com.blue.open.Code.TipoTablero.TipoTablero;
import com.blue.open.Code.TipoTarea.TipoTarea;
import com.blue.open.Code.TipoTarea.TipoTareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Crea en los catalogos de Shiro los estados y tipos de Jira que el mapeo pide crear (resolucion B32 del 19/11). Los
 * catalogos son globales: un estado nuevo aparece en ese tipo de tablero de todos los proyectos y un tipo nuevo, en el
 * catalogo de todos. Si ya hay uno vigente con el mismo nombre, lo reusa en vez de duplicarlo.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class CatalogosDesdeJira {

    private static final int LARGO_DEL_CODIGO = 20;

    private static final int LARGO_DEL_NOMBRE = 60;

    private final EstadoRepository estadoRepository;

    private final TipoTareaRepository tipoTareaRepository;

    // Uuid del estado del tipo de tablero con el nombre del de Jira: el que ya existe o uno nuevo al final de las columnas
    public String estado(TipoTablero tipoTablero, ElementoDeJiraDTO deJira, Long idUsuario) {
        List<Estado> estados = estadoRepository.findByTipoTableroAndStatusOrderByOrden(tipoTablero, 0);
        for (Estado estado : estados)
            if (estado.getNombre().equalsIgnoreCase(deJira.getNombre()))
                return estado.getUuid();
        Estado nuevo = new Estado();
        nuevo.setTipoTablero(tipoTablero);
        nuevo.setNombre(recortar(deJira.getNombre(), LARGO_DEL_NOMBRE));
        nuevo.setOrden(estados.isEmpty() ? 1 : estados.getLast().getOrden() + 1);
        nuevo.setInicial(false);
        nuevo.setTerminado(Boolean.TRUE.equals(deJira.getTerminado()));
        nuevo.setIdUsuarioDeCreacion(idUsuario);
        return estadoRepository.save(nuevo).getUuid();
    }

    // Uuid del tipo de tarea con el nombre del de Jira: el que ya existe o uno nuevo con su nivel, al final del catalogo
    public String tipo(ElementoDeJiraDTO deJira, Long idUsuario) {
        List<TipoTarea> tipos = tipoTareaRepository.findByStatusOrderByOrden(0);
        for (TipoTarea tipo : tipos)
            if (tipo.getNombre().equalsIgnoreCase(deJira.getNombre()))
                return tipo.getUuid();
        TipoTarea nuevo = new TipoTarea();
        nuevo.setCodigo(codigo(deJira, tipos));
        nuevo.setNombre(recortar(deJira.getNombre(), LARGO_DEL_NOMBRE));
        nuevo.setNivel(deJira.getNivel() == null ? 2 : deJira.getNivel());
        nuevo.setOrden(tipos.isEmpty() ? 1 : tipos.getLast().getOrden() + 1);
        nuevo.setIdUsuarioDeCreacion(idUsuario);
        return tipoTareaRepository.save(nuevo).getUuid();
    }

    // Codigo estable del tipo nuevo: JIRA_ y el nombre en mayusculas sin tildes ni simbolos (20 caracteres); JIRA_ y el
    // id de Jira si ya existe
    private static String codigo(ElementoDeJiraDTO deJira, List<TipoTarea> tipos) {
        String base = "JIRA_" + Normalizer.normalize(deJira.getNombre(), Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_").replaceAll("^_|_$", "");
        String codigo = recortar(base, LARGO_DEL_CODIGO);
        boolean repetido = tipos.stream().anyMatch(tipo -> tipo.getCodigo().equals(codigo));
        return repetido ? recortar("JIRA_" + deJira.getIdJira(), LARGO_DEL_CODIGO) : codigo;
    }

    // Texto recortado al largo de su columna
    private static String recortar(String texto, int largo) {
        return texto.length() > largo ? texto.substring(0, largo) : texto;
    }
}
