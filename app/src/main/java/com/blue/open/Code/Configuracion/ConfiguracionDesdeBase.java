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

    // Dentro de la transaccion del llamador, si hay una: TraceDeEjecucionAspect consulta DEBUG en cada metodo de un
    // Controller o Service. Con NOT_SUPPORTED (como venia del template) cada llamada a un Service dentro de una
    // transaccion pedia una segunda conexion al pool; con 10 transacciones abiertas a la vez el pool se agotaba y
    // quedaban trabadas (T1.3, 50 altas concurrentes de tareas). Si la lectura falla devuelve el valor por defecto;
    // un error de la base en esa lectura invalida la transaccion del llamador, que de todos modos no podria seguir
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public String getValor(String clave, String porDefecto) {
        try {
            return repository.buscarValorPorClave(clave).orElse(porDefecto);
        } catch (Exception e) {
            return porDefecto;
        }
    }
}
