package com.blue.open.Code.TipoTablero;

import com.blue.open.Code.TipoTablero.CasosDeUso.TipoTableroListarUseCase;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints del catalogo de tipos de tablero. Es de solo lectura: los tableros y sus estados los siembra Liquibase.
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("tipo-tablero")
@RequiredArgsConstructor
public class TipoTableroController {

    private final TipoTableroListarUseCase listarUseCase;

    // Lista los tipos de tablero con sus estados en orden de columna
    @GetMapping("/listar")
    @Contrato(casoDeUso = "CU-TTB-01", resumen = "Lista los tipos de tablero con sus estados en orden de columna",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<List<TipoTableroDTO>> listar() {
        return new ResponseEntity<>(listarUseCase.ejecutar(), HttpStatus.OK);
    }
}
