package com.blue.open.Code.Common.Seguridad;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registra FiltroDeLimiteDeIngreso para todos los pedidos, antes que el resto de los filtros (incluido FiltroDeMcp),
 * para que un bloqueo corte el pedido antes de autenticar y para ver el 401 de cada intento fallido (MJ-04).
 * Dependencias externas: ninguna.
 */
@Configuration
public class LimiteDeIngresoConfig {

    // Filtro del limite de intentos, con la mayor precedencia
    @Bean
    public FilterRegistrationBean<FiltroDeLimiteDeIngreso> filtroDeLimiteDeIngreso(LimiteDeIntentosDeIngreso limite) {
        FilterRegistrationBean<FiltroDeLimiteDeIngreso> registro = new FilterRegistrationBean<>(new FiltroDeLimiteDeIngreso(limite));
        registro.addUrlPatterns("/*");
        registro.setName("filtroDeLimiteDeIngreso");
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }
}
