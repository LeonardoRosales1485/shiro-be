package com.blue.open.Code.Version;

import com.blue.open.Code.Version.CasosDeUso.VersionArchivarUseCase;
import com.blue.open.Code.Version.CasosDeUso.VersionAsignarUseCase;
import com.blue.open.Code.Version.CasosDeUso.VersionChangelogUseCase;
import com.blue.open.Code.Version.CasosDeUso.VersionPublicarUseCase;
import com.blue.open.Code.Version.CasosDeUso.VersionQuitarUseCase;
import com.blue.open.Code.Version.CasosDeUso.VersionRestaurarUseCase;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.Pagina;
import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * Endpoints de versiones (Spec 3, T5.3): ABM sin borrar, asignar y quitar tareas, publicar, changelog en Markdown,
 * archivar y restaurar. Leer exige VER en el proyecto, cambiar EDITAR y archivar ARCHIVAR.
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("version")
@RequiredArgsConstructor
public class VersionController {

    private static final MediaType MARKDOWN = new MediaType("text", "markdown", StandardCharsets.UTF_8);

    private final VersionService service;

    private final VersionAsignarUseCase asignarUseCase;

    private final VersionQuitarUseCase quitarUseCase;

    private final VersionPublicarUseCase publicarUseCase;

    private final VersionChangelogUseCase changelogUseCase;

    private final VersionArchivarUseCase archivarUseCase;

    private final VersionRestaurarUseCase restaurarUseCase;

    // Obtiene una version. Responde el VersionDTO
    @GetMapping("/uuid/{uuid}")
    @Contrato(casoDeUso = "CU-VER-01", resumen = "Obtiene una versión", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<VersionDTO> obtenerPorUuid(@Descripcion("Uuid de la versión") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista versiones con filtros y orden. Responde la pagina de VersionDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-VER-02", resumen = "Lista versiones con filtros y orden", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<Pagina<VersionDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }

    // Crea una version sin publicar o edita su nombre. Responde la version guardada
    @PostMapping("/guardar")
    @Contrato(casoDeUso = "CU-VER-03", resumen = "Crea una versión o edita su nombre (una publicada no se edita)", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<VersionDTO> guardar(@Valid @RequestBody VersionDTO dto) {
        return new ResponseEntity<>(service.guardar(dto), HttpStatus.OK);
    }

    // Asigna tareas a la version. Responde la version
    @PostMapping("/{uuid}/asignar")
    @Contrato(casoDeUso = "CU-VER-04", resumen = "Asigna tareas del proyecto a una versión sin publicar", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<VersionDTO> asignar(@Descripcion("Uuid de la versión") @PathVariable("uuid") String uuid,
            @Valid @RequestBody VersionTareasRequestDTO dto) {
        return new ResponseEntity<>(asignarUseCase.ejecutar(uuid, dto), HttpStatus.OK);
    }

    // Quita tareas de la version. Responde la version
    @PostMapping("/{uuid}/quitar")
    @Contrato(casoDeUso = "CU-VER-05", resumen = "Quita tareas de una versión sin publicar", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<VersionDTO> quitar(@Descripcion("Uuid de la versión") @PathVariable("uuid") String uuid,
            @Valid @RequestBody VersionTareasRequestDTO dto) {
        return new ResponseEntity<>(quitarUseCase.ejecutar(uuid, dto), HttpStatus.OK);
    }

    // Publica la version con la fecha elegida. Responde la version publicada
    @PostMapping("/{uuid}/publicar")
    @Contrato(casoDeUso = "CU-VER-06", resumen = "Publica una versión con la fecha elegida; su changelog queda fijo", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<VersionDTO> publicar(@Descripcion("Uuid de la versión") @PathVariable("uuid") String uuid,
            @RequestBody VersionPublicarRequestDTO dto) {
        return new ResponseEntity<>(publicarUseCase.ejecutar(uuid, dto), HttpStatus.OK);
    }

    // Changelog de la version en Markdown
    @GetMapping(value = "/{uuid}/changelog", produces = "text/markdown")
    @Contrato(casoDeUso = "CU-VER-07", resumen = "Changelog de una versión en Markdown: tareas terminadas agrupadas por tipo", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<String> changelog(@Descripcion("Uuid de la versión") @PathVariable("uuid") String uuid) {
        return ResponseEntity.ok().contentType(MARKDOWN).body(changelogUseCase.ejecutar(uuid));
    }

    // Archiva la version. Responde la version archivada
    @PostMapping("/{uuid}/archivar")
    @Contrato(casoDeUso = "CU-VER-08", resumen = "Archiva una versión", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<VersionDTO> archivar(@Descripcion("Uuid de la versión") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(archivarUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Restaura la version archivada. Responde la version restaurada
    @PostMapping("/{uuid}/restaurar")
    @Contrato(casoDeUso = "CU-VER-09", resumen = "Restaura una versión archivada", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<VersionDTO> restaurar(@Descripcion("Uuid de la versión") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(restaurarUseCase.ejecutar(uuid), HttpStatus.OK);
    }
}
