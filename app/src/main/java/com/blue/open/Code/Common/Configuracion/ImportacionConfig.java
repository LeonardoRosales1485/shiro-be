package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Trazabilidad.InicializadorDeTraceIdGlobal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Ejecutor de las importaciones de Jira (Spec 6: corren en segundo plano). Un hilo y una cola chica: una importacion
 * es larga y no conviene que varias compitan. Con shiro.importacion.sincronica=true (las pruebas de contrato) corre
 * en el hilo del request, dentro de su transaccion, para que se pueda verificar y revertir.
 * Dependencias externas: ninguna.
 */
@Configuration
public class ImportacionConfig {

    // Ejecutor "ejecutorDeImportaciones": asincronico de un hilo, o sincronico si la propiedad lo pide
    @Bean("ejecutorDeImportaciones")
    public TaskExecutor ejecutorDeImportaciones(@Value("${shiro.importacion.sincronica:false}") boolean sincronica,
            InicializadorDeTraceIdGlobal inicializadorDeTraceIdGlobal) {
        if (sincronica)
            return new SyncTaskExecutor();
        ThreadPoolTaskExecutor ejecutor = new ThreadPoolTaskExecutor();
        ejecutor.setCorePoolSize(1);
        ejecutor.setMaxPoolSize(1);
        ejecutor.setQueueCapacity(20);
        ejecutor.setThreadNamePrefix("Importacion-Jira-");
        ejecutor.setTaskDecorator(inicializadorDeTraceIdGlobal);
        return ejecutor;
    }
}
