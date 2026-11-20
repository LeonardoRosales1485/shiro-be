package com.blue.open.Code.Importacion;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Seguimiento de una ejecucion del importador (propuesta 55): guarda el avance y mira si un administrador pidio
 * cancelarla. Corre en su propia transaccion (la de la importacion se confirma recien al final), asi el front ve el
 * avance mientras corre y la cancelacion llega aunque la importacion siga abierta.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class SeguimientoDeImportacion {

    private final ImportacionRepository repository;

    private final ObjectMapper lector = new ObjectMapper();

    // Guarda el avance de la importacion. Lanza IMPORTACION_CANCELADA si se pidio cancelarla
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void controlar(Long idImportacion, AvanceDeImportacion avance) {
        repository.guardarAvance(idImportacion, avance(avance));
        if (repository.cancelacionPedida(idImportacion).orElse(false))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.IMPORTACION_CANCELADA, "SeguimientoDeImportacion | controlar | " + idImportacion);
    }

    // Avance como JSON, para guardarlo en Importacion.avance
    public String avance(AvanceDeImportacion avance) {
        return lector.writeValueAsString(avance);
    }
}
