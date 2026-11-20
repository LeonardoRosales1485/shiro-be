package com.blue.open.Code.Importacion.CasosDeUso;

import com.blue.open.Code.Importacion.ClienteDeJira;
import com.blue.open.Code.Importacion.CredencialesDeJira;
import com.blue.open.Code.Importacion.ImportacionProyectosDeJiraRequestDTO;
import com.blue.open.Code.Importacion.ImportacionProyectosDeJiraResponseDTO;
import com.blue.open.Code.Importacion.ValidadorDeUrlDeJira;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso: proyectos de Jira visibles con unas credenciales (CU-IMP-08, propuesta 53 resuelta el 20/11). Es el
 * «Probar conexion» del asistente: comprueba las credenciales y lista los proyectos para elegir cual importar. No
 * guarda nada: el token se usa solo para esta lectura.
 * Dependencias externas: Jira (ClienteDeJira).
 * Recibe: ImportacionProyectosDeJiraRequestDTO. Responde: lista de ImportacionProyectosDeJiraResponseDTO. Lanza JIRA_URL_NO_PERMITIDA,
 * JIRA_CREDENCIALES_INVALIDAS y JIRA_NO_DISPONIBLE.
 */
@Service
@RequiredArgsConstructor
public class ImportacionProyectosDeJiraUseCase {

    private final ClienteDeJira jira;

    private final ValidadorDeUrlDeJira validadorDeUrl;

    public List<ImportacionProyectosDeJiraResponseDTO> ejecutar(ImportacionProyectosDeJiraRequestDTO dto) {
        validadorDeUrl.validar(dto.getUrl());
        return jira.proyectos(new CredencialesDeJira(dto.getUrl(), dto.getCorreo().trim(), dto.getToken().trim()));
    }
}
