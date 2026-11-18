# Los endpoints se diseñan desde los casos de uso, no desde la entidad

Antes, una funcionalidad se planificaba desde la entidad: se diseñaba primero el modelo y cada entidad nacía con controller, service, repository, mapper y un DTO con todos sus campos, publicando los cuatro endpoints heredados. Eso transportaba más datos de los que el front necesitaba y obligaba a crear capas vacías para entidades que nunca se exponen solas. Ahora se listan primero los casos de uso, de cada uno se derivan su endpoint y sus DTOs, y la entidad se diseña como el modelo de persistencia que los cubre; solo el `@Entity` y el `Repository` son obligatorios.

Los endpoints heredados se conservan como default para las entidades chicas (todos sus casos de uso son un ABM que usa todos sus campos), porque para ese caso ya resuelven todo sin código propio. Se descartó eliminarlos para código nuevo: habría obligado a escribir un `UseCase` por cada ABM trivial.
