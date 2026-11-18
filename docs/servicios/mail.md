# Mail

**Clases**: `com.blue.open.Core.Kernel.Puertos.EnviadorDeMail` (puerto, `core-kernel`), implementado por `com.blue.open.Core.Integracion.Mail.EnviadorDeMailSmtp` (módulo `core-mail`); `com.blue.open.Core.Kernel.Notificacion.NotificacionPorMail`

Envío de emails vía SMTP. El contenido de cada email se define implementando la interfaz `NotificacionPorMail` (una clase por tipo de email), no armando el asunto/cuerpo inline en el service.

## Configuración

`application.properties`: `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password` (protocolo/auth/starttls tienen default) y `mail.opcion` (default `smtp`; `notify` todavía no está implementado). `estaConfigurada()` permite chequear en un smoke test de arranque si está configurado.

## Cómo usarlo

1. Crear una clase que implemente `NotificacionPorMail`, con el asunto y el cuerpo (HTML soportado). Ejemplo real: `Code/Usuario/Notificaciones/RegistroMail.java`. La plantilla HTML vive junto a la clase (`Registro/codigo-de-verificacion.html`) y se carga con `getClass().getResourceAsStream(...)`; `app/pom.xml` empaqueta los `.html` de `src/main/java`.
   ```java
   public class RegistroMail implements NotificacionPorMail {
       private final UsuarioBase usuario;
       private final String codigo;

       public RegistroMail(UsuarioBase usuario, String codigo) {
           this.usuario = usuario;
           this.codigo = codigo;
       }

       @Override
       public String getAsunto() { return "Código de Verificación - ..."; }

       @Override
       public String getCuerpo() { /* arma el HTML, puede leer una plantilla */ }
   }
   ```
2. Inyectar el puerto `EnviadorDeMail` y enviar (sin `core-mail` no hay bean: usar `ObjectProvider` si el envío es opcional):
   ```java
   private final EnviadorDeMail enviadorDeMail;

   NotificacionPorMail mail = new RegistroMail(usuario, codigo);
   enviadorDeMail.enviar(usuario.getEmail(), mail.getAsunto(), mail.getCuerpo());
   ```
- `enviar(para, asunto, cuerpo)` — sin adjuntos.
- `enviar(para, asunto, cuerpo, Map<String, byte[]> adjuntos)` — con adjuntos (nombre de archivo -> bytes).

Ver también `Code/Usuario/Notificaciones/RecuperarContrasenaMail.java` y cómo `UsuarioService.hookObtenerNotificacionPorMail()` elige la plantilla que usan `UsuarioBaseService.enviarMailDeVerificacion()`/`enviarMailDeRecuperacion()`.
