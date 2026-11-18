package com.blue.open.Code.Configuracion;

import com.blue.open.Code.Configuracion.CasosDeUso.ConfiguracionGuardarUseCase;
import com.blue.open.Code.Configuracion.CasosDeUso.ConfiguracionListarUseCase;
import com.blue.open.Code.Configuracion.CasosDeUso.ConfiguracionObtenerUseCase;
import com.blue.open.Code.Configuracion.CasosDeUso.ConfiguracionObtenerVariasUseCase;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de configuraciones: variables del sistema que se cambian sin recompilar o que varian segun el entorno.
 * No hay un limite de variables a crear, y son dinamicas: siempre se tiene que comprobar que existan o usar un valor
 * predeterminado.
 *
 * Existen 2 tipos de variables, las publicas y las privadas. Las publicas las puede obtener cualquier usuario, incluso
 * sin estar conectado. Las privadas requieren el permiso CONFIGURACION_VER, o se obtienen desde el backend. Solo se
 * pueden crear o editar con el permiso CONFIGURACION_CREAR.
 *
 * No publica el ABM (receta de core-crud): todos buscan por clave y no por uuid, asi que cada uno es un caso de uso propio.
 */
@RestController
@RequestMapping("configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionListarUseCase listarUseCase;

    private final ConfiguracionObtenerUseCase obtenerUseCase;

    private final ConfiguracionObtenerVariasUseCase obtenerVariasUseCase;

    private final ConfiguracionGuardarUseCase guardarUseCase;

    // Lista las configuraciones visibles para quien consulta (todas con CONFIGURACION_VER, si no solo las publicas)
    @GetMapping("/listar")
    @Contrato(casoDeUso = "CU-CON-01", resumen = "Lista las configuraciones visibles para quien consulta",
            permiso = Contrato.PUBLICO)
    public ResponseEntity<List<ConfiguracionDTO>> listar() {
        return new ResponseEntity<>(listarUseCase.ejecutar(), HttpStatus.OK);
    }

    // Obtiene una configuracion por su clave. Una privada exige CONFIGURACION_VER
    @GetMapping("/obtener/{clave}")
    @Contrato(casoDeUso = "CU-CON-02", resumen = "Obtiene una configuración por su clave", permiso = Contrato.PUBLICO)
    public ResponseEntity<ConfiguracionDTO> obtener(
            @Descripcion("Clave (nombre) de la configuración") @PathVariable("clave") String clave) {
        return new ResponseEntity<>(obtenerUseCase.ejecutar(clave), HttpStatus.OK);
    }

    // Obtiene varias configuraciones por sus claves. Sin CONFIGURACION_VER, falla si alguna es privada
    @PostMapping("/obtener-varias")
    @Contrato(casoDeUso = "CU-CON-03", resumen = "Obtiene varias configuraciones por sus claves",
            permiso = Contrato.PUBLICO)
    public ResponseEntity<List<ConfiguracionDTO>> obtenerVarias(@RequestBody List<String> claves) {
        return new ResponseEntity<>(obtenerVariasUseCase.ejecutar(claves), HttpStatus.OK);
    }

    // Crea o edita una lista de configuraciones (busca cada una por su clave). Requiere CONFIGURACION_CREAR
    @PostMapping("/guardar")
    @Contrato(casoDeUso = "CU-CON-04", resumen = "Crea o edita configuraciones (busca cada una por su clave)",
            permiso = "CONFIGURACION_CREAR")
    public ResponseEntity<List<ConfiguracionDTO>> guardar(
            @RequestBody List<@Valid ConfiguracionGuardarRequestDTO> configuraciones) {
        return new ResponseEntity<>(guardarUseCase.ejecutar(configuraciones), HttpStatus.OK);
    }
}
