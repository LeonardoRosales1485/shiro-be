package com.blue.open.Code.Common.Paginado;

import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MJ-16 del plan de mejoras: tope de 500 elementos por pagina en todos los endpoints que reciben un PedidoDePaginado
 * (los /paginar del ABM; /archivo/paginar tiene su propio DTO, con maximo 200). Por encima responde 400 PAGINADO_INVALIDO con el maximo en datos; 500 sigue andando, que es
 * lo que pide el tablero del front. Los endpoints se descubren del contexto, asi que uno nuevo queda cubierto solo.
 * Dependencias externas: ninguna.
 */
class TopeDePaginadoContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mapeos;

    @Test
    void porEncimaDelTopeRespondeCuatrocientosEnTodosLosPaginar() {
        List<String> rutas = rutasConPedidoDePaginado();
        assertTrue(rutas.size() >= 10, "Se esperaban al menos 10 endpoints de paginar y se encontraron: " + rutas);
        for (String ruta : rutas) {
            como(ADMIN).post(ruta).json(pedido(501)).preparacion().ejecutar().esperarError(400, "PAGINADO_INVALIDO");
        }
    }

    @Test
    void porEncimaDelTopeLaTareaRespondePaginadoInvalido() {
        como(ADMIN).post("/tarea/paginar").json(pedido(501)).ejecutar().esperarError(400, "PAGINADO_INVALIDO");
    }

    @Test
    void elTopeExactoSigueAndando() {
        como(ADMIN).post("/tarea/paginar").json(pedido(500)).preparacion().ejecutar().esperar(200);
    }

    // Pedido de la primera pagina con el tamano indicado, sin filtros ni orden
    private static Map<String, Object> pedido(int tamano) {
        return Map.of("pagina", 1, "tamano", tamano, "filtros", List.of(), "ordenes", List.of());
    }

    // Rutas de los endpoints cuyo cuerpo es un PedidoDePaginado, sin el context-path
    private List<String> rutasConPedidoDePaginado() {
        return mapeos.getHandlerMethods().entrySet().stream()
                .filter(e -> Arrays.stream(e.getValue().getMethodParameters())
                        .anyMatch(p -> PedidoDePaginado.class.equals(p.getParameterType())))
                .flatMap(e -> e.getKey().getPatternValues().stream())
                .sorted()
                .toList();
    }
}
