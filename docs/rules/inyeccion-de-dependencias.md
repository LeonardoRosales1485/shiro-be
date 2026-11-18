# Inyección de Dependencias

Este documento define cómo una clase recibe sus dependencias. Es de cumplimiento obligatorio y aplica tanto a `Code` como al Core.

## Regla principal

**Está prohibido anotar un campo con `@Autowired`.** No se declara una dependencia así:

```java
// ! PROHIBIDO: inyeccion por campo
@Service
public class ProductoService {

    @Autowired
    private ProductoRepository repository;
}
```

Motivos:

- El bean se puede construir sin sus dependencias, y falla recién al usarlo (`NullPointerException`) en vez de al arrancar.
- El campo no puede ser `final`: cualquiera lo puede reemplazar después de construido.
- Oculta el exceso de dependencias. Un constructor con 9 parámetros avisa que la clase hace demasiado; 9 campos `@Autowired` no avisan nada.
- Oculta las dependencias circulares. Con inyección por campo, un ciclo A → B → A arranca igual y aparece como un bug raro en runtime; con constructor, Spring lo corta al arrancar.
- Obliga a inyectar los mocks por reflexión (`ReflectionTestUtils.setField`) en vez de construir la clase con `new`.

## Cómo se declara una dependencia

### Caso por defecto: clase concreta → constructor

Toda clase concreta (`Service`, `Controller`, `UseCase`, `Repository` propio, filtro, aspecto, configuración) recibe sus dependencias **por constructor**, con los campos `private final` y `@RequiredArgsConstructor` de Lombok:

```java
@Service
@RequiredArgsConstructor
public class ProductoActualizarStockUseCase {

    private final EntidadRepository<Producto> repository;

    private final Proyector proyector;
}
```

- Los campos son `private final`. Nunca `protected` ni `public`.
- No se escribe el constructor a mano: lo genera `@RequiredArgsConstructor` con todos los campos `final` sin inicializar.
- No se anota `@Autowired` sobre el constructor: con un único constructor, Spring lo usa solo.
- Si la clase ya tiene un constructor propio (porque calcula algo al construirse), se le agrega el parámetro ahí y no se usa Lombok. Ejemplo: `Code/Common/WebSocket/WebSocketConfig.java`.

### Excepción: clase base que extienden los proyectos → setter

Las clases base del Core que extiende un proyecto (`AbstractService`, `UsuarioBaseService`, `UsuarioBaseMultiempresaService`, `EmpresaBaseService`, `RolBaseService`) reciben sus dependencias **por setter público anotado con `@Autowired`**:

```java
public abstract class AbstractService<T, DTO, PAGINADODTO> {

    protected EntidadRepository<T> repository;

    // Recibe el repositorio de la entidad concreta
    @Autowired
    public void setRepository(EntidadRepository<T> repository) {
        this.repository = repository;
    }
}
```

Motivo: con constructor, **toda** subclase estaría obligada a declarar un constructor y encadenar `super(...)` con las dependencias de cada nivel de la jerarquía, y agregar una dependencia a una clase base rompería todos los proyectos que la extienden. Con setter, la subclase no declara nada:

```java
@Service
public class ProductoService extends AbstractService<Producto, ProductoDTO, ProductoPaginateDTO> {
    // Sin constructor: las dependencias de la base las inyecta Spring por setter
}
```

Condiciones de la excepción:

- Solo aplica a clases base **del Core**. Una clase de `Code` no usa inyección por setter: si dos entidades comparten lógica, se extrae a un `UseCase` o a un bean propio, no a una clase base.
- El campo queda `protected` (lo usan las subclases) y sin `final`.
- El setter es `public`, para que los test puedan inyectar los mocks sin reflexión.
- Cada setter lleva su comentario, igual que toda función (ver [idioma-y-estilo.md](idioma-y-estilo.md)).
- Si una clase base nueva no la extiende ningún proyecto, no aplica la excepción: va por constructor.

### Dependencias opcionales → `ObjectProvider`

Una dependencia que puede no existir (porque el módulo del Core que la publica es opcional) **no** se declara con `@Autowired(required = false)`: se declara como `ObjectProvider<T>` y se resuelve con `getIfAvailable()`:

```java
// ObjectProvider: la dependencia es opcional (el proyecto puede no declarar ese modulo)
private final ObjectProvider<ProveedorDeConfiguracion> proveedorDeConfiguracion;

private boolean debugHabilitado() {
    ProveedorDeConfiguracion proveedor = proveedorDeConfiguracion.getIfAvailable();
    if (proveedor == null) {
        return false;
    }
    // ...
}
```

Lo mismo para una lista de implementaciones que puede estar vacía (`ObjectProvider<T>` es iterable): un `List<T>` por constructor falla al arrancar si no hay ningún candidato. Ejemplo: `AuthService.estrategias`, que está vacío en un proyecto sin ningún módulo `core-protocolo-*`.

## Qué no cambia

- **`@Value` sobre un campo se mantiene.** La regla prohíbe `@Autowired` sobre campos, no `@Value`: una property no es una dependencia del grafo de beans, no genera ciclos y se sigue leyendo bien desde un campo.
- **`@Autowired` en clases de test de integración se mantiene.** En un `@SpringBootTest` es la forma estándar de obtener un bean del contexto (ver `app/src/test/java/com/blue/open/SecurityIntegrationIT.java`).

## Dependencias circulares

Un ciclo entre beans (A necesita B, B necesita A) con inyección por constructor falla al arrancar con `BeanCurrentlyInCreationException`. **Eso es lo correcto: el ciclo es un problema de diseño, y la solución por defecto es romperlo**, moviendo la lógica compartida a un tercer bean.

Solo cuando el ciclo no se puede romper sin partir un módulo del Core, se anota el parámetro con `@Lazy` y se deja un comentario explicando el ciclo:

```java
// Lazy: AuthService -> ResolutorDePermisosMultiempresa -> ContextoDeEmpresa -> AuthService es circular
@Lazy
private final AuthService authService;
```

`lombok.config` en la raíz del repositorio declara `@Lazy`, `@Qualifier` y `@Value` como anotaciones copiables: sin eso, Lombok genera el constructor sin ellas y Spring no resuelve la dependencia.

## Testing

Ver [testing.md](testing.md). Como consecuencia de esta regla:

- Una clase concreta se construye en el test con `new`, pasando los mocks al constructor. No se usa `@InjectMocks` ni `ReflectionTestUtils.setField`.
- Una clase base del Core se construye con `new` y se le inyectan los mocks llamando a sus setters.
- `ReflectionTestUtils.setField` queda únicamente para los campos `@Value`.
