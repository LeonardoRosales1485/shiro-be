package com.blue.open.Code.TipoTarea;

import com.blue.open.Code.Autorizacion.SoloSesion;
import com.blue.open.Code.TipoTarea.CasosDeUso.TipoTareaListarUseCase;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints del catalogo de tipos de tarea. Es de solo lectura: los tipos los siembra Liquibase.
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("tipo-tarea")
@RequiredArgsConstructor
public class TipoTareaController {

    private final TipoTareaListarUseCase listarUseCase;

    // Lista los tipos de tarea con su nivel en la jerarquia
    @GetMapping("/listar")
    @SoloSesion("catálogo común a todos los proyectos")
    @Contrato(casoDeUso = "CU-TTA-01", resumen = "Lista los tipos de tarea con su nivel en la jerarquía",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<List<TipoTareaDTO>> listar() {
        return new ResponseEntity<>(listarUseCase.ejecutar(), HttpStatus.OK);
    }
}
