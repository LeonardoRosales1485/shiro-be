package com.blue.open.Code.Comentario;

import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.Pagina;
import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
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
 * Endpoints de comentarios: ABM (receta de core-crud/docs/receta-controller.md) sin borrar. El listado de una tarea
 * es el paginado filtrado por tareaUuid. Con sesion; los permisos por proyecto llegan en F2 (comentar exige EDITAR
 * en el tablero).
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("comentario")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService service;

    // Obtiene un comentario. Responde el ComentarioDTO
    @GetMapping("/uuid/{uuid}")
    @Contrato(casoDeUso = "CU-COM-01", resumen = "Obtiene un comentario", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<ComentarioDTO> obtenerPorUuid(@Descripcion("Uuid del comentario") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista comentarios con filtros y orden (normalmente los de una tarea). Responde la pagina de ComentarioDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-COM-02", resumen = "Lista comentarios con filtros y orden, por ejemplo los de una tarea",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<Pagina<ComentarioDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }

    // Comenta una tarea (sin uuid) o edita el cuerpo de un comentario propio. Responde el comentario guardado
    @PostMapping("/guardar")
    @Contrato(casoDeUso = "CU-COM-03", resumen = "Comenta una tarea, o edita el cuerpo de un comentario propio",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<ComentarioDTO> guardar(@Valid @RequestBody ComentarioDTO dto) {
        return new ResponseEntity<>(service.guardar(dto), HttpStatus.OK);
    }
}
