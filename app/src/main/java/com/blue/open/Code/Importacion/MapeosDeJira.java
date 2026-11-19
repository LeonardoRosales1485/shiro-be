package com.blue.open.Code.Importacion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Lee y escribe shr_mapeo_jira para el importador: que registro de Shiro corresponde a cada elemento de Jira.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class MapeosDeJira {

    private final MapeoJiraRepository repository;

    // Uuid de Shiro del elemento de Jira, si ya se importo
    public Optional<String> uuid(TipoDeEntidadJira tipo, String idJira) {
        return repository.findFirstByTipoEntidadAndIdJira(tipo, idJira).map(MapeoJira::getUuidShiro);
    }

    // Uuid de Shiro por id de Jira de los elementos recibidos que ya se importaron, en una consulta
    public Map<String, String> uuids(TipoDeEntidadJira tipo, Collection<String> idsJira) {
        Map<String, String> uuids = new HashMap<>();
        if (!idsJira.isEmpty())
            repository.findByTipoEntidadAndIdJiraIn(tipo, idsJira).forEach(mapeo -> uuids.put(mapeo.getIdJira(), mapeo.getUuidShiro()));
        return uuids;
    }

    // Registra que el elemento de Jira quedo en el registro de Shiro recibido
    public void registrar(TipoDeEntidadJira tipo, String idJira, String uuidShiro, Long idUsuario) {
        repository.save(new MapeoJira(tipo, idJira, uuidShiro, idUsuario));
    }
}
