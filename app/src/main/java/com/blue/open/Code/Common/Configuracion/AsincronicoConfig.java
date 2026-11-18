package com.blue.open.Code.Common.Configuracion;

import com.blue.open.Core.Kernel.Trazabilidad.InicializadorDeTraceIdGlobal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Pool de hilos para metodos @Async ("EjecutorAsincronico"). Propaga el traceId de la request al
 * hilo asincronico para que la trazabilidad del Core siga el flujo completo.
 */
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsincronicoConfig {

    private final InicializadorDeTraceIdGlobal inicializadorDeTraceIdGlobal;

    // Ejecutor de los metodos anotados con @Async("EjecutorAsincronico"): 4 hilos y cola de 150 tareas
    @Bean("EjecutorAsincronico")
    public Executor ejecutorAsincronico() {
        ThreadPoolTaskExecutor ejecutor = new ThreadPoolTaskExecutor();
        ejecutor.setCorePoolSize(4);
        ejecutor.setQueueCapacity(150);
        ejecutor.setMaxPoolSize(4);
        ejecutor.setThreadNamePrefix("Hilo-Asincronico");
        ejecutor.setTaskDecorator(inicializadorDeTraceIdGlobal);
        ejecutor.initialize();
        return ejecutor;
    }
}
