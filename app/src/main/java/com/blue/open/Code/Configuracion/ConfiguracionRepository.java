package com.blue.open.Code.Configuracion;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de configuraciones. Todas las consultas devuelven solo registros activos (status = 0):
 * una configuracion borrada logicamente deja de existir para la aplicacion.
 */
public interface ConfiguracionRepository extends EntidadRepositoryBorradoLogico<Configuracion> {

    // Todas las configuraciones activas, publicas y privadas
    @Query("SELECT new com.blue.open.Code.Configuracion.ConfiguracionDTO(c.uuid, c.status, c.fechaDeCreacion, c.nombre, "
            + "c.valor, c.global) FROM Configuracion c WHERE c.status = 0")
    List<ConfiguracionDTO> listarVigentes();

    // Configuraciones activas visibles sin sesion (global = true)
    @Query("SELECT new com.blue.open.Code.Configuracion.ConfiguracionDTO(c.uuid, c.status, c.fechaDeCreacion, c.nombre, "
            + "c.valor, c.global) FROM Configuracion c WHERE c.status = 0 AND c.global = true")
    List<ConfiguracionDTO> listarPublicas();

    // Configuracion activa por clave
    @Query("SELECT new com.blue.open.Code.Configuracion.ConfiguracionDTO(c.uuid, c.status, c.fechaDeCreacion, c.nombre, "
            + "c.valor, c.global) FROM Configuracion c WHERE c.nombre = :clave AND c.status = 0")
    Optional<ConfiguracionDTO> buscarPorClave(@Param("clave") String clave);

    // Configuraciones activas cuyas claves esten en la lista
    @Query("SELECT new com.blue.open.Code.Configuracion.ConfiguracionDTO(c.uuid, c.status, c.fechaDeCreacion, c.nombre, "
            + "c.valor, c.global) FROM Configuracion c WHERE c.nombre IN :claves AND c.status = 0")
    List<ConfiguracionDTO> buscarPorClaves(@Param("claves") List<String> claves);

    // Solo el valor de una configuracion activa (lo usa ConfiguracionDesdeBase)
    @Query("SELECT c.valor FROM Configuracion c WHERE c.nombre = :clave AND c.status = 0")
    Optional<String> buscarValorPorClave(@Param("clave") String clave);

    // Entidad activa por clave, para editarla en el caso de uso guardar
    Optional<Configuracion> findFirstByNombreAndStatus(String nombre, Integer status);
}
