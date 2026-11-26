package com.blue.open.Code.Common.Paginado;

import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;

/**
 * MJ-16 del plan de mejoras: tope de elementos por pagina para todos los endpoints que reciben un PedidoDePaginado
 * (los /paginar del ABM). core-crud no limita el tamano, asi que un pedido podria traer todas las filas de una vez.
 * Se aplica al leer el cuerpo, despues de la autorizacion de @Contrato, en un solo lugar para los controllers
 * actuales y los que vengan.
 * Dependencias externas: ninguna.
 */
@ControllerAdvice
public class TopeDePaginado extends RequestBodyAdviceAdapter {

    // Maximo de elementos por pagina: alcanza para el tablero del front, que pide todas sus tareas en una pagina
    public static final int MAXIMO = 500;

    // Solo intercepta los cuerpos de tipo PedidoDePaginado
    @Override
    public boolean supports(MethodParameter parametro, Type tipo, Class<? extends HttpMessageConverter<?>> conversor) {
        return PedidoDePaginado.class.equals(parametro.getParameterType());
    }

    /*
     * Valida el tamano del pedido ya leido. Recibe el cuerpo convertido; lo responde sin cambios.
     * Lanza ExcepcionDeApi PAGINADO_INVALIDO (400) con el maximo en datos si el tamano lo supera.
     */
    @Override
    public Object afterBodyRead(Object cuerpo, HttpInputMessage mensaje, MethodParameter parametro, Type tipo,
                                Class<? extends HttpMessageConverter<?>> conversor) {
        int tamano = ((PedidoDePaginado) cuerpo).getTamano();
        if (tamano > MAXIMO)
            throw new ExcepcionDeApi(CodigoDeErrorKernel.PAGINADO_INVALIDO, "Se pueden pedir hasta " + MAXIMO + " elementos por página",
                    "TopeDePaginado | afterBodyRead | tamano: " + tamano)
                    .conDato("maximo", MAXIMO);
        return cuerpo;
    }
}
