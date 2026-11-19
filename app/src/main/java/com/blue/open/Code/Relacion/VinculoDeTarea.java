package com.blue.open.Code.Relacion;

/**
 * Como ve una tarea su relacion con otra (seccion 4 del DAF): el sentido guardado y su inverso. Lleva el texto que
 * usa el historial ("bloqueada por SHI-3").
 */
public enum VinculoDeTarea {
    BLOQUEA("bloquea"),
    BLOQUEADA_POR("bloqueada por"),
    RELACIONA("se relaciona con"),
    DUPLICA("duplica"),
    DUPLICADA_POR("duplicada por"),
    CLONADA_EN("clonada en"),
    CLONADA_DE("clonada de");

    private final String texto;

    VinculoDeTarea(String texto) {
        this.texto = texto;
    }

    // Texto del vinculo seguido de la clave de la otra tarea, para el historial
    public String con(String clave) {
        return texto + " " + clave;
    }
}
