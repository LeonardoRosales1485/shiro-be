package com.blue.open.Code.Configuracion.CasosDeUso;

import com.blue.open.Code.Configuracion.Configuracion;
import com.blue.open.Code.Configuracion.ConfiguracionDTO;
import com.blue.open.Code.Configuracion.ConfiguracionGuardarRequestDTO;
import com.blue.open.Code.Configuracion.ConfiguracionRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Caso de uso: crea o edita una lista de configuraciones, buscando cada una por su clave.
// Dependencias externas: la base de datos.
// Recibe: la lista de ConfiguracionGuardarRequestDTO. Responde: las configuraciones guardadas, en el orden recibido.
// El permiso CONFIGURACION_CREAR lo valida el controller.
@Service
@RequiredArgsConstructor
public class ConfiguracionGuardarUseCase {

    private static final int ACTIVO = 0;

    private final ConfiguracionRepository repository;

    private final Proyector proyector;

    // Crea las configuraciones cuya clave no existe y edita las existentes, siempre activas.
    // Recibe: la lista de configuraciones. Responde: las guardadas, re-proyectadas, en el orden recibido.
    @Transactional
    public List<ConfiguracionDTO> ejecutar(List<ConfiguracionGuardarRequestDTO> configuraciones) {
        List<Configuracion> aGuardar = new ArrayList<>();
        for (ConfiguracionGuardarRequestDTO dto : configuraciones) {
            Configuracion modelo = repository.findFirstByNombreAndStatus(dto.getNombre(), ACTIVO)
                    .orElseGet(Configuracion::new);
            // Guardar nunca cambia la vigencia: se ignora el status del request (aplicar valida sus transiciones)
            dto.setStatus(ACTIVO);
            aGuardar.add(proyector.aplicar(dto, modelo, CodigoDeErrorCrud.RELACION_NO_ENCONTRADA));
        }
        repository.saveAll(aGuardar);
        // La respuesta sale de la misma proyeccion que las lecturas, en el orden en que se recibio
        Map<String, ConfiguracionDTO> guardadas = repository.buscarPorClaves(
                        aGuardar.stream().map(Configuracion::getNombre).toList()).stream()
                .collect(Collectors.toMap(ConfiguracionDTO::getNombre, Function.identity(), (a, b) -> a));
        return aGuardar.stream().map(modelo -> guardadas.get(modelo.getNombre())).toList();
    }
}
