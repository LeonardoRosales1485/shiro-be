package com.blue.open.Code.Common.Autenticacion;

import com.blue.open.Core.Protocolo.ApiKey.AuthTokenService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Saca del contexto el bean AuthTokenService de core-protocolo-apikey 4.0.0 (tokens en texto plano) para que el
 * protocolo "tokens" lo resuelva solo AuthTokenConSha256Service (T2.1). Es el paso "puerto" de la escalera de
 * extension del Core: se reemplaza el bean, no se modifica el Core. Se borra cuando el Core publique 4.1.0.
 * Dependencias externas: ninguna.
 */
@Configuration
public class AuthTokenConSha256Config {

    // Nombre del bean del Core que se reemplaza
    private static final String BEAN_DEL_CORE = "authTokenService";

    // Quita la definicion del bean del Core si es exactamente la clase AuthTokenService (no una subclase)
    @Bean
    public static BeanDefinitionRegistryPostProcessor quitarAuthTokenServiceDelCore() {
        return (BeanDefinitionRegistry registro) -> {
            if (!registro.containsBeanDefinition(BEAN_DEL_CORE)) {
                return;
            }
            BeanDefinition definicion = registro.getBeanDefinition(BEAN_DEL_CORE);
            if (AuthTokenService.class.getName().equals(definicion.getBeanClassName())) {
                registro.removeBeanDefinition(BEAN_DEL_CORE);
            }
        };
    }
}
