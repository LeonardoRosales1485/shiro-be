package com.blue.open.Code.Autorizacion;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Identity.DTO.RolDTO;
import com.blue.open.Core.Identity.Entidad.Rol;
import com.blue.open.Core.Identity.Repository.RolRepository;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

/**
 * Caso de uso: archiva un grupo (rol) vigente (T2.5): nada se borra. Los grupos del sistema (SYSADMIN, ADMIN) no se
 * archivan. Deja una fila ARCHIVAR en el historial del grupo.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del grupo. Responde: RolDTO. Lanza REGISTRO_NO_ENCONTRADO o GRUPO_DEL_SISTEMA.
 */
@Service
@RequiredArgsConstructor
public class RolArchivarUseCase {

    private static final String GRUPO = "GRUPO";

    private final GrupoRepository grupoRepository;

    private final RolRepository rolRepository;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final RegistradorDeHistorial historial;

    private final Proyector proyector;

    @Transactional
    public RolDTO ejecutar(String uuid) {
        Rol grupo = grupoRepository.findFirstByUuidAndStatus(uuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "RolArchivarUseCase | ejecutar | grupo vigente " + uuid));
        if (ProteccionDeAdministradores.esDelSistema(grupo))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.GRUPO_DEL_SISTEMA, "RolArchivarUseCase | ejecutar | " + grupo.getNombre());
        grupo.setStatus(-1);
        grupo.setFechaDeBorrado(new Timestamp(System.currentTimeMillis()));
        grupo.setIdUsuarioDeBorrado(usuarioActual.getIdUsuarioActual());
        rolRepository.save(grupo);
        historial.registrar(GRUPO, uuid, AccionDeHistorial.ARCHIVAR, null, null, grupo.getNombre());
        return proyector.porUuid(Rol.class, uuid, RolDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "RolArchivarUseCase | ejecutar | no se pudo proyectar " + uuid));
    }
}
