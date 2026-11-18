package com.blue.open.Code.Configuracion.CasosDeUso;

import com.blue.open.Code.Autorizacion.PermisoEnum;
import com.blue.open.Code.Configuracion.ConfiguracionDTO;
import com.blue.open.Code.Configuracion.ConfiguracionRepository;
import com.blue.open.Core.Identity.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Caso de uso: lista las configuraciones visibles para quien consulta.
// Dependencias externas: la base de datos.
// Recibe: nada. Responde: todas las configuraciones si el usuario tiene CONFIGURACION_VER;
// solo las publicas (global = true) si no lo tiene o si no esta logeado.
@Service
@RequiredArgsConstructor
public class ConfiguracionListarUseCase {

    private final ConfiguracionRepository repository;

    private final AuthService authService;

    // Responde: todas las configuraciones vigentes con CONFIGURACION_VER; si no, solo las publicas
    @Transactional(readOnly = true)
    public List<ConfiguracionDTO> ejecutar() {
        if (authService.tienePermisos(List.of(PermisoEnum.CONFIGURACION_VER))) {
            return repository.listarVigentes();
        }
        return repository.listarPublicas();
    }
}
