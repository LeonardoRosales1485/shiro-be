package com.blue.open.Code.TipoTarea.CasosDeUso;

import com.blue.open.Code.TipoTarea.TipoTareaDTO;
import com.blue.open.Code.TipoTarea.TipoTareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Caso de uso: lista el catalogo de tipos de tarea.
// Dependencias externas: la base de datos.
// Recibe: nada. Responde: los tipos vigentes con su nivel, en el orden de los selectores.
@Service
@RequiredArgsConstructor
public class TipoTareaListarUseCase {

    private final TipoTareaRepository repository;

    // Responde los tipos de tarea vigentes
    @Transactional(readOnly = true)
    public List<TipoTareaDTO> ejecutar() {
        return repository.listarVigentes();
    }
}
