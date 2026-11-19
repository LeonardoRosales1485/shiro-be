package com.blue.open.Code.Usuario.CasosDeUso;

import com.blue.open.Code.Usuario.GrupoAsignado;
import com.blue.open.Code.Usuario.UsuarioAdministracionGrupoDTO;
import com.blue.open.Code.Usuario.UsuarioAdministracionResponseDTO;
import com.blue.open.Code.Usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso: listado de administracion de usuarios (CU-USR-26, T6.5, prototipo "Administracion · usuarios"):
 * todos los usuarios vigentes y archivados con su forma de ingreso y sus grupos, para que la pantalla filtre por
 * texto, grupo, ingreso y archivados sin pedir de nuevo. Dos consultas, sin N+1.
 * Dependencias externas: la base de datos.
 * Recibe: nada. Responde: lista de UsuarioAdministracionResponseDTO por nombre.
 */
@Service
@RequiredArgsConstructor
public class UsuarioListadoDeAdministracionUseCase {

    private final UsuarioRepository repository;

    @Transactional(readOnly = true)
    public List<UsuarioAdministracionResponseDTO> ejecutar() {
        List<UsuarioAdministracionResponseDTO> usuarios = repository.paraAdministrar();
        Map<String, UsuarioAdministracionResponseDTO> porUuid = usuarios.stream()
                .collect(Collectors.toMap(UsuarioAdministracionResponseDTO::getUuid, Function.identity()));
        for (GrupoAsignado asignado : repository.gruposAsignados()) {
            UsuarioAdministracionResponseDTO usuario = porUuid.get(asignado.usuarioUuid());
            if (usuario != null)
                usuario.getGrupos().add(new UsuarioAdministracionGrupoDTO(asignado.grupoUuid(), asignado.nombre(), asignado.desde()));
        }
        return usuarios;
    }
}
