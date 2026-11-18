package com.blue.open.Code.Configuracion.CasosDeUso;

import com.blue.open.Code.Autorizacion.PermisoEnum;
import com.blue.open.Code.Configuracion.ConfiguracionDTO;
import com.blue.open.Code.Configuracion.ConfiguracionRepository;
import com.blue.open.Core.Identity.Service.AuthService;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Caso de uso: obtiene una configuracion por su clave.
// Dependencias externas: la base de datos.
// Recibe: la clave. Responde: la configuracion; si no existe, una publica con valor null.
// Una configuracion privada exige CONFIGURACION_VER (401 si no hay sesion, 403 si no tiene el permiso).
@Service
@RequiredArgsConstructor
public class ConfiguracionObtenerUseCase {

    private final ConfiguracionRepository repository;

    private final AuthService authService;

    // Busca la configuracion por clave (si no existe responde una publica con valor null).
    // Si es privada exige sesion y CONFIGURACION_VER: lanza NO_AUTENTICADO o SIN_PERMISO.
    @Transactional(readOnly = true)
    public ConfiguracionDTO ejecutar(String clave) {
        ConfiguracionDTO configuracion = repository.buscarPorClave(clave)
                .orElseGet(() -> ConfiguracionDTO.inexistente(clave));
        // El permiso depende del registro (global o privada): no se puede declarar en @Contrato.
        // Una privada exige sesion (getActualObligatorio: 401 NO_AUTENTICADO) y CONFIGURACION_VER
        if (!configuracion.isGlobal()) {
            authService.getActualObligatorio();
            if (!authService.tienePermisos(List.of(PermisoEnum.CONFIGURACION_VER))) {
                throw new ExcepcionDeApi(CodigoDeErrorKernel.SIN_PERMISO, "No tienes permiso",
                        "ConfiguracionObtenerUseCase | ejecutar | configuracion privada sin CONFIGURACION_VER: " + clave);
            }
        }
        return configuracion;
    }
}
