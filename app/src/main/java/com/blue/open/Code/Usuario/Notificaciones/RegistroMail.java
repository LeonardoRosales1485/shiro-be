package com.blue.open.Code.Usuario.Notificaciones;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Notificacion.NotificacionPorMail;

/**
 * Email con el codigo de verificacion que recibe un usuario al registrarse por un alta publica.
 * Dependencias externas: ninguna (el envio lo hace core-mail). Recibe: el usuario, el codigo y la
 * URL del front. Responde: asunto y cuerpo HTML armados desde Registro/codigo-de-verificacion.html.
 */
public class RegistroMail implements NotificacionPorMail {
    private static final String PLANTILLA = "Registro/codigo-de-verificacion.html";

    private final UsuarioBase usuario;
    // Codigo de verificacion que el usuario ingresa en el front
    private final String codigo;
    private final String urlDelFront;

    // Recibe el usuario destinatario, el codigo de verificacion y la URL base del front
    public RegistroMail(UsuarioBase usuario, String codigo, String urlDelFront) {
        this.usuario = usuario;
        this.codigo = codigo;
        this.urlDelFront = urlDelFront;
    }

    // Asunto del email
    @Override
    public String getAsunto() {
        return "Código de Verificación - Blue Open Data";
    }

    // Cuerpo HTML: la plantilla con la URL del front, el usuario y el codigo reemplazados
    @Override
    public String getCuerpo() {
        String html;
        // Plantilla relativa a esta clase: Maven la empaqueta junto al .class (ver <resources> en app/pom.xml)
        try (InputStream plantilla = getClass().getResourceAsStream(PLANTILLA)) {
            if (plantilla == null) {
                throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.ERROR_GENERANDO_EMAIL,
                        getClass().getSimpleName() + " | getCuerpo | No se encontro la plantilla " + PLANTILLA);
            }
            html = new String(plantilla.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.ERROR_GENERANDO_EMAIL,
                    getClass().getSimpleName() + " | getCuerpo | " + e.getMessage(), e);
        }

        return html.replace("{urlDelFront}", urlDelFront != null ? urlDelFront : "")
                .replace("{usuario}", usuario.getNombreDeUsuario())
                .replace("{codigo}", codigo);
    }
}
