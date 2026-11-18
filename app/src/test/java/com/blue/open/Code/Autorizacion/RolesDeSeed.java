package com.blue.open.Code.Autorizacion;

/**
 * Uuids de los roles de los seeds de Liquibase que usan las pruebas de contrato de roles. Los permisos no tienen uuid
 * fijo: los crea InicializadorDeBaseService al arrancar, asi que se buscan por nombre
 * (EscenarioDeAutorizacion.uuidDePermiso).
 */
final class RolesDeSeed {

    // Rol CLIENTE global (2026-11-18-insert-roles-de-usuarios-seed.yaml): sin permisos y no es rol por defecto
    static final String CLIENTE = "90000000-0000-4000-8000-000000000009";

    private RolesDeSeed() {
    }
}
