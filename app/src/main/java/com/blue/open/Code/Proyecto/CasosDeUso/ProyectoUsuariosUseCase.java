package com.blue.open.Code.Proyecto.CasosDeUso;

import com.blue.open.Code.Autorizacion.EvaluadorDeReglasDePermiso;
import com.blue.open.Code.Autorizacion.ReglaDeUnUsuario;
import com.blue.open.Code.Autorizacion.ReglaEvaluable;
import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Code.Proyecto.ProyectoUsuarioResponseDTO;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.ReglaPermiso.ReglaPermisoRepository;
import com.blue.open.Code.Tablero.TableroRepository;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Caso de uso: usuarios con acceso a un proyecto (CU-PRY-06, decision del 19/11 sobre la adenda propuesta 42), para
 * que cualquier usuario del proyecto elija al responsable de una tarea. Tienen acceso el super admin, los
 * Administradores y quienes pueden VER el proyecto o alguno de sus tableros por sus reglas o las de sus grupos
 * (EvaluadorDeReglasDePermiso). Exige VER en el proyecto.
 * Dependencias externas: la base de datos (cuatro consultas, sin importar cuantos usuarios haya).
 * Recibe: uuid del proyecto. Responde: lista de ProyectoUsuarioResponseDTO por nombre de usuario. Lanza
 * REGISTRO_NO_ENCONTRADO o SIN_PERMISO.
 */
@Service
@RequiredArgsConstructor
public class ProyectoUsuariosUseCase {

    // Grupos que acceden a todos los proyectos
    private static final List<String> GRUPOS_CON_ACCESO_TOTAL = List.of("SYSADMIN", "ADMIN");

    private final ProyectoRepository proyectoRepository;

    private final TableroRepository tableroRepository;

    private final ReglaPermisoRepository reglaPermisoRepository;

    private final UsuarioRepository usuarioRepository;

    private final ResolutorDePermisosDeRecurso permisos;

    @Transactional(readOnly = true)
    public List<ProyectoUsuarioResponseDTO> ejecutar(String proyectoUuid) {
        Proyecto proyecto = proyectoRepository.findFirstByUuidAndStatus(proyectoUuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "ProyectoUsuariosUseCase | ejecutar | proyecto " + proyectoUuid));
        permisos.exigir(NivelDePermiso.VER, proyecto);
        Set<Long> conAcceso = new HashSet<>(usuarioRepository.idsDeLosGrupos(GRUPOS_CON_ACCESO_TOTAL));
        conAcceso.addAll(porReglas(proyecto.getId()));
        return conAcceso.isEmpty() ? List.of() : usuarioRepository.vigentesParaElProyecto(conAcceso);
    }

    // Ids de los usuarios que pueden ver el proyecto o alguno de sus tableros por sus reglas o las de sus grupos
    private Set<Long> porReglas(Long idProyecto) {
        Map<Long, List<ReglaEvaluable>> porUsuario = Stream.concat(
                        reglaPermisoRepository.reglasDeUsuariosDelProyecto(idProyecto).stream(),
                        reglaPermisoRepository.reglasDeGruposDelProyecto(idProyecto).stream())
                .collect(Collectors.groupingBy(ReglaDeUnUsuario::idUsuario,
                        Collectors.mapping(ReglaDeUnUsuario::evaluable, Collectors.toList())));
        List<Long> tableros = tableroRepository.idsVigentesDelProyecto(idProyecto);
        return porUsuario.entrySet().stream()
                .filter(reglas -> EvaluadorDeReglasDePermiso.puede(reglas.getValue(), NivelDePermiso.VER, null, idProyecto)
                        || tableros.stream().anyMatch(tablero ->
                                EvaluadorDeReglasDePermiso.puede(reglas.getValue(), NivelDePermiso.VER, tablero, idProyecto)))
                .map(Map.Entry::getKey).collect(Collectors.toSet());
    }
}
