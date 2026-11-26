package com.blue.open.Code.Relacion;

import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Autorizacion.RecursoDePermiso;
import com.blue.open.Code.Autorizacion.PermisoDeRecurso;
import com.blue.open.Code.Relacion.CasosDeUso.RelacionArchivarUseCase;
import com.blue.open.Code.Relacion.CasosDeUso.RelacionCrearUseCase;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de relaciones entre tareas: crear y archivar. Se leen desde cada tarea (GET /tarea/{uuid}/relaciones).
 * Sin ABM: una relacion no se edita, se archiva y se crea otra.
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("relacion")
@RequiredArgsConstructor
public class RelacionController {

    private final RelacionCrearUseCase crearUseCase;

    private final RelacionArchivarUseCase archivarUseCase;

    // Relaciona dos tareas. Responde la relacion creada
    @PostMapping("/crear")
    @PermisoDeRecurso(nivel = NivelDePermiso.EDITAR, recurso = RecursoDePermiso.TABLERO, detalle = "el tablero de origen, y VER en el de destino")
    @Contrato(casoDeUso = "CU-REL-01", resumen = "Relaciona dos tareas: bloquea, se relaciona con o duplica",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<RelacionDTO> crear(@Valid @RequestBody RelacionCrearRequestDTO dto) {
        return new ResponseEntity<>(crearUseCase.ejecutar(dto), HttpStatus.OK);
    }

    // Archiva una relacion. Responde la relacion archivada
    @PostMapping("/{uuid}/archivar")
    @PermisoDeRecurso(nivel = NivelDePermiso.EDITAR, recurso = RecursoDePermiso.TABLERO, detalle = "el tablero de origen")
    @Contrato(casoDeUso = "CU-REL-02", resumen = "Archiva una relación entre tareas", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<RelacionDTO> archivar(@Descripcion("Uuid de la relación") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(archivarUseCase.ejecutar(uuid), HttpStatus.OK);
    }
}
