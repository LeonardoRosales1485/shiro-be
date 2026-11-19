package com.blue.open.Code.Importacion;

import com.blue.open.Core.Crud.AbstractService;
import org.springframework.stereotype.Service;

/**
 * Service del ABM de importaciones sobre AbstractService: solo obtener y paginar (CU-IMP-05 y CU-IMP-06). Las
 * importaciones se crean al conectar con Jira y no se borran. El DTO no lleva el token (es @NoProyectable).
 * Dependencias externas: la base de datos.
 * Recibe: uuid o PedidoDePaginado. Responde: ImportacionDTO.
 */
@Service
public class ImportacionService extends AbstractService<Importacion, ImportacionDTO, ImportacionDTO> {
    // Sin overrides: el permiso (administradores) lo declara el @Contrato del controller
}
