package com.blue.open.Code.Tarea.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Estado.Estado;
import com.blue.open.Code.Estado.EstadoRepository;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaDTO;
import com.blue.open.Code.Tarea.TareaMoverRequestDTO;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: mueve una tarea vigente a otro estado de su tablero (T1.5). Cualquier estado del tablero es valido
 * (seccion 4 del DAF: las transiciones estrictas quedan para despues de la v1). Deja una fila MOVER en el historial.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la tarea y TareaMoverRequestDTO con el estado de destino.
 * Responde: TareaDTO con la tarea movida. Lanza REGISTRO_NO_ENCONTRADO, RELACION_NO_ENCONTRADA o
 * ESTADO_DE_OTRO_TABLERO.
 */
@Service
@RequiredArgsConstructor
public class TareaMoverUseCase {

    private static final int VIGENTE = 0;

    private final TareaRepository tareaRepository;

    private final EstadoRepository estadoRepository;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final Proyector proyector;

    private final RegistradorDeHistorial historial;

    private final ResolutorDePermisosDeRecurso permisos;

    @Transactional
    public TareaDTO ejecutar(String uuid, TareaMoverRequestDTO dto) {
        Tarea tarea = tareaRepository.findFirstByUuidAndStatus(uuid, VIGENTE)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "TareaMoverUseCase | ejecutar | tarea " + uuid));
        permisos.exigir(NivelDePermiso.EDITAR, tarea.getTablero());
        Estado estado = estadoRepository.findFirstByUuidAndStatus(dto.getEstadoUuid(), VIGENTE)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.RELACION_NO_ENCONTRADA,
                        "TareaMoverUseCase | ejecutar | estado " + dto.getEstadoUuid()).conDato("campo", "estadoUuid"));
        if (!estado.getTipoTablero().getId().equals(tarea.getTablero().getTipoTablero().getId()))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.ESTADO_DE_OTRO_TABLERO,
                    "TareaMoverUseCase | ejecutar | tarea " + tarea.getClave() + " | estado " + estado.getUuid());
        String estadoAnterior = tarea.getEstado().getNombre();
        tarea.setEstado(estado);
        tarea.setIdUsuarioDeActualizacion(usuarioActual.getIdUsuarioActual());
        tareaRepository.save(tarea);
        historial.registrar(Historial.TAREA, uuid, AccionDeHistorial.MOVER, "estado", estadoAnterior, estado.getNombre());
        return proyector.porUuid(Tarea.class, uuid, TareaDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "TareaMoverUseCase | ejecutar | no se pudo proyectar " + uuid));
    }
}
