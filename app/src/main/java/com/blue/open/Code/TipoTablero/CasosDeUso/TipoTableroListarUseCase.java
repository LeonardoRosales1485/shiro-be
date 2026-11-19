package com.blue.open.Code.TipoTablero.CasosDeUso;

import com.blue.open.Code.TipoTablero.TipoTablero;
import com.blue.open.Code.TipoTablero.TipoTableroDTO;
import com.blue.open.Code.TipoTablero.TipoTableroRepository;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Caso de uso: lista el catalogo de tableros con sus estados en orden de columna.
// Dependencias externas: la base de datos.
// Recibe: nada. Responde: los tipos de tablero vigentes en orden de pestana, cada uno con sus estados (dos consultas en
// total: los tableros y, con @Coleccion, los estados de todos).
@Service
@RequiredArgsConstructor
public class TipoTableroListarUseCase {

    private final TipoTableroRepository repository;

    private final Proyector proyector;

    // Responde los tipos de tablero vigentes con sus estados
    @Transactional(readOnly = true)
    public List<TipoTableroDTO> ejecutar() {
        return proyector.completarColecciones(TipoTablero.class, repository.listarVigentes());
    }
}
