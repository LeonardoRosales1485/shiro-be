package com.blue.open.Code.Proyecto;

import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Autorizacion.RecursoDePermiso;
import com.blue.open.Code.Autorizacion.PermisoDeRecurso;
import com.blue.open.Code.Proyecto.CasosDeUso.ProyectoArchivarUseCase;
import com.blue.open.Code.Proyecto.CasosDeUso.ProyectoBacklogUseCase;
import com.blue.open.Code.Proyecto.CasosDeUso.ProyectoOrdenarBacklogUseCase;
import com.blue.open.Code.Proyecto.CasosDeUso.ProyectoRestaurarUseCase;
import com.blue.open.Code.Proyecto.CasosDeUso.ProyectoUsuariosUseCase;
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

import java.util.List;

/**
 * Endpoints de proyectos: ABM (receta de core-crud/docs/receta-controller.md) sin borrar, porque nada se borra: se
 * archiva con todo lo que contiene y se restaura (adenda 4 del DAF). Crear y editar: super admin y Administradores (seccion 4 del DAF). Leer: con
 * sesion; el filtro por permisos de proyecto llega en F2 (T2.4).
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("proyecto")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService service;

    private final ProyectoArchivarUseCase archivarUseCase;

    private final ProyectoRestaurarUseCase restaurarUseCase;

    private final ProyectoBacklogUseCase backlogUseCase;

    private final ProyectoOrdenarBacklogUseCase ordenarBacklogUseCase;

    private final ProyectoUsuariosUseCase usuariosUseCase;

    // Obtiene un proyecto con sus tableros. Responde el ProyectoDTO
    @GetMapping("/uuid/{uuid}")
    @PermisoDeRecurso(nivel = NivelDePermiso.VER, recurso = RecursoDePermiso.PROYECTO, detalle = "o alguno de sus tableros")
    @Contrato(casoDeUso = "CU-PRY-01", resumen = "Obtiene un proyecto con sus tableros", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<ProyectoDTO> obtenerPorUuid(@Descripcion("Uuid del proyecto") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista los proyectos con filtros y orden. Responde la pagina de ProyectoDTO
    @PostMapping("/paginar")
    @PermisoDeRecurso(nivel = NivelDePermiso.VER, recurso = RecursoDePermiso.PROYECTO, detalle = "lista solo los proyectos visibles")
    @Contrato(casoDeUso = "CU-PRY-02", resumen = "Lista los proyectos con filtros y orden", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<Pagina<ProyectoDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }

    // Crea (sin uuid, con sus cuatro tableros) o edita un proyecto. Responde el proyecto guardado
    @PostMapping("/guardar")
    @Contrato(casoDeUso = "CU-PRY-03", resumen = "Crea un proyecto con sus cuatro tableros, o edita su nombre y descripción",
            permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<ProyectoDTO> guardar(@Valid @RequestBody ProyectoDTO dto) {
        return new ResponseEntity<>(service.guardar(dto), HttpStatus.OK);
    }

    // Archiva un proyecto con todo lo que contiene. Responde el proyecto archivado
    @PostMapping("/{uuid}/archivar")
    @PermisoDeRecurso(nivel = NivelDePermiso.ARCHIVAR, recurso = RecursoDePermiso.PROYECTO)
    @Contrato(casoDeUso = "CU-PRY-04", resumen = "Archiva un proyecto con sus tableros, tareas, comentarios y relaciones (exige ARCHIVAR en el proyecto)",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<ProyectoDTO> archivar(@Descripcion("Uuid del proyecto") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(archivarUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Restaura un proyecto archivado con lo que se archivo en su misma operacion. Responde el proyecto restaurado
    @PostMapping("/{uuid}/restaurar")
    @PermisoDeRecurso(nivel = NivelDePermiso.ARCHIVAR, recurso = RecursoDePermiso.PROYECTO)
    @Contrato(casoDeUso = "CU-PRY-05", resumen = "Restaura un proyecto archivado con lo que se archivó junto con él (exige ARCHIVAR en el proyecto)",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<ProyectoDTO> restaurar(@Descripcion("Uuid del proyecto") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(restaurarUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Backlog del proyecto en su orden unico. Responde la lista de tareas
    @GetMapping("/{uuid}/backlog")
    @PermisoDeRecurso(nivel = NivelDePermiso.VER, recurso = RecursoDePermiso.PROYECTO)
    @Contrato(casoDeUso = "CU-BKL-01", resumen = "Backlog de un proyecto: tareas sin sprint de todos los tableros y tipos, en su orden único",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<List<ProyectoBacklogResponseDTO>> backlog(@Descripcion("Uuid del proyecto") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(backlogUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Ordena el backlog del proyecto. Responde el backlog ordenado
    @PostMapping("/{uuid}/ordenar-backlog")
    @PermisoDeRecurso(nivel = NivelDePermiso.EDITAR, recurso = RecursoDePermiso.PROYECTO)
    @Contrato(casoDeUso = "CU-BKL-02", resumen = "Ordena el backlog de un proyecto (un único orden para todos)", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<List<ProyectoBacklogResponseDTO>> ordenarBacklog(@Descripcion("Uuid del proyecto") @PathVariable("uuid") String uuid,
            @Valid @RequestBody ProyectoOrdenarBacklogRequestDTO dto) {
        return new ResponseEntity<>(ordenarBacklogUseCase.ejecutar(uuid, dto), HttpStatus.OK);
    }

    // Usuarios con acceso al proyecto, para elegir el responsable de una tarea. Responde la lista por nombre de usuario
    @GetMapping("/{uuid}/usuarios")
    @PermisoDeRecurso(nivel = NivelDePermiso.VER, recurso = RecursoDePermiso.PROYECTO)
    @Contrato(casoDeUso = "CU-PRY-06", resumen = "Usuarios con acceso a un proyecto, para elegir el responsable de una tarea (exige VER en el proyecto)",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<List<ProyectoUsuarioResponseDTO>> usuarios(@Descripcion("Uuid del proyecto") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(usuariosUseCase.ejecutar(uuid), HttpStatus.OK);
    }
}
