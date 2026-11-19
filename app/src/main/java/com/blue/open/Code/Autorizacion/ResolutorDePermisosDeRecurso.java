package com.blue.open.Code.Autorizacion;

import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.ReglaPermiso.ReglaPermisoRepository;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Identity.Service.AuthService;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;

/**
 * Resuelve si el usuario de la sesion tiene un nivel (ver, editar, archivar) sobre un proyecto o un tablero (Spec 2).
 * El super admin y los Administradores pueden todo; el resto se resuelve con EvaluadorDeReglasDePermiso y las reglas
 * del usuario y de sus grupos, que se leen una vez por request. Lo llaman la API y el MCP.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class ResolutorDePermisosDeRecurso {

    // Atributos del request donde se cachean la condicion de administrador y las reglas
    private static final String ATRIBUTO_ADMINISTRADOR = "shiro.permisos.administrador";

    private static final String ATRIBUTO_REGLAS = "shiro.permisos.reglas";

    private static final String ADMIN = "ADMIN";

    private final AuthService authService;

    private final ReglaPermisoRepository reglas;

    // Si el usuario de la sesion tiene el nivel sobre el tablero (sus reglas o, si no deciden, las de su proyecto)
    public boolean puede(NivelDePermiso nivel, Tablero tablero) {
        return esAdministrador() || EvaluadorDeReglasDePermiso.puede(reglasDeLaSesion(), nivel, tablero.getId(),
                tablero.getProyecto().getId());
    }

    // Si el usuario de la sesion tiene el nivel sobre el proyecto
    public boolean puede(NivelDePermiso nivel, Proyecto proyecto) {
        return esAdministrador() || EvaluadorDeReglasDePermiso.puede(reglasDeLaSesion(), nivel, null, proyecto.getId());
    }

    // Exige el nivel sobre el tablero. Lanza SIN_PERMISO si no lo tiene
    public void exigir(NivelDePermiso nivel, Tablero tablero) {
        if (!puede(nivel, tablero))
            throw sinPermiso(nivel, "el tablero", "tablero " + tablero.getUuid());
    }

    // Exige el nivel sobre el proyecto. Lanza SIN_PERMISO si no lo tiene
    public void exigir(NivelDePermiso nivel, Proyecto proyecto) {
        if (!puede(nivel, proyecto))
            throw sinPermiso(nivel, "el proyecto", "proyecto " + proyecto.getUuid());
    }

    // Si el usuario de la sesion es el super admin o del grupo Administradores
    public boolean esAdministrador() {
        Object cache = atributo(ATRIBUTO_ADMINISTRADOR);
        if (cache instanceof Boolean administrador) {
            return administrador;
        }
        UsuarioBase usuario = authService.getActualObligatorio();
        boolean administrador = authService.esSysAdmin(usuario)
                || authService.tieneAlgunRolOPermisoPorNombre(usuario, List.of(ADMIN), List.of());
        guardar(ATRIBUTO_ADMINISTRADOR, administrador);
        return administrador;
    }

    // Reglas vigentes del usuario de la sesion y de sus grupos, leidas una vez por request
    @SuppressWarnings("unchecked")
    public List<ReglaEvaluable> reglasDeLaSesion() {
        Object cache = atributo(ATRIBUTO_REGLAS);
        if (cache instanceof List<?> lista) {
            return (List<ReglaEvaluable>) lista;
        }
        List<ReglaEvaluable> delUsuario = reglas.reglasDelUsuario(authService.getActualObligatorio().getId());
        guardar(ATRIBUTO_REGLAS, delUsuario);
        return delUsuario;
    }

    private static Object atributo(String nombre) {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        return atributos == null ? null : atributos.getAttribute(nombre, RequestAttributes.SCOPE_REQUEST);
    }

    private static void guardar(String nombre, Object valor) {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (atributos != null) {
            atributos.setAttribute(nombre, valor, RequestAttributes.SCOPE_REQUEST);
        }
    }

    // SIN_PERMISO con el nivel y el recurso en el mensaje
    private static ExcepcionDeApi sinPermiso(NivelDePermiso nivel, String recurso, String detalle) {
        String accion = switch (nivel) {
            case VER -> "ver";
            case EDITAR -> "editar";
            case ARCHIVAR -> "archivar";
        };
        return new ExcepcionDeApi(CodigoDeErrorKernel.SIN_PERMISO, "No tenés permiso para " + accion + " en " + recurso + ".",
                "ResolutorDePermisosDeRecurso | exigir | " + nivel + " | " + detalle);
    }
}
