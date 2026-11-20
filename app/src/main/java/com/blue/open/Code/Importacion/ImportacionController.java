package com.blue.open.Code.Importacion;

import com.blue.open.Code.Importacion.CasosDeUso.ImportacionCancelarUseCase;
import com.blue.open.Code.Importacion.CasosDeUso.ImportacionCatalogoUseCase;
import com.blue.open.Code.Importacion.CasosDeUso.ImportacionConectarUseCase;
import com.blue.open.Code.Importacion.CasosDeUso.ImportacionEjecutarUseCase;
import com.blue.open.Code.Importacion.CasosDeUso.ImportacionMapearUseCase;
import com.blue.open.Code.Importacion.CasosDeUso.ImportacionProyectosDeJiraUseCase;
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
 * Endpoints del importador de Jira (F4, Spec 6): conectar, leer el catalogo, mapear, ejecutar, y obtener y listar
 * importaciones. Solo los administradores (super admin y Administrador).
 * Dependencias externas: Jira y la base de datos (a traves de los casos de uso).
 */
@RestController
@RequestMapping("importacion")
@RequiredArgsConstructor
public class ImportacionController {

    private static final String SYSADMIN = "SYSADMIN";

    private static final String ADMIN = "ADMIN";

    private final ImportacionService service;

    private final ImportacionConectarUseCase conectarUseCase;

    private final ImportacionCatalogoUseCase catalogoUseCase;

    private final ImportacionMapearUseCase mapearUseCase;

    private final ImportacionEjecutarUseCase ejecutarUseCase;

    private final ImportacionCancelarUseCase cancelarUseCase;

    private final ImportacionProyectosDeJiraUseCase proyectosDeJiraUseCase;

    // Obtiene una importacion por su uuid (sin el token). Responde el ImportacionDTO
    @GetMapping("/uuid/{uuid}")
    @Contrato(casoDeUso = "CU-IMP-05", resumen = "Obtiene una importación de Jira por su uuid (estado, mapeo y resumen; nunca el token)",
            permiso = {SYSADMIN, ADMIN})
    public ResponseEntity<ImportacionDTO> obtenerPorUuid(@Descripcion("Uuid del registro") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista las importaciones con filtros y orden. Responde la pagina de ImportacionDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-IMP-06", resumen = "Lista las importaciones de Jira con filtros y orden", permiso = {SYSADMIN, ADMIN})
    public ResponseEntity<Pagina<ImportacionDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }

    // Conecta con un proyecto de Jira y guarda la importacion. Responde el catalogo para mapear
    @PostMapping("/conectar")
    @Contrato(casoDeUso = "CU-IMP-01", resumen = "Conecta con un proyecto de Jira (guarda el token cifrado) y responde sus tableros, estados, tipos y usuarios para mapear",
            permiso = {SYSADMIN, ADMIN})
    public ResponseEntity<ImportacionCatalogoDTO> conectar(@Valid @RequestBody ImportacionConectarRequestDTO dto) {
        return new ResponseEntity<>(conectarUseCase.ejecutar(dto), HttpStatus.OK);
    }

    // Vuelve a leer el catalogo de Jira de una importacion. Responde el catalogo
    @GetMapping("/{uuid}/catalogo")
    @Contrato(casoDeUso = "CU-IMP-02", resumen = "Vuelve a leer de Jira los tableros, estados, tipos y usuarios de una importación",
            permiso = {SYSADMIN, ADMIN})
    public ResponseEntity<ImportacionCatalogoDTO> catalogo(@Descripcion("Uuid de la importación") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(catalogoUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Guarda el mapeo de tableros, estados y tipos. Responde la importacion MAPEADA
    @PostMapping("/{uuid}/mapeo")
    @Contrato(casoDeUso = "CU-IMP-03", resumen = "Guarda el mapeo de tableros, estados y tipos de Jira a los de Shiro",
            permiso = {SYSADMIN, ADMIN})
    public ResponseEntity<ImportacionDTO> mapear(@Descripcion("Uuid de la importación") @PathVariable("uuid") String uuid,
            @Valid @RequestBody ImportacionMapeoRequestDTO dto) {
        return new ResponseEntity<>(mapearUseCase.ejecutar(uuid, dto), HttpStatus.OK);
    }

    // Ejecuta la importacion en segundo plano. Responde la importacion EN_CURSO
    @PostMapping("/{uuid}/ejecutar")
    @Contrato(casoDeUso = "CU-IMP-04", resumen = "Ejecuta la importación en segundo plano (reimportar actualiza sin duplicar); se consulta su estado hasta que termine",
            permiso = {SYSADMIN, ADMIN})
    public ResponseEntity<ImportacionDTO> ejecutar(@Descripcion("Uuid de la importación") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(ejecutarUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Pide cancelar la importacion en curso. Responde la importacion con cancelacionPedida
    @PostMapping("/{uuid}/cancelar")
    @Contrato(casoDeUso = "CU-IMP-07", resumen = "Cancela una importación en curso: se corta en el próximo control, no queda nada de esa ejecución y pasa a CANCELADA",
            permiso = {SYSADMIN, ADMIN})
    public ResponseEntity<ImportacionDTO> cancelar(@Descripcion("Uuid de la importación") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(cancelarUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Lista los proyectos de Jira visibles con las credenciales, sin guardar nada. Responde la lista de proyectos
    @PostMapping("/proyectos-de-jira")
    @Contrato(casoDeUso = "CU-IMP-08", resumen = "Prueba la conexión con Jira y lista los proyectos visibles con esas credenciales (no guarda el token)",
            permiso = {SYSADMIN, ADMIN})
    public ResponseEntity<List<ProyectoDeJiraDTO>> proyectosDeJira(@Valid @RequestBody ImportacionProyectosRequestDTO dto) {
        return new ResponseEntity<>(proyectosDeJiraUseCase.ejecutar(dto), HttpStatus.OK);
    }
}
