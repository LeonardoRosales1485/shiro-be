package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.DTO.PermisoDTO;
import com.blue.open.Core.Identity.DTO.RolDTO;
import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica la personalizacion del proyecto sobre la regla del Core: un rol declarado en RolEnum
 * se puede asignar aunque el solicitante no tenga todos sus permisos; el resto sigue la regla base.
 */
class RolServiceTest {

    private RolService rolService;
    private UsuarioBase usuarioActual;

    @BeforeEach
    void preparar() {
        // puedeAsignarRol no usa las dependencias de RolService
        rolService = new RolService(null, null, null);
        usuarioActual = new UsuarioBase() {
        };
        usuarioActual.setId(1L);
    }

    private RolDTO rol(String nombre, String... permisos) {
        RolDTO dto = new RolDTO();
        dto.setNombre(nombre);
        dto.setPermisos(List.of(permisos).stream().map(p -> {
            PermisoDTO permisoDto = new PermisoDTO();
            permisoDto.setNombre(p);
            return permisoDto;
        }).toList());
        return dto;
    }

    @Test
    void rolFueraDelEnum_conPermisoQueNoPosee_noSePuedeAsignar() {
        boolean permitido = rolService.puedeAsignarRol(
                rol("ROL_CUALQUIERA", "user.delete"), usuarioActual, Set.of("user.view"));
        assertFalse(permitido);
    }

    @Test
    void rolFueraDelEnum_conTodosLosPermisos_sePuedeAsignar() {
        boolean permitido = rolService.puedeAsignarRol(
                rol("ROL_CUALQUIERA", "user.view"), usuarioActual, Set.of("user.view"));
        assertTrue(permitido);
    }

    @Test
    void rolSinPermisos_sePuedeAsignar() {
        boolean permitido = rolService.puedeAsignarRol(rol("ROL_VACIO"), usuarioActual, Set.of());
        assertTrue(permitido);
    }
}
