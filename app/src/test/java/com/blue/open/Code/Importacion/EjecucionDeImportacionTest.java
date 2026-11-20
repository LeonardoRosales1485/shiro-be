package com.blue.open.Code.Importacion;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Ejecucion en segundo plano de una importacion (T4.3): si el importador termina no toca el estado; si falla, deja
 * la importacion FALLIDA con el motivo en el resumen (el mensaje para el usuario de un error de la API, o uno
 * generico ante un error inesperado) y no lanza.
 */
class EjecucionDeImportacionTest {

    private final ImportadorDeJira importador = mock(ImportadorDeJira.class);

    private final ImportacionRepository repository = mock(ImportacionRepository.class);

    private final EjecucionDeImportacion ejecucion = new EjecucionDeImportacion(importador, repository);

    @Test
    void siTerminaNoLaMarcaFallida() {
        ejecucion.correr(1L, "uuid", 2L);

        verify(importador).importar(1L, 2L);
        verify(repository, never()).marcarFallida(eq(1L), anyString());
    }

    @Test
    void unErrorDeJiraQuedaEnElResumen() {
        doThrow(new ExcepcionDeApi(CodigoDeErrorDelProyecto.JIRA_CREDENCIALES_INVALIDAS)).when(importador).importar(1L, 2L);

        ejecucion.correr(1L, "uuid", 2L);

        ArgumentCaptor<String> resumen = ArgumentCaptor.forClass(String.class);
        verify(repository).marcarFallida(eq(1L), resumen.capture());
        assertTrue(resumen.getValue().contains("JIRA_CREDENCIALES_INVALIDAS"));
        assertTrue(resumen.getValue().contains("Jira rechazó el correo o el token"));
    }

    @Test
    void unErrorInesperadoNoMuestraElDetalle() {
        doThrow(new IllegalStateException("detalle interno")).when(importador).importar(1L, 2L);

        ejecucion.correr(1L, "uuid", 2L);

        ArgumentCaptor<String> resumen = ArgumentCaptor.forClass(String.class);
        verify(repository).marcarFallida(eq(1L), resumen.capture());
        assertTrue(resumen.getValue().contains("ERROR_INTERNO"));
        assertTrue(!resumen.getValue().contains("detalle interno"));
    }

    @Test
    void unaCancelacionLaDejaCancelada() {
        doThrow(new ExcepcionDeApi(CodigoDeErrorDelProyecto.IMPORTACION_CANCELADA)).when(importador).importar(1L, 2L);

        ejecucion.correr(1L, "uuid", 2L);

        ArgumentCaptor<String> resumen = ArgumentCaptor.forClass(String.class);
        verify(repository).marcarCancelada(eq(1L), resumen.capture());
        verify(repository, never()).marcarFallida(eq(1L), anyString());
        assertTrue(resumen.getValue().contains("IMPORTACION_CANCELADA"));
    }
}
