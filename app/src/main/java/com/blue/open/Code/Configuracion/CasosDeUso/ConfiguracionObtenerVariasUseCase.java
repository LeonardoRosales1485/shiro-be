package com.blue.open.Code.Configuracion.CasosDeUso;

import com.blue.open.Code.Autorizacion.PermisoEnum;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Configuracion.ConfiguracionDTO;
import com.blue.open.Code.Configuracion.ConfiguracionRepository;
import com.blue.open.Core.Identity.Service.AuthService;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Caso de uso: obtiene varias configuraciones por sus claves.
// Dependencias externas: la base de datos.
// Recibe: la lista de claves. Responde: una configuracion por clave (las inexistentes, publicas con valor null).
// Si alguna es privada y el usuario no tiene CONFIGURACION_VER lanza 403 CONFIGURACIONES_PRIVADAS indicando cuales.
@Service
@RequiredArgsConstructor
public class ConfiguracionObtenerVariasUseCase {

    private final ConfiguracionRepository repository;

    private final AuthService authService;

    // Busca las configuraciones por clave y completa las inexistentes como publicas con valor null.
    // Sin CONFIGURACION_VER, si alguna es privada lanza CONFIGURACIONES_PRIVADAS con sus nombres.
    @Transactional(readOnly = true)
    public List<ConfiguracionDTO> ejecutar(List<String> claves) {
        List<ConfiguracionDTO> configuraciones = new ArrayList<>(repository.buscarPorClaves(claves));

        // Las claves que no existen se responden publicas y con valor null
        Set<String> existentes = configuraciones.stream()
                .map(c -> c.getNombre())
                .collect(Collectors.toSet());
        for (String clave : claves) {
            if (!existentes.contains(clave)) {
                configuraciones.add(ConfiguracionDTO.inexistente(clave));
            }
        }

        if (!authService.tienePermisos(List.of(PermisoEnum.CONFIGURACION_VER))) {
            String privadas = configuraciones.stream()
                    .filter(c -> !c.isGlobal())
                    .map(c -> c.getNombre())
                    .collect(Collectors.joining(", "));
            if (!privadas.isEmpty()) {
                throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CONFIGURACIONES_PRIVADAS,
                        "Las variables " + privadas + " son privadas",
                        "ConfiguracionObtenerVariasUseCase | ejecutar | sin permiso CONFIGURACION_VER");
            }
        }

        return configuraciones;
    }
}
