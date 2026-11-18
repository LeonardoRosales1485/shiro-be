package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.Auth.PermisoBaseEnum;
import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.ContratoDeAbmTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Contrato del ABM de /permiso (CU-PER-01 y CU-PER-02) con sus casos comunes. PermisoController solo publica
 * uuid y paginar: los permisos se sincronizan desde codigo al arrancar, asi que guardar y borrar no existen y
 * responden 404. Migrado de bruno/Permission (Save - no publicado). Los casos propios
 * de cada caso de uso estan en Permiso{CasoDeUso}ContratoTest.
 */
class PermisoContratoTest extends ContratoDeAbmTest {

    private final EscenarioDeAutorizacion autorizacion = new EscenarioDeAutorizacion(this::como);

    @Override
    protected Class<?> controlador() {
        return PermisoController.class;
    }

    @Override
    protected String rol() {
        return RolBaseEnum.SYSADMIN.name();
    }

    // Los permisos no tienen uuid fijo en los seeds: se busca el de PERMISO_VER
    @Override
    protected String uuidVigente() {
        return autorizacion.uuidDePermiso(PermisoBaseEnum.PERMISO_VER.getNombre());
    }

    // guardar no esta publicado: no se usa
    @Override
    protected Object cuerpoValido() {
        return Map.of();
    }

    // guardar no esta publicado: no se usa
    @Override
    protected Object cuerpoInvalido() {
        return Map.of();
    }


    @Test
    void guardarNoEstaPublicado() {
        como(rol()).post("/permiso/guardar").json(Map.of("nombre", "permiso.inventado")).ejecutar().esperar(404);
    }

    @Test
    void eliminarNoEstaPublicado() {
        String uuid = uuidVigente();
        como(rol()).delete("/permiso/borrar/{uuid}", uuid).ejecutar().esperar(404);
    }

    // Las rutas /permission que publicaba el Core hasta 3.2.0 (/paginate y /uuid/{uuid}) ya no existen
    @Test
    void rutaEnInglesNoExiste() {
        como(rol()).post("/permission/paginate").json(Map.of()).ejecutar().esperar(404);
        como(rol()).get("/permission/uuid/{uuid}", uuidVigente()).ejecutar().esperar(404);
    }
}
