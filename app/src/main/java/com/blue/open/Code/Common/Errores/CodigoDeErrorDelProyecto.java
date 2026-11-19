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
