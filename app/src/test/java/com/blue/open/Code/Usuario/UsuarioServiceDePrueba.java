package com.blue.open.Code.Usuario;

import com.blue.open.Code.Common.Configuracion.UrlsDelFrontProperties;
import com.blue.open.Code.Empresa.ValidadorDeCuitService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Doble de UsuarioService para las pruebas de contrato (profile "test"): los codigos de verificacion (6 caracteres) y de
 * recuperacion de contrasena (32) son fijos en vez de aleatorios. El codigo de recuperacion viaja en el request de
 * POST /usuario/publico/cambiar-contrasena: con un valor fijo el ejemplo publicado en docs/api es igual en cada corrida
 * (-Dcontratos.verificar), y la prueba lo conoce sin leer el mail. El resto del comportamiento es el de UsuarioService.
 * Dependencias externas: las de UsuarioService (front.url para los mails).
 */
@Primary
@Profile("test")
@Service
public class UsuarioServiceDePrueba extends UsuarioService {

    // Alfabeto del codigo fijo: el codigo de largo N son sus primeros N caracteres
    private static final String BASE = "CODIGODEPRUEBA0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // Recibe las dependencias de UsuarioService
    public UsuarioServiceDePrueba(UrlsDelFrontProperties urlsDelFront, ValidadorDeCuitService validadorDeCuitService,
            UsuarioRepository usuarioRepository) {
        super(urlsDelFront, validadorDeCuitService, usuarioRepository);
    }

    // Codigo fijo del largo recibido (6 = verificacion, 32 = recuperacion de contrasena)
    public static String codigo(int largo) {
        return BASE.substring(0, largo);
    }

    // Responde el codigo fijo del largo pedido
    @Override
    protected String generarAlfanumericoAleatorio(int largo) {
        return codigo(largo);
    }
}
