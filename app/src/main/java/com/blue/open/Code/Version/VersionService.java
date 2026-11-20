package com.blue.open.Code.Version;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Core.Crud.AbstractService;
import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * ABM de versiones (CU-VER-01 a CU-VER-03) sobre los hooks de AbstractService. Leer exige VER en el proyecto y crear
 * o editar, EDITAR. Una version publicada no se edita (VERSION_PUBLICADA). El estado, la fecha de publicacion y el
 * changelog solo los cambia publicar; el proyecto no cambia.
 * Dependencias externas: la base de datos.
 */
@Service
@RequiredArgsConstructor
public class VersionService extends AbstractService<Version, VersionDTO, VersionDTO> {

    private final VersionRepository versionRepository;

    private final ResolutorDePermisosDeRecurso permisos;

    private final RegistradorDeHistorial historial;

    // Exige VER en el proyecto de la version
    @Override
    protected void hookObtenerAuth(String uuid) {
        versionRepository.findFirstByUuidAndStatus(uuid, 0).ifPresent(version -> permisos.exigir(NivelDePermiso.VER, version.getProyecto()));
    }

    // El listado trae solo las versiones de proyectos visibles
    @Override
    protected PedidoDePaginado hookPaginarAntesDeConsultar(PedidoDePaginado pedido) {
        permisos.restringirAProyectosVisibles(pedido, "t.proyecto.id");
        return super.hookPaginarAntesDeConsultar(pedido);
    }

    // Aplica el DTO conservando proyecto, estado y fecha; rechaza editar una publicada y exige EDITAR en el proyecto
    @Override
    protected Version hookGuardarAplicarDto(VersionDTO dto, Version modelo) {
        boolean esAlta = modelo.getId() == null;
        if (!esAlta && modelo.getEstado() == EstadoDeVersion.PUBLICADA)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.VERSION_PUBLICADA, "VersionService | editar publicada " + modelo.getUuid());
        String nombreAnterior = modelo.getNombre();
        if (!esAlta)
            dto.setProyectoUuid(modelo.getProyecto().getUuid());
        Version aplicada = super.hookGuardarAplicarDto(dto, modelo);
        permisos.exigir(NivelDePermiso.EDITAR, aplicada.getProyecto());
        if (!esAlta && !Objects.equals(nombreAnterior, aplicada.getNombre()))
            historial.registrar(Historial.VERSION, aplicada.getUuid(), AccionDeHistorial.EDITAR, "nombre", nombreAnterior, aplicada.getNombre());
        return aplicada;
    }

    // Guarda la version y, si es un alta, escribe la fila CREAR
    @Override
    protected Version hookGuardarEnBase(Version modelo) {
        boolean esAlta = modelo.getId() == null;
        Version guardada = super.hookGuardarEnBase(modelo);
        if (esAlta)
            historial.registrar(Historial.VERSION, guardada.getUuid(), AccionDeHistorial.CREAR, null, null, guardada.getNombre());
        return guardada;
    }
}
