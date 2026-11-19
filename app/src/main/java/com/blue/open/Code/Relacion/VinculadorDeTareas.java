package com.blue.open.Code.Relacion;

import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Crea una relacion entre dos tareas y escribe la fila RELACIONAR en el historial de las dos, cada una con su
 * sentido. Lo usan crear relacion (T1.9) y clonar (T1.10, tipo CLONA). No valida: lo hace cada caso de uso.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class VinculadorDeTareas {

    private final RelacionRepository repository;

    private final RegistradorDeHistorial historial;

    private final ProveedorDeUsuarioActual usuarioActual;

    // Guarda la relacion de origen a destino y responde la relacion guardada
    public Relacion vincular(Tarea origen, Tarea destino, TipoDeRelacion tipo) {
        Relacion relacion = new Relacion(origen, destino, tipo);
        relacion.setIdUsuarioDeCreacion(usuarioActual.getIdUsuarioActual());
        Relacion guardada = repository.save(relacion);
        historial.registrar(Historial.TAREA, origen.getUuid(), AccionDeHistorial.RELACIONAR, null, null,
                tipo.vinculo(true).con(destino.getClave()));
        historial.registrar(Historial.TAREA, destino.getUuid(), AccionDeHistorial.RELACIONAR, null, null,
                tipo.vinculo(false).con(origen.getClave()));
        return guardada;
    }
}
