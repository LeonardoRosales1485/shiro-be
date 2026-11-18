# Captcha

**Clase**: `com.blue.open.Core.Integracion.Captcha.CaptchaService` (módulo `core-captcha`)

Verifica un token de hCaptcha contra `https://hcaptcha.com/siteverify`.

## Configuración

`application.properties`: `captcha.secreto`, `captcha.habilitado` (default `true`). Si `captcha.habilitado=false`, `verificar(...)` no hace nada (útil para deshabilitar en desarrollo/tests). `estaConfigurada()` permite chequear en un smoke test de arranque si está configurado.

## Cómo usarlo

```java
private final CaptchaService captchaService;

captchaService.verificar(token); // lanza ExcepcionDeApi si el captcha no es válido
```
