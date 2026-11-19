package com.blue.open.Code.Common.Mcp;

import com.blue.open.Code.Usuario.AuthTokenConSha256Service;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra FiltroDeMcp solo para /mcp (relativo al context-path /api). El resto de la API sigue autorizando con
 * @Contrato, como el Core.
 * Dependencias externas: ninguna.
 */
@Configuration
public class FiltroDeMcpConfig {

    // Filtro de /mcp con el protocolo tokens
    @Bean
    public FilterRegistrationBean<FiltroDeMcp> filtroDeMcp(AuthTokenConSha256Service tokens) {
        FilterRegistrationBean<FiltroDeMcp> registro = new FilterRegistrationBean<>(new FiltroDeMcp(tokens));
        registro.addUrlPatterns("/mcp", "/mcp/*");
        registro.setName("filtroDeMcp");
        return registro;
    }
}
