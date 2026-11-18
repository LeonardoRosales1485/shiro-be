# Testing

## Unitario

- Los test viven en el módulo que contiene el código que prueban: un test del paginado va en `sm-core-maven/core-kernel`, uno de las reglas de roles en `sm-core-maven/core-identity`, uno de las reglas por empresa en `sm-core-maven/core-multiempresa`, y uno del código del proyecto en `app`.
- Los mocks se pasan por constructor: la clase bajo test se construye con `new`, no con `@InjectMocks`. Ver [inyeccion-de-dependencias.md](inyeccion-de-dependencias.md).
  - Clase concreta (`Service`, `UseCase`, filtro): `new ProductoStockUseCase(repositoryMock, proyectorMock)`.
  - Clase base del Core (`AbstractService` y sus derivadas): se construye con `new` y los mocks entran por sus setters (`service.setAuthService(authServiceMock)`), porque esas clases se inyectan por setter y no por constructor.
  - `ReflectionTestUtils.setField` queda **solo** para los campos `@Value` (properties), no para dependencias. Ejemplo: `ReflectionTestUtils.setField(authService, "protocolos", "jwt,basic")`.
  - `@Autowired` sí se usa en los test de integración (`@SpringBootTest`), donde el bean lo provee el contexto de Spring.
- Los test unitarios se usan solo para funcionalidades que no dependan de datos externos. Ejemplos: exportación de información, queries complejas, procesamiento de imágenes (compresión, cropeo), conversión de archivos (docs a pdf, etc).
- No se deben crear test unitarios de funcionalidades que dependan de información externa o del contexto de ejecución.

## Pruebas de contrato

Cómo funcionan y cómo se escriben: [`docs/arquitectura/testing/README.md`](../arquitectura/testing/README.md).

- Cada endpoint tiene su prueba de contrato (`core-test`): una clase por caso de uso, `{Entidad}{CasoDeUso}ContratoTest`, en `app/src/test/java/com/blue/open/Code/{Entidad}/`, con el caso feliz y cada caso de error que el cliente tenga que distinguir.
- El método del controller lleva `@Contrato` (caso de uso, resumen y permiso) y cada campo de DTO y cada parámetro lleva `@Descripcion` (ver [idioma-y-estilo.md](idioma-y-estilo.md)). Los casos comunes del ABM de una entidad (receta de `sm-core-maven/core-crud/docs/receta-controller.md`) los cubre `{Entidad}ContratoTest`, que extiende `ContratoDeAbmTest`; sus metadatos salen del `@Contrato` de cada método.
- Todo caso de error assertea `codigo` con `esperarError(status, codigo)` (ver [errores.md](errores.md)).
- Toda constante de un `CodigoDeError` lleva `@Descripcion` (regla 7 de [errores.md](errores.md)): `CodigosDeErrorTest` (`app`) recorre todos los del classpath y falla nombrando el enum y la constante que no la tenga.
- Los casos "sin autenticación" y "sin permiso" no se escriben a mano: los genera `core-test` a partir del `permiso` declarado. Sí se escribe a mano lo que va más allá de eso (un rol que sí puede, una restricción por dependencia o por dueño).
- Los datos que el caso necesita y no están en los seeds se crean en el mismo test por los endpoints reales (builders `EscenarioDe{Entidad}`), nunca con SQL a mano. Todo corre en la transacción del test y se revierte al terminar.
- Las pruebas corren en surefire, sin Docker: `mvn -pl app test`. La corrida completa (sin `-Dtest`) exige que todo endpoint publicado tenga prueba (`ContratosCompletosTest`) y, si está en verde, regenera `docs/api`, que se commitea.
- Después de hacer cambios en el código se corren las pruebas del módulo afectado (`mvn -pl app test -Dtest='{Entidad}*'`) y, antes de commitear, la suite completa.
- Contra un ambiente desplegado no se corren pruebas de modificación: se verifica con `deploy/smoke.py` (health UP y versión de `/actuator/info`, ver `deploy/deploy.md`).

## Reglas verificadas por la suite

Además de las pruebas de contrato, `app` trae tests que verifican reglas de este repositorio de forma determinística (sin contexto de Spring, corren en segundos). Cada uno cita en su mensaje de fallo el id del checklist de auditoría ([`docs/auditoria/checklist.md`](../auditoria/checklist.md)) y el archivo de la regla:

| Test | Qué verifica |
|---|---|
| `CorePublicadoTest` | El Core llega solo como dependencia publicada: no hay `core/` ni clases en `com.blue.open.Core` dentro del proyecto (CORE-01) |
| `ArquitecturaTest` (ArchUnit) | Dependencias Core → Code, `@Autowired` sobre campos, estructura por entidad, herencia de entidades y DTOs, relaciones LAZY, forma de los `UseCase`, `Service` solo con hooks, repositorios con `Optional` |
| `EstiloDeCodigoTest` | Sobre los fuentes: tildes en el código, Lombok prohibido, referencias inline, `System.out`, concatenación en logs, errores sin `ExcepcionDeApi`, archivos al filesystem |
| `CodigosDeErrorTest` | `@Descripcion` y unicidad de todo `CodigoDeError` (ERR-03) |
| `ContratosCompletosTest` | Todo endpoint tiene prueba de contrato (TST-01) |

- Un fallo de estos tests se resuelve corrigiendo el código o registrando una excepción en `docs/excepciones.md` (ver [`docs/auditoria/README.md`](../auditoria/README.md)). **Nunca** desactivando, borrando o parcheando el test.
- Se corren solos con `mvn -pl app test -Dtest='CorePublicadoTest,ArquitecturaTest,EstiloDeCodigoTest,CodigosDeErrorTest'`; es lo que hace `/auditar` antes de la revisión por criterio.
- Una regla nueva que se pueda verificar así se agrega como test en `app/src/test/java/com/blue/open/` y se marca `suite` en el checklist.
