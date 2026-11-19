package com.blue.open.Code.Importacion.CasosDeUso;

import com.blue.open.Code.Importacion.CatalogoDeJira;
import com.blue.open.Code.Importacion.Importacion;
import com.blue.open.Code.Importacion.ImportacionCatalogoDTO;
import com.blue.open.Code.Importacion.ImportacionRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: vuelve a leer el catalogo de Jira de una importacion (CU-IMP-02), con sus credenciales guardadas.
 * Dependencias externas: Jira (ClienteDeJira) y la base de datos.
 * Recibe: uuid de la importacion. Responde: ImportacionCatalogoDTO. Lanza REGISTRO_NO_ENCONTRADO,
 * CREDENCIAL_NO_DESCIFRABLE y los errores de Jira.
 */
@Service
@RequiredArgsConstructor
public class ImportacionCatalogoUseCase {

    private final ImportacionRepository importacionRepository;

    private final CatalogoDeJira catalogo;

    public ImportacionCatalogoDTO ejecutar(String uuid) {
        Importacion importacion = importacionRepository.findFirstByUuidAndStatus(uuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "ImportacionCatalogoUseCase | " + uuid));
        return catalogo.catalogo(importacion, catalogo.credenciales(importacion));
    }
}
