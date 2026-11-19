package com.blue.open.Code.TipoTablero;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * CU-TTB-01: catalogo de tableros con sus estados (GET /tipo-tablero/listar). T1.1 del plan: los estados por tablero
 * son los de la seccion 4 del DAF, en orden de columna; el primero es el inicial y los terminados son Hecho en el
 * principal y Aprobado en los sub-tableros (adenda 3 del DAF).
 */
class TipoTableroListarContratoTest extends PruebaDeContrato {

    @Test
    void listaLosCuatroTablerosEnOrden() {
        JsonNode tableros = listar();

        assertEquals(List.of("PRINCIPAL", "QA", "ANALISIS", "DISENO"), valores(tableros, "codigo"));
        assertEquals(List.of("Principal", "QA", "Análisis", "Diseño"), valores(tableros, "nombre"));
        tableros.forEach(tablero -> assertFalse(tablero.has("id"), "No se expone el id interno"));
    }

    @Test
    void cadaTableroTieneLosEstadosDelDafEnOrden() {
        JsonNode tableros = listar();

        assertEquals(List.of("Por hacer", "En curso", "Bloqueado", "En revisión", "Hecho"), estados(tableros, 0));
        assertEquals(List.of("Por probar", "Probando", "Bloqueado", "Aprobado", "Rechazado"), estados(tableros, 1));
        assertEquals(List.of("Por analizar", "Analizando", "Bloqueado", "En validación", "Aprobado"),
                estados(tableros, 2));
        assertEquals(List.of("Por diseñar", "Diseñando", "Bloqueado", "En revisión", "Aprobado"), estados(tableros, 3));
    }

    @Test
    void elPrimerEstadoEsElInicialYLosTerminadosSonHechoYAprobado() {
        JsonNode tableros = listar();

        for (JsonNode tablero : tableros) {
            List<String> iniciales = new ArrayList<>();
            List<String> terminados = new ArrayList<>();
            for (JsonNode estado : tablero.get("estados")) {
                if (estado.get("inicial").asBoolean()) {
                    iniciales.add(estado.get("nombre").asString());
                }
                if (estado.get("terminado").asBoolean()) {
                    terminados.add(estado.get("nombre").asString());
                }
            }
            String codigo = tablero.get("codigo").asString();
            assertEquals(List.of(tablero.get("estados").get(0).get("nombre").asString()), iniciales, codigo);
            assertEquals(List.of("PRINCIPAL".equals(codigo) ? "Hecho" : "Aprobado"), terminados, codigo);
        }
    }

    // GET /tipo-tablero/listar con un usuario sin roles especiales (alcanza con la sesion)
    private JsonNode listar() {
        return como(RolBaseEnum.ADMIN.name()).get("/tipo-tablero/listar").ejecutar().esperar(200).json();
    }

    // Nombres de los estados del tablero en la posicion recibida, en el orden en que llegan
    private static List<String> estados(JsonNode tableros, int posicion) {
        return valores(tableros.get(posicion).get("estados"), "nombre");
    }

    // Valores de un campo de texto de cada elemento, en orden
    private static List<String> valores(JsonNode lista, String campo) {
        List<String> resultado = new ArrayList<>();
        lista.forEach(elemento -> resultado.add(elemento.get(campo).asString()));
        return resultado;
    }
}
