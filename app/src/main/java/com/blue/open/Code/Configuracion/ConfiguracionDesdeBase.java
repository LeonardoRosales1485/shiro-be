package com.blue.open.Code.Configuracion;

import com.blue.open.Core.Kernel.Puertos.ProveedorDeConfiguracion;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Conecta el puerto de configuracion del kernel con la tabla configuraciones de este proyecto.
 * Es lo que permite que la trazabilidad lea la clave DEBUG de la base sin que el Core conozca
 * la entidad Configuracion (en Core 1.x el Core importaba ConfiguracionService directamente).
 * Dependencias externas: la base de datos.
 * Recibe: la clave y su valor por defecto. Responde: el valor configurado.
 */
@Primary
@Component
@RequiredArgsConstructor
public class ConfiguracionDesdeBase implements ProveedorDeConfiguracion {

    private final ConfiguracionRepository repository;

    // Fuera de la transaccion del llamador: si la lectura falla devuelve el valor por defecto sin
    // dejar esa transaccion marcada como rollback-only (la trazabilidad consulta DEBUG en cualquier punto)
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String getValor(String clave, String porDefecto) {
        try {
            return repository.buscarValorPorClave(clave).orElse(porDefecto);
        } catch (Exception e) {
            return porDefecto;
        }
    }
}
