# Estructura de Carpetas

Este documento define, sin lugar a interpretación, dónde vive físicamente cada archivo dentro de `app/src/main/java/com/blue/open/Code` (y su espejo en `app/src/test/java/com/blue/open/Code`). Es de cumplimiento obligatorio: si un archivo nuevo no encaja claramente en ninguna regla de este documento, el agente debe detenerse y preguntar al usuario antes de crear una carpeta o convención nueva.

El Core no sigue esta estructura: desde 2.0.0 son módulos Maven separados en `sm-core-maven/core-*`, cada uno organizado por su propio criterio. Esta guía aplica únicamente a `Code`, que es el código específico de cada proyecto. Qué módulos del Core existen y cómo se eligen está en [`docs/arquitectura/core/README.md`](../arquitectura/core/README.md).

## Principio general

`Code` se organiza **por módulo/entidad de negocio (package-by-feature), nunca por tipo de archivo**. Está prohibido crear carpetas de primer nivel como `Controller/`, `Service/`, `Repository/` o `Entity/` dentro de `Code`. Todo lo que compone una entidad (Controller, Service, Repository, Entity, DTOs, casos de uso) vive junto, en una única carpeta con el nombre de esa entidad.

Razón: cuando se toca una funcionalidad, todos los archivos relevantes están en un solo lugar en vez de repartidos en 5 carpetas paralelas. Es la organización estándar de la industria para evitar que proyectos grandes se vuelvan inmanejables (ver también: Vertical Slice Architecture, Screaming Architecture).

## Regla de oro para ubicar un archivo nuevo

Aplicar en este orden. La primera regla que matchee decide la carpeta:

1. **¿El archivo es el Controller, Service, Repository, Entity (`@Entity`) o DTO de una entidad de negocio concreta?** → va en `Code/{Entidad}/`. (Ya no existen `RepositoryCustom`/`RepositoryImpl` por entidad: `EntidadRepositoryBase` del kernel provee `paginar()` para todos los repositorios.)
2. **¿El archivo solo tiene sentido para los flujos de una entidad específica, aunque no sea uno de los 5 tipos de arriba?** (ejemplos: un validador de un campo de esa entidad, una notificación por email que solo esa entidad dispara, un caso de uso). → va en `Code/{Entidad}/` (o en `Code/{Entidad}/CasosDeUso/` si es un caso de uso, o en `Code/{Entidad}/Notificaciones/` si es una notificación — ver más abajo). Esto aplica **aunque el código que lo invoque esté en otra entidad**: lo que importa es de qué dato/entidad depende el archivo, no quién lo llama. Un caso de uso que toca varias entidades vive en la carpeta de la entidad principal, la que aparece en la URL del endpoint (`POST /factura/emitir` → `Code/Factura/CasosDeUso/FacturaEmitirUseCase`); las demás entidades solo aportan su `Repository`.
3. **¿El archivo define roles o permisos del proyecto, o la lógica de asignación de roles?** → va en `Code/Autorizacion/` (carpeta fija, no es por-entidad).
4. **¿El archivo es infraestructura transversal que no depende de ninguna entidad puntual?** (configuración, WebSocket, utilidades genéricas) → va en `Code/Common/{Concern}/`, donde `{Concern}` agrupa archivos del mismo tema (`Configuracion`, `WebSocket`, etc). Si no existe un `{Concern}` adecuado, se puede crear uno nuevo con un nombre claro.
5. Si ninguna regla anterior aplica con claridad → **detenerse y preguntar al usuario**. No inventar una carpeta nueva por criterio propio.

## Estructura de un módulo de entidad

Todos los archivos van **planos dentro de `Code/{Entidad}/`**, sin subcarpetas `Controller/`, `Service/`, `Repository/`, etc. Las únicas subcarpetas permitidas dentro de un módulo de entidad son `CasosDeUso/` (ver [controllers-y-endpoints.md](controllers-y-endpoints.md)) y, si la entidad envía notificaciones, `Notificaciones/`.

```
Code/
└── {Entidad}/
    ├── {Entidad}.java                       # @Entity (JPA)
    ├── {Entidad}DTO.java                    # DTO principal (guardado/paginado si la entidad es chica)
    ├── {Entidad}PaginateDTO.java            # Solo si la entidad es grande (ver entidades-y-dtos.md)
    ├── {Entidad}Controller.java
    ├── {Entidad}Service.java
    ├── {Entidad}Repository.java              # extiende EntidadRepositoryBorradoLogico o EntidadRepository
    ├── CasosDeUso/
    │   └── {Entidad}{CasoDeUso}UseCase.java
    └── Notificaciones/                      # Solo si la entidad dispara emails/notificaciones propias
        └── {NombreNotificacion}.java
```

No todos los archivos son obligatorios: una entidad que no publica el ABM (como `Configuracion` en este template) no tiene `Service` y puede no tener `Notificaciones/`. Lo que nunca cambia es que **todo lo que sí existe para esa entidad vive en su misma carpeta**, plana.

### Ejemplo real: módulo `Empresa`

```
Code/Empresa/
├── Empresa.java                 # @Entity, extiende EmpresaBase (core-multiempresa)
├── EmpresaDTO.java              # extiende EmpresaBaseDTO (core-multiempresa)
├── EmpresaController.java       # ABM (receta de core-crud) + GET /contexto
├── EmpresaService.java          # extiende EmpresaBaseService (core-multiempresa)
├── EmpresaRepository.java       # extiende EntidadRepositoryBorradoLogico (core-kernel)
└── ValidadorDeCuitService.java  # valida el campo "cuit" de Empresa (regla 2 de la sección anterior)
```

`ValidadorDeCuitService` es invocado desde `EmpresaService` y también desde `UsuarioService` (porque el CUIT se valida durante el alta pública de usuario+empresa), pero vive en `Empresa/` porque el dato que valida es un campo de `Empresa`, no de `Usuario`. **Quién llama al archivo no determina su ubicación; de qué entidad depende sí.**

### Ejemplo real: módulo `Usuario`

```
Code/Usuario/
├── Usuario.java                 # @Entity, extiende UsuarioBaseMultiempresa (core-multiempresa)
├── UsuarioDTO.java              # extiende UsuarioBaseMultiempresaDTO (core-multiempresa)
├── UsuarioController.java       # ABM (receta de core-crud) + sesion, verificacion y altas publicas
├── UsuarioService.java          # extiende UsuarioBaseMultiempresaService (core-multiempresa)
├── UsuarioRepository.java       # extiende EntidadRepositoryBorradoLogico (core-kernel)
└── Notificaciones/
    ├── RecuperarContrasenaMail.java  # implementa NotificacionPorMail (Core), recibe un UsuarioBase
    ├── RegistroMail.java             # implementa NotificacionPorMail (Core), recibe un UsuarioBase
    ├── RecuperarContrasena/
    │   └── recuperar-contrasena.html
    └── Registro/
        └── codigo-de-verificacion.html
```

### Ejemplo real: módulo `Configuracion` (sin ABM)

```
Code/Configuracion/
├── Configuracion.java                     # @Entity, extiende EntidadConBorradoLogico (core-kernel)
├── ConfiguracionDTO.java                  # respuesta compartida por los 4 casos de uso
├── ConfiguracionGuardarRequestDTO.java    # request del caso de uso guardar
├── ConfiguracionController.java           # sin ABM: solo endpoints de sus casos de uso
├── ConfiguracionRepository.java           # extiende EntidadRepositoryBorradoLogico, con proyecciones
├── ConfiguracionDesdeBase.java            # implementa el puerto ProveedorDeConfiguracion (core-kernel)
└── CasosDeUso/
    ├── ConfiguracionListarUseCase.java
    ├── ConfiguracionObtenerUseCase.java
    ├── ConfiguracionObtenerVariasUseCase.java
    └── ConfiguracionGuardarUseCase.java   # aplica el DTO con el Proyector (core-kernel)
```

Desde Core 2.0.0 el Core no trae repositorios de usuario ni de empresa: los declara el proyecto, como cualquier otra entidad. El Core accede a esas tablas por los puertos `UsuarioRepositorio` y `EmpresaRepositorio`, que descubren la entidad en el metamodelo de JPA (ver "Relación con el Core" más abajo).

Las plantillas HTML de una notificación van en una subcarpeta con el nombre de esa notificación, dentro de `Notificaciones/`, junto a la clase que las usa. Si se agrega una notificación nueva, la clase debe cargar el `.html` con una ruta relativa a esa misma ubicación, por classpath (`getClass().getResourceAsStream("Carpeta/plantilla.html")`, ver `RecuperarContrasenaMail.java`). `app/pom.xml` declara `src/main/java/**/*.html` como recurso: sin eso las plantillas no llegan al jar.

## Módulos que no son una entidad de negocio

### `Code/Autorizacion/` — configuración de roles y permisos del proyecto

Carpeta fija (no depende de una entidad puntual). Contiene la personalización del sistema de autorización del Core para este proyecto:

```
Code/Autorizacion/
├── RolEnum.java               # implementa IRol (Core): roles concretos del proyecto
├── PermisoEnum.java           # implementa IPermiso (Core): permisos concretos del proyecto
├── CatalogoDelProyecto.java   # implementa CatalogoDeAutorizacion: le declara al Core RolEnum y PermisoEnum
├── RolController.java         # ABM de Rol (core-identity), receta de core-crud: endpoints /rol
├── PermisoController.java     # lectura de Permiso (core-identity): endpoints /permiso
└── RolService.java            # extiende RolBaseMultiempresaService (core-multiempresa): política de asignación de roles
```

El nombre de cada permiso (`auth_permisos.nombre`, por ejemplo `configuration.view`) es un valor persistido: no se traduce aunque la constante del enum esté en español (ADR 0002).

Cualquier archivo nuevo que defina un rol, un permiso, o la lógica de qué rol puede asignar qué, va acá.

### `Code/Common/{Concern}/` — infraestructura transversal

Para código que no pertenece a ninguna entidad ni es configuración de auth. Cada subcarpeta agrupa un tema (`Concern`) concreto:

```
Code/Common/
├── Configuracion/
│   ├── UrlsDelFrontProperties.java  # URLs del frontend (usadas por notificaciones y WebSocket)
│   └── AsincronicoConfig.java       # configuración de ejecución asincrónica (@EnableAsync)
├── Errores/
│   └── CodigoDeErrorDelProyecto.java
└── WebSocket/
    ├── WebSocketConfig.java
    ├── WebSocketAuthInterceptor.java
    └── StompPrincipal.java
```

`Common` y `WebSocket` son palabras reservadas: quedan en inglés (ver [idioma-y-estilo.md](idioma-y-estilo.md)). Los nombres de las clases de `WebSocket/` también quedan en inglés porque están formados por nombres de framework (`WebSocket`, `Stomp`), términos reservados (`Auth`), interfaces de Java (`Principal`) y sufijos técnicos (`Config`, `Interceptor`).

Nota sobre `AsincronicoConfig`: aunque históricamente estaba junto a los archivos de WebSocket, no depende de WebSocket en absoluto (es configuración genérica de un `Executor` asincrónico) — por eso vive en `Common/Configuracion/` y no en `Common/WebSocket/`. Al clasificar un archivo transversal, agruparlo por lo que realmente hace, no por dónde estaba antes.

Antes de crear un `{Concern}` nuevo dentro de `Common/`, revisar si el archivo encaja en uno ya existente.

## Convención de nombres y de casos de uso

Ver [entidades-y-dtos.md](entidades-y-dtos.md) para la convención de nombres de DTOs, y [controllers-y-endpoints.md](controllers-y-endpoints.md) para cuándo crear un endpoint del ABM vs. un DTO/UseCase por caso de uso. Ese documento asume que `{Entidad}{CasoDeUso}UseCase.java` vive en `Code/{Entidad}/CasosDeUso/`.

## Tests

La carpeta `app/src/test/java/com/blue/open/Code` **espeja exactamente** la estructura de `app/src/main/java/com/blue/open/Code`: un test de `Code/Usuario/UsuarioService.java` va en `app/src/test/.../Code/Usuario/` (por ejemplo `UsuarioGuardarContratoTest.java`), un test de `Code/Autorizacion/RolService.java` va en `app/src/test/.../Code/Autorizacion/`, etc. Nunca se crea una carpeta `Controller/` o `Service/` del lado de test si no existe del lado de main.

## Relación con el Core

El Core **no conoce** ninguna clase del proyecto: no importa `com.blue.open.Code` en ningún archivo.
Donde necesita algo del proyecto (el usuario logeado, la entidad de usuario, los roles declarados, la
versión) lo pide a través de un puerto que el proyecto implementa. El catálogo completo está en
[`docs/arquitectura/core/README.md`](../arquitectura/core/README.md).

Consecuencia práctica: renombrar o mover `Code/Usuario/Usuario.java`, `Code/Empresa/Empresa.java` o
cualquier otra clase del proyecto **no requiere tocar el Core**. El Core descubre la entidad de
usuario y la de empresa en el metamodelo de JPA, no por nombre.

Hasta 1.4.0 no era así: `BaseUserService`, `AuthService`, las clases de conversión y los repositories del Core
importaban `Code.User.User` y `Code.Company.Company` por nombre concreto, y mover esas clases obligaba
a editar el Core.

## Escaneo de Spring (`Application.java`)

`Application.java` debe declarar `@EnableJpaRepositories` y `@EntityScan` apuntando a los dos paquetes raíz completos. Esto permite agregar entidades nuevas en `Code/{Entidad}/` sin tocar `Application.java`, y que los módulos del Core que estén declarados como dependencia se registren solos (los que no estén, no existen):

```java
@EnableJpaRepositories(basePackages = {
        "com.blue.open.Code",
        "com.blue.open.Core",
}, repositoryBaseClass = EntidadRepositoryBase.class)
@EntityScan(basePackages = {
        "com.blue.open.Code",
        "com.blue.open.Core",
})
```

`repositoryBaseClass = EntidadRepositoryBase.class` es lo que le da `paginar()` a todos los repositorios sin necesidad de un `{Entidad}RepositoryImpl` por entidad.

## Cómo crecer: módulos grandes y agrupación por dominio

Esta estructura (`Code/{Entidad}/...`) es la única obligatoria hoy y sirve tanto para proyectos chicos como grandes. Cuando un proyecto acumula muchas entidades que forman grupos de negocio claros (por ejemplo 15+ entidades, con subconjuntos que claramente pertenecen a un mismo dominio como "Facturación" o "Logística"), se puede agregar **una capa de agrupación opcional** por encima, sin cambiar nada dentro de cada carpeta de entidad:

```
Code/
├── Facturacion/
│   ├── Factura/
│   ├── ItemFactura/
│   └── MedioDePago/
├── Logistica/
│   ├── Envio/
│   └── Deposito/
├── Auth/
└── Common/
```

Introducir esta capa es mover carpetas de entidad completas dentro de una carpeta de dominio nueva (`mkdir` + mover, sin tocar el contenido de cada entidad) y ajustar los imports afectados. **No crear esta capa preventivamente**: solo cuando el número de entidades y su agrupación natural lo justifiquen, y siempre coordinándolo con el usuario antes de reorganizar (regla general de este repositorio: ante ambigüedad, preguntar antes de decidir).

Si el dominio crece tanto que amerita compilación independiente, el techo siguiente es extraerlo a un módulo Maven propio (un `pom.xml` separado). Eso es un cambio de infraestructura de build, no solo de carpetas, y requiere pedido explícito del usuario.

## Checklist: agregar una entidad nueva desde cero

0. Listar los casos de uso de la funcionalidad y derivar de cada uno su endpoint y sus DTOs (ver "Orden de trabajo" en [entidades-y-dtos.md](entidades-y-dtos.md)). Los pasos 3, 5 y 6 solo aplican si la entidad publica el ABM.
1. Crear `Code/{Entidad}/`.
2. Crear `{Entidad}.java` (extiende `EntidadConBorradoLogico` si el borrado es lógico, o `EntidadBase` si es físico — ver [entidades-y-dtos.md](entidades-y-dtos.md)).
3. Crear `{Entidad}DTO.java` (y `{Entidad}PaginateDTO.java` si el listado necesita menos campos — ver [entidades-y-dtos.md](entidades-y-dtos.md)).
4. Crear `{Entidad}Repository.java` (extiende `EntidadRepositoryBorradoLogico<{Entidad}>` si la entidad tiene borrado lógico, o `EntidadRepository<{Entidad}>` si tiene borrado físico).
5. Crear `{Entidad}Service.java` (extiende `AbstractService`).
6. Crear `{Entidad}Controller.java` con la receta de `sm-core-maven/core-crud/docs/receta-controller.md`: solo los endpoints del ABM que la entidad necesita, cada uno con su `@Contrato` y su `permiso`.
7. Por cada caso de uso que no sea ABM de la entidad completa, crear `{Entidad}{CasoDeUso}UseCase.java` en `Code/{Entidad}/CasosDeUso/` con sus DTOs (ver [controllers-y-endpoints.md](controllers-y-endpoints.md)). Si la entidad no publica el ABM, el controller solo tiene los endpoints de sus casos de uso.
8. Si la entidad dispara notificaciones propias, crear `Code/{Entidad}/Notificaciones/`.
9. Agregar los seeds de Liquibase correspondientes (ver [base-de-datos.md](base-de-datos.md)).
10. No es necesario tocar `Application.java`: `com.blue.open.Code` ya está escaneado completo.
11. Todo endpoint lleva `@Contrato` con su `permiso` (`Contrato.PUBLICO`, `Contrato.AUTENTICADO` o roles/permisos): la aplicación no arranca si falta (ver "Autorización: `@Contrato`" en [controllers-y-endpoints.md](controllers-y-endpoints.md)).
