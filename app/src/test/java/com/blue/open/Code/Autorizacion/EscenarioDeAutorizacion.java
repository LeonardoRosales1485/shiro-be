package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.Peticion;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Builder de escenarios de roles y permisos para las pruebas de contrato: busca permisos por nombre y crea o elimina
 * roles por los endpoints reales (pasos de preparacion, dentro de la transaccion del test: se revierten al terminar).
 * Equivalente de los scripts pre-request de bruno/Role y bruno/Permission.
 * Se construye con el cliente de la prueba: new EscenarioDeAutorizacion(this::como).
 */
public final class EscenarioDeAutorizacion {

    // Actor de los pasos de preparacion (tiene todos los permisos)
    public static final String SYSADMIN = RolBaseEnum.SYSADMIN.name();

    private final Function<String, Peticion> como;

    // Recibe el cliente de la prueba (rol -> peticion), normalmente this::como de una PruebaDeContrato
    public EscenarioDeAutorizacion(Function<String, Peticion> como) {
        this.como = como;
    }

    // Body de POST /rol/guardar para un alta: nombre, no es rol por defecto y los permisos recibidos (por uuid)
    public static Map<String, Object> rol(String nombre, String... permisosUuid) {
        List<Map<String, Object>> permisos = new ArrayList<>();
        for (String uuid : permisosUuid) {
            permisos.add(Map.of("uuid", uuid));
        }
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("nombre", nombre);
        cuerpo.put("porDefecto", 0);
        cuerpo.put("permisos", permisos);
        return cuerpo;
    }

    // Uuid del permiso con el nombre recibido (POST /permiso/paginar filtrando por nombre). Los permisos no tienen
    // uuid fijo: los crea InicializadorDeBaseService al arrancar
    public String uuidDePermiso(String nombre) {
        JsonNode pagina = como.apply(SYSADMIN).post("/permiso/paginar")
                .json(Map.of("pagina", 1, "tamano", 1, "filtros", List.of(List.of(
                        Map.of("campo", "nombre", "operacion", "=", "valor", nombre)))))
                .preparacion().ejecutar().esperar(200).json();
        assertEquals(1, pagina.get("totalDeElementos").asInt(), () -> "No existe el permiso " + nombre);
        return pagina.get("contenido").get(0).get("uuid").asString();
    }

    // Crea un rol con el nombre y los permisos (uuids) recibidos (POST /rol/guardar) y responde su uuid
    public String crearRol(String nombre, String... permisosUuid) {
        return como.apply(SYSADMIN).post("/rol/guardar").json(rol(nombre, permisosUuid))
                .preparacion().ejecutar().esperar(200).json().get("uuid").asString();
    }

    // Elimina el rol recibido (DELETE /rol/borrar/{uuid})
    public void eliminarRol(String uuid) {
        como.apply(SYSADMIN).delete("/rol/borrar/{uuid}", uuid).preparacion().ejecutar()
                .esperar(200);
    }
}
