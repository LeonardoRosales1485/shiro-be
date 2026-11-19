package com.blue.open.Code.Tarea.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivo;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivoRepository;
import com.blue.open.Code.OperacionDeArchivo.TipoDeOperacionDeArchivo;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaDTO;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: restaura una tarea archivada y todo lo que se archivo en su misma operacion (T1.8 y seccion 4 del
 * DAF); lo archivado antes por separado queda archivado. El Core no permite volver de -1 a 0: lo hace Tarea.restaurar.
 * Cada tarea restaurada deja su fila RESTAURAR en el historial, con la operacion nueva.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la tarea. Responde: TareaDTO de la tarea restaurada. Lanza REGISTRO_NO_ENCONTRADO si no existe o
 * no esta archivada, y PADRE_ARCHIVADO si su padre sigue archivado.
 */
@Service
@RequiredArgsConstructor
public class TareaRestaurarUseCase {

    private static final int ARCHIVADA = -1;

    private final TareaRepository tareaRepository;

    private final OperacionDeArchivoRepository operacionRepository;

    private final RegistradorDeHistorial historial;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final Proyector proyector;

    private final ResolutorDePermisosDeRecurso permisos;

    @Transactional
    public TareaDTO ejecutar(String uuid) {
        Tarea raiz = tareaRepository.bloquearArchivadaPorUuid(uuid)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "TareaRestaurarUseCase | ejecutar | tarea archivada " + uuid));
        permisos.exigir(NivelDePermiso.ARCHIVAR, raiz.getTablero());
        Tarea padre = raiz.getPadre();
        if (padre != null && padre.getStatus() == ARCHIVADA)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.PADRE_ARCHIVADO, "Restaurá primero " + padre.getClave() + ".",
                    "TareaRestaurarUseCase | ejecutar | tarea " + raiz.getClave() + " | padre " + padre.getClave())
                    .conDato("padreClave", padre.getClave());
        Long idUsuario = usuarioActual.getIdUsuarioActual();
        OperacionDeArchivo operacion = operacionRepository.save(
                new OperacionDeArchivo(TipoDeOperacionDeArchivo.RESTAURAR, Historial.TAREA, uuid, idUsuario));
        for (Tarea tarea : tareaRepository.findByIdOperacionArchivoAndStatus(raiz.getIdOperacionArchivo(), ARCHIVADA)) {
            tarea.restaurar();
            tarea.setIdUsuarioDeActualizacion(idUsuario);
            tareaRepository.save(tarea);
            historial.registrar(Historial.TAREA, tarea.getUuid(), AccionDeHistorial.RESTAURAR, null, null,
                    raiz.getClave(), operacion.getId());
        }
        return proyector.porUuid(Tarea.class, uuid, TareaDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "TareaRestaurarUseCase | ejecutar | no se pudo proyectar " + uuid));
    }
}
