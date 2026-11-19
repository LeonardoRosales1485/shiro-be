package com.blue.open.Code.Tarea.CasosDeUso;

import com.blue.open.Code.Common.Canal.CanalDeCambio;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.HistorialDTO;
import com.blue.open.Code.Historial.HistorialRepository;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: historial de cambios de una tarea, vigente o archivada (T1.6), el mas nuevo primero, opcionalmente
 * solo de un canal (por ejemplo, los cambios hechos por MCP).
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la tarea y el canal (o null para todos).
 * Responde: la lista de HistorialDTO. Lanza REGISTRO_NO_ENCONTRADO si la tarea no existe.
 */
@Service
@RequiredArgsConstructor
public class TareaHistorialUseCase {

    private final TareaRepository tareaRepository;

    private final HistorialRepository historialRepository;

    @Transactional(readOnly = true)
    public List<HistorialDTO> ejecutar(String uuid, CanalDeCambio canal) {
        if (!tareaRepository.existsByUuid(uuid))
            throw new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "TareaHistorialUseCase | ejecutar | tarea " + uuid);
        return canal == null ? historialRepository.listar(Historial.TAREA, uuid)
                : historialRepository.listarPorCanal(Historial.TAREA, uuid, canal);
    }
}
