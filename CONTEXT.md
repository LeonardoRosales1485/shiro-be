# Contexto: sm-template (backend)

Vocabulario de la metodología con la que se construyen los backends a partir de este template.

## Términos

**Caso de uso**:
Una acción concreta que el usuario o el sistema necesita hacer (una pantalla, un botón, un reporte, una integración), con los datos que recibe y los que responde. Es el punto de partida al diseñar una funcionalidad: de él se derivan el endpoint, los DTOs y, después, la entidad.
_Evitar_: funcionalidad, operación, feature

**ABM de una entidad**:
Los cuatro endpoints (obtener por uuid, paginar, guardar, borrar) que el controller de una entidad escribe con la receta de `core-crud` y que delegan en `AbstractService`. Es la forma por defecto de resolver los casos de uso de una entidad chica. Desde Core 3.0.0 no se heredan.
_Evitar_: endpoint heredado, CRUD genérico, endpoint base

**Entidad chica**:
Entidad cuyos casos de uso son todos un ABM que usa todos sus campos. Se define por sus casos de uso, no por su cantidad de campos.
_Evitar_: entidad simple, entidad con pocos campos

**Paginar**:
Endpoint del ABM que lista registros con filtros, orden y paginación.
_Evitar_: paginate

**Guardar**:
Endpoint del ABM que crea un registro si no trae uuid, o lo actualiza si lo trae.
_Evitar_: save

**Usuario**:
Persona que se autentica en el sistema.
_Evitar_: user

**Empresa**:
Organización a la que pertenecen los usuarios en un proyecto multiempresa.
_Evitar_: company

**Rol**:
Conjunto de permisos que se asigna a un usuario (por empresa, en multiempresa).
_Evitar_: role

**Permiso**:
Acción puntual que un rol habilita.
_Evitar_: permission

**Codigo de seguridad**:
Código de un solo uso que se envía al usuario para verificar su identidad (registro, recuperación de contraseña).
_Evitar_: security code

**Contrasena**:
Secreto con el que un usuario se autentica.
_Evitar_: password

**AccessToken**:
Credencial de acceso de un usuario. Término reservado: no se traduce.

**Configuracion**:
Par clave/valor persistido que ajusta el comportamiento del proyecto en tiempo de ejecución.
_Evitar_: configuration

**Proyeccion**:
DTO armado directamente por la consulta, con solo las columnas de sus campos (incluidos los que vienen de una relación y las colecciones). Es la forma por defecto de leer un registro o una página.
_Evitar_: mapeo, mapper, modelToDto

**Aplicar un DTO**:
Copiar los campos de un DTO sobre una entidad cargada, al guardar. Los campos que cambian por un método de negocio los completa la propia entidad.
_Evitar_: mapear, dtoToModel

**Caso de uso con consulta propia**:
Caso de uso cuya lectura no se puede resolver con una **Proyeccion** y escribe su propia consulta en el repositorio.
_Evitar_: mapper específico, caso especial

**uuid**:
Identificador público de un registro; el único que conoce el front. Término reservado: no se traduce.

**status**:
Vigencia de un registro con borrado lógico (0 activo, -1 eliminado, -2 temporal). Término reservado: no se traduce.

## Relaciones

- Una funcionalidad se descompone en uno o más **Casos de uso**.
- Un **Caso de uso** que toca varias entidades pertenece a la entidad principal, la que aparece en su URL.
- Una **Entidad chica** resuelve sus **Casos de uso** con el **ABM de una entidad**; cualquier otro caso de uso lleva endpoint propio.
- El **ABM de una entidad** lee (obtener, **Paginar**) con una **Proyeccion** y escribe (**Guardar**) aplicando el DTO; la respuesta de guardar y borrar también es una **Proyeccion**.
- Un **Usuario** tiene uno o más **Roles**; en multiempresa, cada **Rol** se le asigna dentro de una **Empresa**.
- Un **Rol** agrupa uno o más **Permisos**.
