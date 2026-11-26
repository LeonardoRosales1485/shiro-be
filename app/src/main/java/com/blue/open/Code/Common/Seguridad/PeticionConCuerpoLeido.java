package com.blue.open.Code.Common.Seguridad;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Pedido cuyo cuerpo ya se leyo (hasta un maximo) para mirarlo antes del endpoint y que el endpoint lo pueda volver a
 * leer. Lo usa FiltroDeLimiteDeIngreso para sacar el usuario de iniciar sesion. Si el cuerpo pasa el maximo, se lee
 * igual completo para el endpoint, pero cuerpo() responde null.
 * Dependencias externas: ninguna.
 */
class PeticionConCuerpoLeido extends HttpServletRequestWrapper {

    private final byte[] contenido;

    private final boolean dentroDelMaximo;

    // Lee el cuerpo completo del pedido; maximo es el largo hasta el que cuerpo() lo devuelve
    PeticionConCuerpoLeido(HttpServletRequest pedido, int maximo) throws IOException {
        super(pedido);
        this.contenido = pedido.getInputStream().readAllBytes();
        this.dentroDelMaximo = contenido.length <= maximo;
    }

    // Cuerpo leido, o null si pasa el maximo
    byte[] cuerpo() {
        return dentroDelMaximo ? contenido : null;
    }

    // El cuerpo leido, para que el endpoint lo vuelva a leer
    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream entrada = new ByteArrayInputStream(contenido);
        return new ServletInputStream() {
            // Si ya se leyo todo el cuerpo
            @Override
            public boolean isFinished() {
                return entrada.available() == 0;
            }

            // El cuerpo esta en memoria: siempre se puede leer
            @Override
            public boolean isReady() {
                return true;
            }

            // Sin lectura asincronica
            @Override
            public void setReadListener(ReadListener oyente) {
                throw new UnsupportedOperationException("El cuerpo ya esta leido: no hay lectura asincronica");
            }

            // Lee un byte del cuerpo
            @Override
            public int read() {
                return entrada.read();
            }

            // Lee varios bytes del cuerpo
            @Override
            public int read(byte[] destino, int desde, int largo) {
                return entrada.read(destino, desde, largo);
            }
        };
    }

    // El cuerpo leido como texto, con la codificacion del pedido (UTF-8 si no la declara)
    @Override
    public BufferedReader getReader() {
        String codificacion = getCharacterEncoding();
        Charset juego = codificacion == null ? StandardCharsets.UTF_8 : Charset.forName(codificacion);
        return new BufferedReader(new InputStreamReader(getInputStream(), juego));
    }
}
