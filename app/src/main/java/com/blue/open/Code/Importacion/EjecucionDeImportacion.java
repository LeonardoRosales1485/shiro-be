package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Corre una importacion en segundo plano (en el ejecutorDeImportaciones) y, si no termina, la deja FALLIDA con el
 * motivo en el resumen: un error de Jira o de credenciales, con su mensaje para el usuario; cualquier otro error, con
 * un mensaje generico y el detalle en el log.
 * Dependencias externas: Jira y la base de datos (a traves de ImportadorDeJira).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EjecucionDeImportacion {

    private final ImportadorDeJira importador;

    private final ImportacionRepository importacionRepository;

    private final ObjectMapper lector = new ObjectMapper();

    // Importa y, si falla, marca la importacion FALLIDA. No lanza: corre fuera del request
    public void correr(Long idImportacion, String uuid, Long idUsuario) {
        try {
            importador.importar(idImportacion, idUsuario);
            log.info("Importacion de Jira terminada | uuid={}", uuid);
        } catch (ExcepcionDeApi e) {
            log.warn("Importacion de Jira fallida | uuid={} | codigo={}", uuid, e.getCodigo().codigo());
            fallida(idImportacion, e.getCodigo().codigo(), e.getMensajeParaUsuario());
        } catch (RuntimeException e) {
            log.error("Importacion de Jira fallida por un error inesperado | uuid={}", uuid, e);
            fallida(idImportacion, "ERROR_INTERNO", "La importación se cortó por un error inesperado. Volvé a intentar.");
        }
    }

    // Deja la importacion FALLIDA con un resumen que solo tiene la falla general
    private void fallida(Long idImportacion, String codigo, String motivo) {
        ResumenDeImportacion resumen = new ResumenDeImportacion();
        resumen.fallo(codigo, motivo);
        importacionRepository.marcarFallida(idImportacion, lector.writeValueAsString(resumen));
    }
}
