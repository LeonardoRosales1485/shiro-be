# Seed demo opcional

El seed demo crea datos de ejemplo para probar y demostrar el sistema sin contaminar el changelog productivo.

## Datos creados

El archivo `db/changelog/seeds/2026-10-09-insert-usuarios-seed.yaml` crea:

- usuario `demo`;
- relacion del usuario `demo` con la empresa `BlueOpenData`;
- asignacion del rol base `ADMIN` al usuario `demo`;
- codigo de seguridad de ejemplo (`codigos_de_seguridad`);
- los actores de las pruebas de contrato (`sysadmin-pruebas`, `cliente`) y los usuarios pendientes de verificacion.

El archivo `db/changelog/seeds/2026-10-09-insert-tokens-seed.yaml` crea el token de acceso (`auth_access_tokens`) y el
refresh token (`auth_jwt_refresh_tokens`) de ejemplo del usuario `demo`, y los tokens de acceso de los actores de las
pruebas de contrato.

La configuracion de ejemplo esta en `seeds/2026-10-08-insert-configuracion-demo-seed.yaml`. El rol por defecto `CLIENTE` (sin permisos) de la empresa `BlueOpenData` esta en `seeds/2026-10-09-insert-empresas-seed.yaml`. Lo necesitan `publico/vincular` y `publico/registro-cliente`, que asignan el rol por defecto de la empresa y desde Core 2.0.1 no permiten entrar a la empresa de sistema.

Credenciales del usuario demo:

```text
usuario: demo
clave: 123
```

## Alcance

Este seed existe para ambientes locales, pruebas manuales y demostraciones.
No debe ejecutarse en produccion salvo una decision explicita del responsable del deploy.

## Reglas para ampliar el seed

- Mantener todos los registros de negocio asociados a la empresa `BlueOpenData`.
- Usar el rol base `ADMIN` para el usuario demo, salvo que exista una razon documentada para crear otro rol (como `CLIENTE`, ver arriba).
- Usar UUIDs estables y legibles para que los flujos de prueba puedan referenciarlos.
- No crear permisos inventados si el codigo no los reconoce.
- Mantener los inserts idempotentes.
- Si se agrega una tabla nueva al schema y necesita datos demo, agregar el dato en un nuevo changeset de seed.

## Revision

Los changesets de seed no requieren aprobacion de un responsable de datos: cualquier integrante del equipo puede agregar o ampliar datos demo sin el visto bueno extra que aplica al resto de los changelogs, ya que no afectan produccion.
