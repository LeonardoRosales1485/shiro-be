package com.blue.open.Code.Importacion.CasosDeUso;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Importacion.EjecucionDeImportacion;
import com.blue.open.Code.Importacion.Importacion;
import com.blue.open.Code.Importacion.ImportacionDTO;
import com.blue.open.Code.Importacion.ImportacionRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: ejecuta una importacion mapeada (CU-IMP-04, T4.3 y Spec 6). La pasa a EN_CURSO (una sola ejecucion a
 * la vez, aunque lleguen dos pedidos juntos) y la manda al ejecutorDeImportaciones, que corre en segundo plano y la
 * deja TERMINADA o FALLIDA. Se puede volver a ejecutar con el mismo mapeo: actualiza sin duplicar. No es
 * transaccional: el cambio a EN_CURSO se confirma antes de largar el proceso.
 * Dependencias externas: Jira y la base de datos (en segundo plano).
 * Recibe: uuid de la importacion. Responde: ImportacionDTO (EN_CURSO; el front consulta GET /importacion/uuid hasta
 * que termine). Lanza REGISTRO_NO_ENCONTRADO, IMPORTACION_SIN_MAPEO o IMPORTACION_EN_CURSO.
 */
@Service
@RequiredArgsConstructor
public class ImportacionEjecutarUseCase {

    private final ImportacionRepository importacionRepository;

    private final EjecucionDeImportacion ejecucion;

    @Qualifier("ejecutorDeImportaciones")
    private final TaskExecutor ejecutor;

    private final Proyector proyector;

    private final ProveedorDeUsuarioActual usuarioActual;

    public ImportacionDTO ejecutar(String uuid) {
        Importacion importacion = importacionRepository.findFirstByUuidAndStatus(uuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "ImportacionEjecutarUseCase | " + uuid));
        if (importacion.getMapeo() == null)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.IMPORTACION_SIN_MAPEO, "ImportacionEjecutarUseCase | " + uuid);
        if (importacionRepository.marcarEnCurso(importacion.getId()) == 0)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.IMPORTACION_EN_CURSO, "ImportacionEjecutarUseCase | " + uuid);
        Long idImportacion = importacion.getId();
        Long idUsuario = usuarioActual.getIdUsuarioActual();
        ejecutor.execute(() -> ejecucion.correr(idImportacion, uuid, idUsuario));
        return proyector.porUuid(Importacion.class, uuid, ImportacionDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "ImportacionEjecutarUseCase | " + uuid));
    }
}
