package com.blue.open.Code.Common.Cifrado;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifra y descifra credenciales que Shiro tiene que guardar y volver a usar (el token de Jira del importador, F4).
 * Usa AES-256-GCM con una clave derivada (SHA-256) de la frase shiro.cifrado.frase (variable SHIRO_CIFRADO_FRASE) y
 * un IV aleatorio por valor; el resultado es base64 de IV + texto cifrado. core-crypter guarda archivos, no cifra
 * datos, por eso esta clase (adenda propuesta 30). Sin frase, la aplicacion arranca igual (con un WARN) y solo el
 * importador queda deshabilitado.
 * Dependencias externas: ninguna.
 */
@Slf4j
@Component
public class CifradorDeCredenciales {

    private static final String ALGORITMO = "AES/GCM/NoPadding";

    private static final int BYTES_DEL_IV = 12;

    private static final int BITS_DEL_TAG = 128;

    private final SecureRandom azar = new SecureRandom();

    // Frase de la que sale la clave. Vacia: no hay cifrado
    @Value("${shiro.cifrado.frase:}")
    private String frase;

    // Avisa al iniciar si falta la frase: el importador de Jira no va a poder guardar credenciales
    @PostConstruct
    void comprobarAlIniciar() {
        if (frase == null || frase.isBlank())
            log.warn("shiro.cifrado.frase sin configurar | el importador de Jira queda deshabilitado");
    }

    // Cifra el texto recibido. Responde base64 de IV + texto cifrado. Lanza CIFRADO_NO_CONFIGURADO sin frase
    public String cifrar(String texto) {
        try {
            byte[] iv = new byte[BYTES_DEL_IV];
            azar.nextBytes(iv);
            Cipher cifrador = Cipher.getInstance(ALGORITMO);
            cifrador.init(Cipher.ENCRYPT_MODE, clave(), new GCMParameterSpec(BITS_DEL_TAG, iv));
            byte[] cifrado = cifrador.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + cifrado.length).put(iv).put(cifrado).array());
        } catch (GeneralSecurityException e) {
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CIFRADO_NO_CONFIGURADO, "CifradorDeCredenciales | cifrar | fallo el cifrado");
        }
    }

    /*
     * Descifra un valor que dio cifrar. Lanza CIFRADO_NO_CONFIGURADO sin frase y CREDENCIAL_NO_DESCIFRABLE si el valor
     * no se cifro con la frase actual (por ejemplo, despues de cambiarla) o esta danado.
     */
    public String descifrar(String cifrado) {
        SecretKeySpec clave = clave();
        try {
            byte[] bytes = Base64.getDecoder().decode(cifrado);
            Cipher cifrador = Cipher.getInstance(ALGORITMO);
            cifrador.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(BITS_DEL_TAG, bytes, 0, BYTES_DEL_IV));
            return new String(cifrador.doFinal(bytes, BYTES_DEL_IV, bytes.length - BYTES_DEL_IV), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CREDENCIAL_NO_DESCIFRABLE,
                    "CifradorDeCredenciales | descifrar | el valor no se cifro con la frase actual");
        }
    }

    // Clave AES de 256 bits derivada de la frase. Lanza CIFRADO_NO_CONFIGURADO si no hay frase
    private SecretKeySpec clave() {
        if (frase == null || frase.isBlank())
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CIFRADO_NO_CONFIGURADO,
                    "CifradorDeCredenciales | clave | shiro.cifrado.frase sin configurar");
        try {
            return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(frase.getBytes(StandardCharsets.UTF_8)), "AES");
        } catch (GeneralSecurityException e) {
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CIFRADO_NO_CONFIGURADO, "CifradorDeCredenciales | clave | sin SHA-256");
        }
    }
}
