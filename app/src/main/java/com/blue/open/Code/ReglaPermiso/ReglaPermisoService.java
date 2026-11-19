package com.blue.open.Code.ReglaPermiso;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Core.Crud.AbstractService;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ABM de reglas de permiso (CU-RPE-01 a CU-RPE-03) sobre los hooks de AbstractService. Valida que el sujeto y el
 * recurso coincidan con su tipo (REGLA_DE_PERMISO_INVALIDA) y que no se repita el mismo sujeto, recurso y nivel
 * (REGLA_DE_PERMISO_REPETIDA). Cada guardado deja su fila en el historial.
 * Dependencias externas: la base de datos.
 */
@Service
@RequiredArgsConstructor
public class ReglaPermisoService extends AbstractService<ReglaPermiso, ReglaPermisoDTO, ReglaPermisoDTO> {

    private final ReglaPermisoRepository reglaPermisoRepository;

    private final RegistradorDeHistorial historial;

    /*
     * Valida el DTO y lo aplica: un solo sujeto segun tipoSujeto y un solo recurso segun tipoRecurso, y que la
     * combinacion no exista. Recuerda los valores anteriores para el historial.
     */
    @Override
    protected ReglaPermiso hookGuardarAplicarDto(ReglaPermisoDTO dto, ReglaPermiso modelo) {
        boolean esRol = dto.getTipoSujeto() == TipoDeSujeto.ROL;
        boolean esProyecto = dto.getTipoRecurso() == TipoDeRecurso.PROYECTO;
        if (vacio(esRol ? dto.getRolUuid() : dto.getUsuarioUuid()) || !vacio(esRol ? dto.getUsuarioUuid() : dto.getRolUuid()))
            throw invalida("Indicá " + (esRol ? "el grupo" : "el usuario") + " de la regla, y nada más.", "sujeto");
        if (vacio(esProyecto ? dto.getProyectoUuid() : dto.getTableroUuid())
                || !vacio(esProyecto ? dto.getTableroUuid() : dto.getProyectoUuid()))
            throw invalida("Indicá " + (esProyecto ? "el proyecto" : "el tablero") + " de la regla, y nada más.", "recurso");
        if (modelo.getId() != null)
            modelo.setValoresAnteriores(valoresDelHistorial(modelo));
        ReglaPermiso aplicada = super.hookGuardarAplicarDto(dto, modelo);
        long id = aplicada.getId() == null ? -1L : aplicada.getId();
        if (reglaPermisoRepository.existsByRolAndUsuarioAndProyectoAndTableroAndNivelAndStatusAndIdNot(aplicada.getRol(),
                aplicada.getUsuario(), aplicada.getProyecto(), aplicada.getTablero(), aplicada.getNivel(), 0, id))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.REGLA_DE_PERMISO_REPETIDA,
                    "ReglaPermisoService | hookGuardarAplicarDto | regla repetida");
        return aplicada;
    }

    // Guarda la regla y escribe su historial: CREAR con el resumen, o EDITAR por campo cambiado
    @Override
    protected ReglaPermiso hookGuardarEnBase(ReglaPermiso modelo) {
        boolean esAlta = modelo.getId() == null;
        ReglaPermiso guardada = super.hookGuardarEnBase(modelo);
        Map<String, String> valores = valoresDelHistorial(guardada);
        if (esAlta)
            historial.registrar(Historial.REGLA_PERMISO, guardada.getUuid(), AccionDeHistorial.CREAR, null, null,
                    String.join(" ", valores.values()));
        else
            historial.registrarCambios(Historial.REGLA_PERMISO, guardada.getUuid(), modelo.getValoresAnteriores(), valores);
        return guardada;
    }

    // Valores de la regla que sigue el historial, como texto y en orden fijo
    private static Map<String, String> valoresDelHistorial(ReglaPermiso regla) {
        Map<String, String> valores = new LinkedHashMap<>();
        valores.put("sujeto", regla.getRol() != null ? "grupo " + regla.getRol().getNombre()
                : regla.getUsuario() == null ? null : "usuario " + regla.getUsuario().getNombreDeUsuario());
        valores.put("recurso", regla.getProyecto() != null ? "proyecto " + regla.getProyecto().getPrefijo()
                : regla.getTablero() == null ? null : "tablero " + regla.getTablero().getUuid());
        valores.put("nivel", regla.getNivel() == null ? null : regla.getNivel().name());
        valores.put("efecto", regla.getEfecto() == null ? null : regla.getEfecto().name());
        return valores;
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    // Error de regla mal armada, con el dato que fallo
    private static ExcepcionDeApi invalida(String mensaje, String dato) {
        return new ExcepcionDeApi(CodigoDeErrorDelProyecto.REGLA_DE_PERMISO_INVALIDA, mensaje,
                "ReglaPermisoService | hookGuardarAplicarDto | " + dato);
    }
}
