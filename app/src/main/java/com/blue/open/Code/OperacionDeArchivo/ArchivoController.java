package com.blue.open.Code.OperacionDeArchivo;

import com.blue.open.Code.OperacionDeArchivo.CasosDeUso.OperacionDeArchivoBuscarUseCase;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.DTO.Pagina;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de la busqueda de archivados (Spec 3, T5.4). Cada elemento se restaura en el endpoint de su tipo.
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("archivo")
@RequiredArgsConstructor
public class ArchivoController {

    private final OperacionDeArchivoBuscarUseCase buscarUseCase;

    // Busca lo archivado que el usuario puede archivar y restaurar. Responde una pagina de OperacionDeArchivoBuscarResponseDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-ARC-01", resumen = "Busca lo archivado de cualquier tipo sobre lo que el usuario tiene permiso de archivo",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<Pagina<OperacionDeArchivoBuscarResponseDTO>> paginar(@Valid @RequestBody OperacionDeArchivoBuscarRequestDTO dto) {
        return new ResponseEntity<>(buscarUseCase.ejecutar(dto), HttpStatus.OK);
    }
}
