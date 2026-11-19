package com.blue.open.Code.Tarea.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso: archiva una tarea vigente y, en cascada, sus hijas vigentes en todos los niveles (T1.8 y adenda 3
 * del DAF), todas con el mismo id de operacion. Cada una deja su fila ARCHIVAR en el historial, con la clave de la
 * tarea raiz. Las hijas archivadas antes por separado no se tocan: conservan su propia operacion.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la tarea. Responde: TareaDTO de la tarea archivada (status -1). Lanza REGISTRO_NO_ENCONTRADO si
 * la tarea no existe o ya esta archivada.
 */
@Service
@RequiredArgsConstructor
public class TareaArchivarUseCase {

    private static final int VIGENTE = 0;

    private final TareaRepository tareaRepository;

    private final OperacionDeArchivoRepository operacionRepository;

    private final RegistradorDeHistorial historial;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final Proyector proyector;

    private final ResolutorDePermisosDeRecurso permisos;

    @Transactional
    public TareaDTO ejecutar(String uuid) {
        Tarea raiz = tareaRepository.bloquearVigentePorUuid(uuid)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "TareaArchivarUseCase | ejecutar | tarea vigente " + uuid));
        permisos.exigir(NivelDePermiso.ARCHIVAR, raiz.getTablero());
        Long idUsuario = usuarioActual.getIdUsuarioActual();
        OperacionDeArchivo operacion = operacionRepository.save(
                new OperacionDeArchivo(TipoDeOperacionDeArchivo.ARCHIVAR, Historial.TAREA, uuid, idUsuario));
        for (Tarea tarea : conDescendientes(raiz)) {
            tarea.archivar(idUsuario, operacion.getId());
            tareaRepository.save(tarea);
            historial.registrar(Historial.TAREA, tarea.getUuid(), AccionDeHistorial.ARCHIVAR, null, null,
                    raiz.getClave(), operacion.getId());
        }
        return proyector.porUuid(Tarea.class, uuid, TareaDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "TareaArchivarUseCase | ejecutar | no se pudo proyectar " + uuid));
    }

    // La tarea y sus descendientes vigentes, nivel por nivel: una consulta por nivel, no por tarea
    private List<Tarea> conDescendientes(Tarea raiz) {
        List<Tarea> todas = new ArrayList<>(List.of(raiz));
        List<Tarea> nivel = List.of(raiz);
        while (!nivel.isEmpty()) {
            nivel = tareaRepository.findByPadreInAndStatus(nivel, VIGENTE);
            todas.addAll(nivel);
        }
        return todas;
    }
}
