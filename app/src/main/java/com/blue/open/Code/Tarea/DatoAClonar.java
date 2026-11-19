package com.blue.open.Code.Tarea;

import java.util.List;

/**
 * Datos opcionales que se copian al clonar una tarea (adenda 1 del DAF). Titulo y tipo se copian siempre.
 * VERSIONES se acepta desde ya y empieza a copiar cuando existan las versiones (F5).
 */
public enum DatoAClonar {
    DESCRIPCION,
    PRIORIDAD,
    ETIQUETAS,
    PUNTOS,
    RESPONSABLE,
    VERSIONES,
    COMENTARIOS,
    SUBTAREAS;

    // Seleccion por defecto: lo que copiaba la regla original del DAF
    public static final List<DatoAClonar> POR_DEFECTO = List.of(DESCRIPCION, PRIORIDAD, ETIQUETAS);
}
