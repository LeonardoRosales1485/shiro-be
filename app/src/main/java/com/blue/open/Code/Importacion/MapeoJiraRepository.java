package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Repository.EntidadRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de MapeoJira.
 */
public interface MapeoJiraRepository extends EntidadRepository<MapeoJira> {

    // Mapeo de un elemento de Jira, si ya se importo
    Optional<MapeoJira> findFirstByTipoEntidadAndIdJira(TipoDeEntidadJira tipoEntidad, String idJira);

    // Mapeos de varios elementos del mismo tipo, en una consulta
    List<MapeoJira> findByTipoEntidadAndIdJiraIn(TipoDeEntidadJira tipoEntidad, Collection<String> idsJira);
}
