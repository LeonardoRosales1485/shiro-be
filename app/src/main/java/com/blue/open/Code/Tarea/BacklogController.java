package com.blue.open.Code.Tarea;

import com.blue.open.Code.Tarea.CasosDeUso.TareaBacklogUseCase;
import com.blue.open.Code.Tarea.CasosDeUso.TareaOrdenarBacklogUseCase;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
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

import java.util.List;

/**
 * Endpoints del backlog de un proyecto (Spec 3, T5.1): leerlo y ordenarlo. El backlog es una vista de las tareas
 * del proyecto, por eso vive con Tarea.
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("backlog")
@RequiredArgsConstructor
public class BacklogController {

    private final TareaBacklogUseCase backlogUseCase;

    private final TareaOrdenarBacklogUseCase ordenarUseCase;

    // Backlog de un proyecto en su orden unico. Responde la lista de tareas
    @GetMapping("/proyecto/{uuid}")
    @Contrato(casoDeUso = "CU-BKL-01", resumen = "Backlog de un proyecto: tareas sin sprint de todos los tableros, en su orden único",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<List<TareaDeBacklogDTO>> backlog(@Descripcion("Uuid del proyecto") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(backlogUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Ordena el backlog de un proyecto. Responde el backlog ordenado
    @PostMapping("/ordenar")
    @Contrato(casoDeUso = "CU-BKL-02", resumen = "Ordena el backlog de un proyecto (un único orden para todos)",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<List<TareaDeBacklogDTO>> ordenar(@Valid @RequestBody TareaOrdenarBacklogRequestDTO dto) {
        return new ResponseEntity<>(ordenarUseCase.ejecutar(dto), HttpStatus.OK);
    }
}
