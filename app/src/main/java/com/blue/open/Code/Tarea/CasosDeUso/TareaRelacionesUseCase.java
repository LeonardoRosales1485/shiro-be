package com.blue.open.Code.Tarea.CasosDeUso;

import com.blue.open.Code.Relacion.RelacionRepository;
import com.blue.open.Code.Tarea.TareaRelacionDTO;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: relaciones vigentes de una tarea (vigente o archivada), vistas desde ella (T1.9). Una relacion se
 * lee desde las dos tareas: cada una con el nombre de su sentido.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la tarea. Responde: la lista de TareaRelacionDTO. Lanza REGISTRO_NO_ENCONTRADO si no existe.
 */
@Service
@RequiredArgsConstructor
public class TareaRelacionesUseCase {

    private final TareaRepository tareaRepository;

    private final RelacionRepository relacionRepository;

    @Transactional(readOnly = true)
    public List<TareaRelacionDTO> ejecutar(String uuid) {
        if (!tareaRepository.existsByUuid(uuid))
            throw new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "TareaRelacionesUseCase | ejecutar | tarea " + uuid);
        return relacionRepository.listarDeTarea(uuid);
    }
}
