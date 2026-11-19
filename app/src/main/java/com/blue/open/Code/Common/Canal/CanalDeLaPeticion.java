package com.blue.open.Code.Common.Canal;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Resuelve el canal del request actual. El filtro de /mcp (T3.1) marca el request con el atributo ATRIBUTO; sin
 * marca el canal es WEB. Fuera de un request (por ejemplo, el importador en segundo plano) el llamador pasa el canal
 * de forma explicita.
 * Dependencias externas: ninguna.
 */
@Component
public class CanalDeLaPeticion {

    // Atributo del request con el CanalDeCambio que fijo un filtro
    public static final String ATRIBUTO = "shiro.canal";

    // Canal del request actual: el que marco un filtro, o WEB si no hay marca ni request
    public CanalDeCambio actual() {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (atributos == null) {
            return CanalDeCambio.WEB;
        }
        Object canal = atributos.getAttribute(ATRIBUTO, RequestAttributes.SCOPE_REQUEST);
        return canal instanceof CanalDeCambio marcado ? marcado : CanalDeCambio.WEB;
    }
}
