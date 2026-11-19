package com.blue.open.Code.Relacion;

/**
 * Tipo de relacion entre tareas (Spec 1): se guarda un sentido, de origen a destino. En CLONA el origen es la tarea
 * original y el destino, el clon. Se persiste por nombre.
 */
public enum TipoDeRelacion {
    BLOQUEA(VinculoDeTarea.BLOQUEA, VinculoDeTarea.BLOQUEADA_POR),
    RELACIONA(VinculoDeTarea.RELACIONA, VinculoDeTarea.RELACIONA),
    DUPLICA(VinculoDeTarea.DUPLICA, VinculoDeTarea.DUPLICADA_POR),
    CLONA(VinculoDeTarea.CLONADA_EN, VinculoDeTarea.CLONADA_DE);

    private final VinculoDeTarea desdeElOrigen;

    private final VinculoDeTarea desdeElDestino;

    TipoDeRelacion(VinculoDeTarea desdeElOrigen, VinculoDeTarea desdeElDestino) {
        this.desdeElOrigen = desdeElOrigen;
        this.desdeElDestino = desdeElDestino;
    }

    // Como ve la relacion la tarea origen (true) o la destino (false)
    public VinculoDeTarea vinculo(boolean esOrigen) {
        return esOrigen ? desdeElOrigen : desdeElDestino;
    }
}
