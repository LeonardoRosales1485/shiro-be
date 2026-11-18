package com.blue.open;

import com.blue.open.Core.Kernel.Configuracion.FechaHoraConfig;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/*
 * Punto de entrada del proyecto. Fuerza la zona horaria GMT-3 antes de levantar Spring.
 *
 * El escaneo apunta a com.blue.open completo: asi los modulos del Core que esten declarados como
 * dependencia (core-identity, core-multiempresa, core-protocolo-*, integraciones) se registran
 * solos, y los que no esten simplemente no existen. Agregar o quitar un modulo del Core es agregar
 * o quitar su dependencia en el pom, sin tocar esta clase.
 *
 * app-version.properties trae app.version, que Maven completa con el <version> de app/pom.xml. El Core la expone en
 * /info y en el header API-Version (ProveedorDeVersion por defecto de core-kernel).
 */
@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableScheduling
@PropertySource("classpath:app-version.properties")
@EnableJpaRepositories(basePackages = {
        "com.blue.open.Code",
        "com.blue.open.Core",
}, repositoryBaseClass = EntidadRepositoryBase.class)
@EntityScan(basePackages = {
        "com.blue.open.Code",
        "com.blue.open.Core",
})
public class Application {

    // Punto de entrada: fuerza la zona horaria GMT-3 antes de levantar Spring y despues imprime
    // la ayuda para importar el certificado en el JDK.
    public static void main(String[] args) {
        FechaHoraConfig.forzarZonaHorariaPorDefecto();
        log.info("TimeZone forzado | zona={}", FechaHoraConfig.ZONA_HORARIA.getID());
        SpringApplication.run(Application.class, args);

        log.info("Java Home | ruta={}", System.getProperty("java.home"));
        log.info("Importar certificado. Ejecutar en powershell: & \"{}\\bin\\keytool.exe\" -importcert -trustcacerts"
                        + " -cacerts -storepass changeit -noprompt -alias sso-malvinas -file \"{}\\sso-malvinas.crt\"",
                System.getProperty("java.home"), System.getProperty("user.dir"));

        log.info("Iniciado correctamente | fecha={}", FechaHoraConfig.ahoraDate());
    }
}
