package com.blue.open.Code.Relacion.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivo;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivoRepository;
import com.blue.open.Code.OperacionDeArchivo.TipoDeOperacionDeArchivo;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Relacion.Relacion;
import com.blue.open.Code.Relacion.RelacionDTO;
import com.blue.open.Code.Relacion.RelacionRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Caso de uso: archiva una relacion vigente (T1.9), con su operacion de archivo. Deja de leerse desde las dos
 * tareas y queda una fila RELACIONAR (valor nuevo vacio) en el historial de cada una.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la relacion. Responde: RelacionDTO. Lanza REGISTRO_NO_ENCONTRADO si no existe o ya esta archivada.
 */
@Service
@RequiredArgsConstructor
public class RelacionArchivarUseCase {

    private static final String RELACION = "RELACION";

    private final RelacionRepository relacionRepository;

    private final OperacionDeArchivoRepository operacionRepository;

    private final RegistradorDeHistorial historial;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final Proyector proyector;

    private final ResolutorDePermisosDeRecurso permisos;

    @Transactional
    public RelacionDTO ejecutar(String uuid) {
        Relacion relacion = relacionRepository.findFirstByUuidAndStatus(uuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "RelacionArchivarUseCase | ejecutar | relacion vigente " + uuid));
        permisos.exigir(NivelDePermiso.EDITAR, relacion.getOrigen().getTablero());
        Long idUsuario = usuarioActual.getIdUsuarioActual();
        OperacionDeArchivo operacion = operacionRepository.save(
                new OperacionDeArchivo(TipoDeOperacionDeArchivo.ARCHIVAR, RELACION, uuid, idUsuario));
        relacion.archivar(idUsuario, operacion.getId());
        relacionRepository.save(relacion);
        historial.registrar(Historial.TAREA, relacion.getOrigen().getUuid(), AccionDeHistorial.RELACIONAR, null,
                relacion.getTipo().vinculo(true).con(relacion.getDestino().getClave()), null, operacion.getId());
        historial.registrar(Historial.TAREA, relacion.getDestino().getUuid(), AccionDeHistorial.RELACIONAR, null,
                relacion.getTipo().vinculo(false).con(relacion.getOrigen().getClave()), null, operacion.getId());
        return proyector.porUuid(Relacion.class, uuid, RelacionDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "RelacionArchivarUseCase | ejecutar | no se pudo proyectar " + uuid));
    }
}
