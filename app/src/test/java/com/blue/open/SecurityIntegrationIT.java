package com.blue.open;

import com.blue.open.Code.Empresa.Empresa;
import com.blue.open.Code.Empresa.EmpresaRepository;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Core.Identity.Entidad.Rol;
import com.blue.open.Core.Identity.Repository.RolRepository;
import com.blue.open.Core.Identity.Service.AuthService;
import com.blue.open.Core.Multiempresa.Entidad.UsuarioEmpresa;
import com.blue.open.Core.Multiempresa.Repository.UsuarioEmpresaRepository;
import com.blue.open.Core.Multiempresa.Service.ContextoDeEmpresa;
import com.blue.open.Core.Test.TestConBaseDeDatos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test de integracion (no unitario): levanta el contexto de Spring completo sobre el PostgreSQL embebido de
// core-test (master + seeds de Liquibase, rollback por test), por eso corre en la fase "verify" de Maven
// (maven-failsafe-plugin) y no con "mvn test". Mockea AuthService y ContextoDeEmpresa para probar el scoping
// por empresa de un admin local, que las pruebas de contrato no cubren.
class SecurityIntegrationIT extends TestConBaseDeDatos {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ContextoDeEmpresa contextoDeEmpresa;

    private Usuario adminLocal;
    private Empresa empresaCliente;

    @BeforeEach
    void preparar() {
        // AuthService es un mock: el permiso de @Contrato se da por cumplido y se prueban las reglas de cada service
        when(authService.tieneAlgunRolOPermisoPorNombre(any(), any(), any())).thenReturn(true);

        // 1. Empresas: la de sistema (uuid 1, de los datos requeridos) y una empresa cliente nueva
        Empresa empresaDeSistema = empresaRepository.buscarPorUuid("1").orElseThrow();

        empresaCliente = new Empresa();
        empresaCliente.setNombre("Client Co 2");
        empresaCliente.setUuid("uuid-co-2");
        empresaCliente = empresaRepository.save(empresaCliente);

        // 2. Roles: uno global de sistema y uno local de la empresa cliente
        Rol rolDeSistema = new Rol();
        rolDeSistema.setNombre("MANAGER");
        rolDeSistema.setIdEmpresa(empresaDeSistema.getId());
        rolDeSistema.setStatus(0);
        rolRepository.save(rolDeSistema);

        Rol rolLocal = new Rol();
        rolLocal.setNombre("LOCAL_WORKER");
        rolLocal.setIdEmpresa(empresaCliente.getId());
        rolLocal.setStatus(0);
        rolRepository.save(rolLocal);

        Rol rolAdmin = new Rol();
        rolAdmin.setNombre("ADMIN");
        rolAdmin.setIdEmpresa(empresaCliente.getId());
        rolAdmin.setStatus(0);
        rolAdmin = rolRepository.save(rolAdmin);

        // 3. Usuario admin de la empresa cliente
        adminLocal = new Usuario();
        adminLocal.setNombreDeUsuario("admin2");
        adminLocal.setEmail("admin@co2.com");
        adminLocal.setStatus(0);
        adminLocal = usuarioRepository.save(adminLocal);

        UsuarioEmpresa relacion = new UsuarioEmpresa();
        relacion.setIdUsuario(adminLocal.getId());
        relacion.setIdEmpresa(empresaCliente.getId());
        relacion.setRoles(List.of(rolAdmin));
        usuarioEmpresaRepository.save(relacion);
    }

    @Test
    void paginarRoles_comoAdminLocal_respondeOk() throws Exception {
        when(authService.getActualObligatorio()).thenReturn(adminLocal);
        when(authService.autenticar()).thenReturn(adminLocal);
        when(authService.esSysAdmin(adminLocal)).thenReturn(false);
        when(contextoDeEmpresa.getEmpresaActualObligatoria()).thenReturn(empresaCliente);

        Map<String, Object> request = new HashMap<>();
        request.put("pagina", 1);
        request.put("tamano", 10);

        mockMvc.perform(post("/rol/paginar")
                .header("X-Empresa-Uuid", "uuid-co-2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // Paginado JPQL con la condicion por empresa (subconsulta a UsuarioEmpresa) y rolesPorEmpresa
    // completado en una sola consulta para toda la pagina
    @Test
    void paginarUsuarios_comoAdminLocal_soloVeUsuariosDeSuEmpresa() throws Exception {
        Usuario otraEmpresa = new Usuario();
        otraEmpresa.setNombreDeUsuario("usuario.otra.empresa");
        otraEmpresa.setEmail("otro@co1.com");
        otraEmpresa.setStatus(0);
        usuarioRepository.save(otraEmpresa);

        when(authService.getActualObligatorio()).thenReturn(adminLocal);
        when(authService.autenticar()).thenReturn(adminLocal);
        when(authService.esSysAdmin(adminLocal)).thenReturn(false);
        when(contextoDeEmpresa.getEmpresaActualObligatoria()).thenReturn(empresaCliente);

        Map<String, Object> request = new HashMap<>();
        request.put("pagina", 1);
        request.put("tamano", 10);
        request.put("filtros", List.of(List.of(Map.of("campo", "status", "operacion", "=", "valor", "0"))));

        mockMvc.perform(post("/usuario/paginar")
                .header("X-Empresa-Uuid", "uuid-co-2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].nombreDeUsuario").value("admin2"))
                .andExpect(jsonPath("$.contenido[0].rolesPorEmpresa[0].empresaNombre").value("Client Co 2"))
                .andExpect(jsonPath("$.contenido[0].rolesPorEmpresa[0].roles[0].nombre").value("ADMIN"));
    }

    @Test
    void configuracionesPublicas_sinSesion_respondeOk() throws Exception {
        when(authService.tienePermisos(any())).thenReturn(false);

        mockMvc.perform(get("/configuracion/listar"))
                .andExpect(status().isOk());
    }

    @Test
    void guardarConfiguracion_sinValor_responde400() throws Exception {
        List<Map<String, Object>> request = List.of(Map.of("nombre", "config.sin.valor", "valor", "", "global", true));

        mockMvc.perform(post("/configuracion/guardar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void guardarEmpresa_conCuitInvalido_responde400() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("nombre", "Empresa cuit invalido");
        request.put("cuit", "20-12345678-0");

        mockMvc.perform(post("/empresa/guardar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensajeParaUsuario").value("CUIT inválido"));
    }
}
