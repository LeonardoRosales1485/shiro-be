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
