package com.blue.open.Code.ReglaPermiso.CasosDeUso;

import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivo;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivoRepository;
import com.blue.open.Code.OperacionDeArchivo.TipoDeOperacionDeArchivo;
import com.blue.open.Code.ReglaPermiso.ReglaPermiso;
import com.blue.open.Code.ReglaPermiso.ReglaPermisoDTO;
import com.blue.open.Code.ReglaPermiso.ReglaPermisoRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Caso de uso: archiva una regla de permiso vigente, con su operacion de archivo. Deja de aplicarse en el request
 * siguiente y queda una fila ARCHIVAR en su historial.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la regla. Responde: ReglaPermisoDTO. Lanza REGISTRO_NO_ENCONTRADO si no existe o ya esta archivada.
 */
@Service
@RequiredArgsConstructor
public class ReglaPermisoArchivarUseCase {

    private final ReglaPermisoRepository repository;

    private final OperacionDeArchivoRepository operaciones;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final RegistradorDeHistorial historial;

    private final Proyector proyector;

    @Transactional
    public ReglaPermisoDTO ejecutar(String uuid) {
        ReglaPermiso regla = repository.findFirstByUuidAndStatus(uuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "ReglaPermisoArchivarUseCase | ejecutar | regla vigente " + uuid));
        Long usuario = usuarioActual.getIdUsuarioActual();
        Long operacion = operaciones.save(new OperacionDeArchivo(TipoDeOperacionDeArchivo.ARCHIVAR,
                Historial.REGLA_PERMISO, uuid, usuario)).getId();
        regla.archivar(usuario, operacion);
        repository.save(regla);
        historial.registrar(Historial.REGLA_PERMISO, uuid, AccionDeHistorial.ARCHIVAR, null, null, null, operacion);
        return proyector.porUuid(ReglaPermiso.class, uuid, ReglaPermisoDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "ReglaPermisoArchivarUseCase | ejecutar | no se pudo proyectar " + uuid));
    }
}
