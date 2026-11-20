package com.blue.open.Code.Importacion.CasosDeUso;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Importacion.Importacion;
import com.blue.open.Code.Importacion.ImportacionDTO;
import com.blue.open.Code.Importacion.ImportacionRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: cancelar una importacion en curso (CU-IMP-07, propuesta 55 resuelta el 20/11). La marca para que la
 * ejecucion se corte en su proximo control (a lo sumo en un segundo de trabajo): como la importacion corre en una sola
 * transaccion, no queda nada de esa ejecucion y pasa a CANCELADA.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la importacion. Responde: ImportacionDTO (EN_CURSO, con cancelacionPedida). Lanza
 * REGISTRO_NO_ENCONTRADO e IMPORTACION_NO_EN_CURSO.
 */
@Service
@RequiredArgsConstructor
public class ImportacionCancelarUseCase {

    private final ImportacionRepository repository;

    private final Proyector proyector;

    @Transactional
    public ImportacionDTO ejecutar(String uuid) {
        Importacion importacion = repository.findFirstByUuidAndStatus(uuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "ImportacionCancelarUseCase | " + uuid));
        if (repository.pedirCancelacion(importacion.getId()) == 0)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.IMPORTACION_NO_EN_CURSO, "ImportacionCancelarUseCase | " + uuid);
        return proyector.porUuid(Importacion.class, uuid, ImportacionDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "ImportacionCancelarUseCase | " + uuid));
    }
}
