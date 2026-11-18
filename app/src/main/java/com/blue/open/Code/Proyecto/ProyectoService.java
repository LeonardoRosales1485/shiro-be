package com.blue.open.Code.Proyecto;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Code.Tablero.TableroRepository;
import com.blue.open.Code.TipoTablero.TipoTablero;
import com.blue.open.Code.TipoTablero.TipoTableroRepository;
import com.blue.open.Core.Crud.AbstractService;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service del ABM de proyectos sobre AbstractService. Aplica las reglas del prefijo (unico entre todos los proyectos,
 * tambien archivados, y fijo despues de crear) y, en el alta, crea los cuatro tableros del proyecto en la misma
 * transaccion de guardar.
 * Dependencias externas: la base de datos.
 * Recibe: ProyectoDTO o uuid segun el endpoint. Responde: ProyectoDTO.
 */
@Service
@RequiredArgsConstructor
public class ProyectoService extends AbstractService<Proyecto, ProyectoDTO, ProyectoDTO> {

    private final ProyectoRepository proyectoRepository;

    private final TipoTableroRepository tipoTableroRepository;

    private final TableroRepository tableroRepository;

    /*
     * Valida el prefijo antes de aplicar el DTO y lo deja en mayusculas. Alta: el prefijo no puede usarlo otro
     * proyecto, vigente o archivado. Edicion: el prefijo no cambia. Lanza PREFIJO_DE_PROYECTO_EN_USO o
     * PREFIJO_DE_PROYECTO_NO_SE_CAMBIA.
     */
    @Override
    protected Proyecto hookGuardarAplicarDto(ProyectoDTO dto, Proyecto modelo) {
        String prefijo = dto.getPrefijo().toUpperCase(Locale.ROOT);
        boolean esAlta = modelo.getId() == null;
        if (!esAlta && !prefijo.equals(modelo.getPrefijo())) {
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.PREFIJO_DE_PROYECTO_NO_SE_CAMBIA);
        }
        if (esAlta && proyectoRepository.existsByPrefijo(prefijo)) {
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.PREFIJO_DE_PROYECTO_EN_USO,
                    "El prefijo " + prefijo + " ya lo usa otro proyecto. Elegí otro.",
                    "ProyectoService | hookGuardarAplicarDto | prefijo en uso: " + prefijo);
        }
        Proyecto aplicado = super.hookGuardarAplicarDto(dto, modelo);
        aplicado.setPrefijo(prefijo);
        return aplicado;
    }

    // Guarda el proyecto y, si es un alta, crea un tablero por cada tipo de tablero vigente, en orden de pestana
    @Override
    protected Proyecto hookGuardarEnBase(Proyecto modelo) {
        boolean esAlta = modelo.getId() == null;
        Proyecto guardado = super.hookGuardarEnBase(modelo);
        if (!esAlta) {
            return guardado;
        }
        for (TipoTablero tipo : tipoTableroRepository.findByStatusOrderByOrden(0)) {
            tableroRepository.save(new Tablero(guardado, tipo));
        }
        return guardado;
    }
}
