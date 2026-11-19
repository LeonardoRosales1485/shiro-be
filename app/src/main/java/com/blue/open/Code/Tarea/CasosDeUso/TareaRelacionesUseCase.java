package com.blue.open.Code.Tarea.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Relacion.RelacionRepository;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaRelacionesResponseDTO;
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
 * Recibe: uuid de la tarea. Responde: la lista de TareaRelacionesResponseDTO. Lanza REGISTRO_NO_ENCONTRADO si no existe.
 */
@Service
@RequiredArgsConstructor
public class TareaRelacionesUseCase {

    private final TareaRepository tareaRepository;

    private final ResolutorDePermisosDeRecurso permisos;

    private final RelacionRepository relacionRepository;

    @Transactional(readOnly = true)
    public List<TareaRelacionesResponseDTO> ejecutar(String uuid) {
        Tarea tarea = tareaRepository.findFirstByUuid(uuid)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "TareaRelacionesUseCase | ejecutar | tarea " + uuid));
        permisos.exigir(NivelDePermiso.VER, tarea.getTablero());
        return relacionRepository.listarDeTarea(uuid);
    }
}
