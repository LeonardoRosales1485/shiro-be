package com.blue.open.Code.Common.Errores;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeError;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

// Codigos de error propios del proyecto (no del Core). Todos los codigos del proyecto
// comparten este unico enum, sin importar de que entidad vengan.
@AllArgsConstructor
public enum CodigoDeErrorDelProyecto implements CodigoDeError {

    @Descripcion("Al guardar una empresa o en el registro público de usuario y empresa, el CUIT no es válido (dígito verificador incorrecto)")
    CUIT_INVALIDO(HttpStatus.BAD_REQUEST, "CUIT inválido"),
    @Descripcion("Al dar de alta un usuario, el email ya lo usa otro usuario activo o pendiente de verificación")
    EMAIL_YA_REGISTRADO(HttpStatus.BAD_REQUEST, "Email ya registrado"),
    @Descripcion("Se piden varias configuraciones y alguna es privada sin tener permiso para verlas. El mensaje nombra las configuraciones privadas")
    CONFIGURACIONES_PRIVADAS(HttpStatus.FORBIDDEN, "No tenes permiso para ver estas variables"),
    @Descripcion("Al crear un proyecto, el prefijo ya lo usa otro proyecto, vigente o archivado. Hay que elegir otro")
    PREFIJO_DE_PROYECTO_EN_USO(HttpStatus.BAD_REQUEST, "El prefijo ya lo usa otro proyecto. Elegí otro."),
    @Descripcion("Al editar un proyecto se mandó un prefijo distinto del actual: el prefijo no se cambia después de crear el proyecto")
    PREFIJO_DE_PROYECTO_NO_SE_CAMBIA(HttpStatus.BAD_REQUEST, "El prefijo no se cambia: lo usan las claves de las tareas existentes."),
    @Descripcion("Al crear o editar una tarea se mandó un código de tipo que no existe (los tipos son EPICA, HISTORIA, TAREA, BUG y SUBTAREA)")
    TIPO_DE_TAREA_INEXISTENTE(HttpStatus.BAD_REQUEST, "El tipo de tarea no existe."),
    @Descripcion("Al editar una tarea se mandó un tablero distinto del actual: una tarea no cambia de tablero, se clona")
    TABLERO_DE_TAREA_NO_SE_CAMBIA(HttpStatus.BAD_REQUEST, "Una tarea no cambia de tablero. Para llevarla a otro tablero, clonala."),
    @Descripcion("Al crear o editar una tarea, el tipo no encaja en la jerarquía: el padre tiene que ser de un nivel menor (Épica, después Historia, Tarea o Bug, después Subtarea), y las hijas, de un nivel mayor")
    JERARQUIA_DE_TAREA_INVALIDA(HttpStatus.BAD_REQUEST, "El tipo de la tarea no encaja en la jerarquía."),
    @Descripcion("Al crear o editar una tarea se eligió como padre una tarea de otro proyecto")
    PADRE_DE_OTRO_PROYECTO(HttpStatus.BAD_REQUEST, "La tarea padre tiene que ser del mismo proyecto."),
    @Descripcion("Al mover una tarea se eligió un estado que no es del tablero de la tarea: una tarea no cambia de tablero, se clona")
    ESTADO_DE_OTRO_TABLERO(HttpStatus.BAD_REQUEST, "El estado no es del tablero de la tarea. Para llevarla a otro tablero, clonala."),
    @Descripcion("Al editar una tarea, otra persona o agente la modificó después de que el cliente la leyó: hay que recargarla y volver a intentar")
    TAREA_MODIFICADA(HttpStatus.CONFLICT, "La tarea cambió mientras la editabas. Recargá y volvé a intentar."),
    @Descripcion("Al editar una tarea no se mandó fechaDeActualizacion, la fecha de la última modificación que se leyó")
    FECHA_DE_LECTURA_OBLIGATORIA(HttpStatus.BAD_REQUEST, "Falta la fecha de actualización que se leyó. Recargá la tarea y volvé a intentar."),
    @Descripcion("Al restaurar una tarea, su padre sigue archivado: hay que restaurar primero el padre (su clave va en datos.padreClave)")
    PADRE_ARCHIVADO(HttpStatus.BAD_REQUEST, "Restaurá primero la tarea padre."),
    @Descripcion("Al relacionar tareas se eligió la misma tarea como origen y destino")
    RELACION_CONSIGO_MISMA(HttpStatus.BAD_REQUEST, "Una tarea no se relaciona consigo misma."),
    @Descripcion("Al relacionar tareas, ya existe una relación vigente del mismo tipo entre ellas, en cualquier sentido")
    RELACION_YA_EXISTE(HttpStatus.BAD_REQUEST, "Esas tareas ya tienen esa relación."),
    @Descripcion("Al relacionar tareas se pidió el tipo CLONA: esa relación solo la crea clonar una tarea")
    TIPO_DE_RELACION_NO_PERMITIDO(HttpStatus.BAD_REQUEST, "La relación de clon se crea al clonar la tarea."),
    @Descripcion("Al clonar una tarea se eligió un tablero de otro proyecto: se clona a tableros del mismo proyecto")
    TABLERO_DE_OTRO_PROYECTO(HttpStatus.BAD_REQUEST, "El tablero de destino tiene que ser del mismo proyecto."),
    @Descripcion("Al relacionar tareas se eligieron tareas de proyectos distintos: las relaciones son dentro de un proyecto")
    RELACION_ENTRE_PROYECTOS(HttpStatus.BAD_REQUEST, "Solo se relacionan tareas del mismo proyecto."),
    @Descripcion("Al ingresar con Google, el ID token no verifica: firma, emisor, audiencia, vencimiento o correo sin verificar. Hay que volver a iniciar sesión con Google")
    TOKEN_DE_GOOGLE_INVALIDO(HttpStatus.UNAUTHORIZED, "No se pudo validar tu cuenta de Google. Volvé a intentar."),
    @Descripcion("Al ingresar con Google, la cuenta no es del dominio de la organización")
    DOMINIO_DE_GOOGLE_NO_PERMITIDO(HttpStatus.FORBIDDEN, "Solo pueden ingresar cuentas de Google de la organización."),
    @Descripcion("El ingreso con Google no está configurado en el servidor (falta el client id o el dominio): se ingresa con usuario y contraseña")
    GOOGLE_NO_CONFIGURADO(HttpStatus.SERVICE_UNAVAILABLE, "El ingreso con Google no está habilitado. Ingresá con tu usuario y contraseña."),
    @Descripcion("Al ingresar con Google, no se pudieron obtener las claves públicas de Google: hay que reintentar más tarde")
    GOOGLE_NO_DISPONIBLE(HttpStatus.SERVICE_UNAVAILABLE, "Google no responde. Volvé a intentar en unos minutos."),
    @Descripcion("Al guardar una regla de permiso, el sujeto no coincide con tipoSujeto (va rolUuid o usuarioUuid, uno solo) o el recurso no coincide con tipoRecurso (va proyectoUuid o tableroUuid, uno solo)")
    REGLA_DE_PERMISO_INVALIDA(HttpStatus.BAD_REQUEST, "La regla tiene que indicar un sujeto y un recurso, según su tipo."),
    @Descripcion("Al guardar una regla de permiso, ya hay otra vigente con el mismo sujeto, recurso y nivel: hay que editar esa")
    REGLA_DE_PERMISO_REPETIDA(HttpStatus.BAD_REQUEST, "Ya hay una regla para ese sujeto, recurso y nivel. Editá esa."),
    @Descripcion("Al archivar un usuario se eligió el propio usuario de la sesión: nadie se archiva a sí mismo")
    USUARIO_NO_SE_ARCHIVA_A_SI_MISMO(HttpStatus.BAD_REQUEST, "No podés archivar tu propio usuario."),
    @Descripcion("Al crear o editar un grupo, ya hay otro vigente con ese nombre")
    GRUPO_REPETIDO(HttpStatus.BAD_REQUEST, "Ya hay un grupo con ese nombre."),
    @Descripcion("Se intentó archivar un grupo del sistema (SYSADMIN o ADMIN), que no se archiva")
    GRUPO_DEL_SISTEMA(HttpStatus.BAD_REQUEST, "Los grupos del sistema no se archivan."),
    @Descripcion("Al ordenar el backlog se mandó una tarea que no está en el backlog del proyecto (de otro proyecto, archivada, con sprint, épica o subtarea)")
    TAREA_FUERA_DEL_BACKLOG(HttpStatus.BAD_REQUEST, "Una de las tareas no está en el backlog del proyecto."),
    @Descripcion("Al guardar un sprint, la fecha de fin es anterior a la de inicio")
    SPRINT_FECHAS_INVALIDAS(HttpStatus.BAD_REQUEST, "La fecha de fin del sprint no puede ser anterior a la de inicio."),
    @Descripcion("Al iniciar un sprint, el proyecto ya tiene otro activo: hay que cerrarlo primero")
    SPRINT_ACTIVO_EXISTENTE(HttpStatus.BAD_REQUEST, "Ya hay un sprint activo en el proyecto. Cerralo antes de iniciar otro."),
    @Descripcion("Se intentó iniciar un sprint que no está planificado (ya está activo o cerrado)")
    SPRINT_NO_PLANIFICADO(HttpStatus.BAD_REQUEST, "Solo se inicia un sprint planificado."),
    @Descripcion("Se intentó cerrar un sprint que no está activo")
    SPRINT_NO_ACTIVO(HttpStatus.BAD_REQUEST, "Solo se cierra el sprint activo."),
    @Descripcion("Se intentó asignar o quitar tareas de un sprint cerrado")
    SPRINT_CERRADO(HttpStatus.BAD_REQUEST, "El sprint está cerrado: no se le asignan ni quitan tareas."),
    @Descripcion("Al cerrar un sprint hacia otro sprint, el destino no es un sprint planificado del mismo proyecto")
    SPRINT_DESTINO_INVALIDO(HttpStatus.BAD_REQUEST, "El sprint de destino tiene que ser un sprint planificado del mismo proyecto."),
    @Descripcion("Se intentó archivar el sprint activo: hay que cerrarlo primero")
    SPRINT_ACTIVO_NO_SE_ARCHIVA(HttpStatus.BAD_REQUEST, "El sprint activo no se archiva: cerralo primero."),
    @Descripcion("Se intentó cambiar una versión publicada (editarla, asignarle o quitarle tareas, o publicarla otra vez): una versión publicada no cambia")
    VERSION_PUBLICADA(HttpStatus.BAD_REQUEST, "La versión está publicada y no cambia."),
    @Descripcion("Se mandó una tarea de otro proyecto (o archivada) para una versión")
    TAREA_DE_OTRO_PROYECTO(HttpStatus.BAD_REQUEST, "La tarea tiene que ser del mismo proyecto."),
    @Descripcion("No se pudo armar el email de registro o de recuperación de contraseña. Es un error del backend")
    ERROR_GENERANDO_EMAIL(HttpStatus.INTERNAL_SERVER_ERROR, "Error al generar el email");

    private final HttpStatus status;
    private final String mensajePorDefecto;

    @Override
    public String codigo() {
        return name();
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String mensajePorDefecto() {
        return mensajePorDefecto;
    }
}
