package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.ActoresDePrueba;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Actores de las pruebas de contrato: un usuario por rol con su token de acceso (protocolo tokens), creados por los
 * seeds de Liquibase (2026-10-09-insert-usuarios-seed.yaml).
 * Para el caso automatico "sin permiso" usa CLIENTE, el rol por defecto de la empresa BlueOpenData, que no otorga
 * ningun permiso.
 * Dependencias externas: ninguna.
 */
@Component
public class ActoresDelProyecto implements ActoresDePrueba {

    // Rol CLIENTE (2026-10-09-insert-empresas-seed.yaml): sin permisos
    public static final String CLIENTE = "CLIENTE";

    // Rol -> token de acceso del seed
    private static final Map<String, String> TOKENS = new LinkedHashMap<>();

    static {
        TOKENS.put(RolBaseEnum.SYSADMIN.name(), "sysadmin-access-token");
        TOKENS.put(RolBaseEnum.ADMIN.name(), "demo-access-token");
        TOKENS.put(CLIENTE, "cliente-access-token");
    }

    // Token del actor del rol. Lanza IllegalArgumentException si el rol no tiene usuario en los seeds
    @Override
    public String token(String rol) {
        String token = TOKENS.get(rol);
        if (token == null) {
            throw new IllegalArgumentException("No hay actor de prueba para el rol " + rol + ". Roles: " + TOKENS.keySet());
        }
        return token;
    }

    // CLIENTE no tiene ningun permiso: es el actor sin permiso de toda operacion, salvo de las que exigen ese rol
    @Override
    public Optional<String> rolSinPermiso(List<String> permisos) {
        return permisos.contains(CLIENTE) ? Optional.empty() : Optional.of(CLIENTE);
    }
}
