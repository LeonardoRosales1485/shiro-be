package com.blue.open.Code.OperacionDeArchivo;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Core.Kernel.DTO.Pagina;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Caso de uso: busqueda de lo archivado de cualquier tipo, segun permisos (T5.4 y Spec 3). Un elemento aparece si el
 * usuario tiene ARCHIVAR sobre su recurso (el tablero de la tarea; el proyecto en los demas); los usuarios
 * archivados, solo para administradores. Filtra por tipo y texto, ordena por fecha de borrado (lo mas reciente
 * primero) y pagina.
 * Dependencias externas: la base de datos.
 * Recibe: ArchivoPaginarRequestDTO. Responde: una pagina de ArchivadoDTO.
 */
@Service
@RequiredArgsConstructor
public class ArchivoBuscarUseCase {

    private final OperacionDeArchivoRepository repository;

    private final ResolutorDePermisosDeRecurso permisos;

    @Transactional(readOnly = true)
    public Pagina<ArchivadoDTO> ejecutar(ArchivoPaginarRequestDTO dto) {
        String tipo = dto.getTipo() == null || dto.getTipo().isBlank() ? null : dto.getTipo().trim().toUpperCase(Locale.ROOT);
        String texto = dto.getTexto() == null ? "" : dto.getTexto().trim().toLowerCase(Locale.ROOT);
        List<ElementoArchivado> candidatos = new ArrayList<>();
        if (tipo == null || tipo.equals("TAREA"))
            candidatos.addAll(repository.tareasArchivadas());
        if (tipo == null || tipo.equals("PROYECTO"))
            candidatos.addAll(repository.proyectosArchivados());
        if (tipo == null || tipo.equals("SPRINT"))
            candidatos.addAll(repository.sprintsArchivados());
        if (tipo == null || tipo.equals("VERSION"))
            candidatos.addAll(repository.versionesArchivadas());
        if ((tipo == null || tipo.equals("USUARIO")) && permisos.esAdministrador())
            candidatos.addAll(repository.usuariosArchivados());
        List<ArchivadoDTO> visibles = candidatos.stream()
                .filter(elemento -> elemento.nombre().toLowerCase(Locale.ROOT).contains(texto))
                .filter(this::puedeVer)
                .sorted(Comparator.comparing(ElementoArchivado::fechaDeBorrado, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ArchivadoDTO::new)
                .toList();
        int desde = Math.min((dto.getPagina() - 1) * dto.getTamano(), visibles.size());
        int hasta = Math.min(desde + dto.getTamano(), visibles.size());
        return new Pagina<>(visibles.subList(desde, hasta), dto.getPagina(), dto.getTamano(), visibles.size());
    }

    // ARCHIVAR sobre el recurso del elemento; los usuarios ya se filtraron por administrador
    private boolean puedeVer(ElementoArchivado elemento) {
        return elemento.idProyecto() == null || permisos.puede(NivelDePermiso.ARCHIVAR, elemento.idTablero(), elemento.idProyecto());
    }
}
