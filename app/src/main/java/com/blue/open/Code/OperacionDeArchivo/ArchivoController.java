package com.blue.open.Code.OperacionDeArchivo;

import com.blue.open.Code.OperacionDeArchivo.CasosDeUso.OperacionDeArchivoBuscarUseCase;
import com.blue.open.Code.OperacionDeArchivo.CasosDeUso.OperacionDeArchivoVistaPreviaUseCase;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.Pagina;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    private final OperacionDeArchivoVistaPreviaUseCase vistaPreviaUseCase;

    // Busca lo archivado que el usuario puede archivar y restaurar. Responde una pagina de OperacionDeArchivoBuscarResponseDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-ARC-01", resumen = "Busca lo archivado de cualquier tipo sobre lo que el usuario tiene permiso de archivo",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<Pagina<OperacionDeArchivoBuscarResponseDTO>> paginar(@Valid @RequestBody OperacionDeArchivoBuscarRequestDTO dto) {
        return new ResponseEntity<>(buscarUseCase.ejecutar(dto), HttpStatus.OK);
    }

    // Vista previa de un archivado: que es, donde estaba y que pasa al restaurarlo. Responde la vista previa
    @GetMapping("/{tipo}/{uuid}/vista-previa")
    @Contrato(casoDeUso = "CU-ARC-02", resumen = "Vista previa de un archivado: qué es, dónde estaba, qué se restaura con él y qué queda archivado",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<ArchivoVistaPreviaResponseDTO> vistaPrevia(
            @Descripcion("Tipo: TAREA, PROYECTO, SPRINT, VERSION o USUARIO") @PathVariable("tipo") String tipo,
            @Descripcion("Uuid del elemento archivado") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(vistaPreviaUseCase.ejecutar(tipo, uuid), HttpStatus.OK);
    }
}
