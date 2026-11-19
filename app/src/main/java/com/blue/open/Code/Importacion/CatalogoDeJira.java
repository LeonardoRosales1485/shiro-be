package com.blue.open.Code.Importacion;

import com.blue.open.Code.Common.Cifrado.CifradorDeCredenciales;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Arma el catalogo de Jira de una importacion (CU-IMP-01 y CU-IMP-02): tableros, estados, tipos y usuarios del
 * proyecto, con cada usuario vinculado por correo al de Shiro. Tambien arma las credenciales de una importacion
 * guardada, descifrando su token. Lo usan conectar, leer el catalogo, mapear y el importador.
 * Dependencias externas: Jira (ClienteDeJira) y la base de datos.
 */
@Component
@RequiredArgsConstructor
public class CatalogoDeJira {

    private final ClienteDeJira jira;

    private final CifradorDeCredenciales cifrador;

    private final UsuarioRepository usuarioRepository;

    // Credenciales de una importacion guardada. Lanza CIFRADO_NO_CONFIGURADO o CREDENCIAL_NO_DESCIFRABLE
    public CredencialesDeJira credenciales(Importacion importacion) {
        return new CredencialesDeJira(importacion.getUrlJira(), importacion.getCorreo(),
                cifrador.descifrar(importacion.getTokenCifrado()));
    }

    // Catalogo del proyecto de Jira de la importacion. Lanza los errores de ClienteDeJira
    public ImportacionCatalogoDTO catalogo(Importacion importacion, CredencialesDeJira credenciales) {
        String clave = importacion.getClaveProyectoJira();
        DatosDeJira.Proyecto proyecto = jira.proyecto(credenciales, clave);
        ImportacionCatalogoDTO catalogo = new ImportacionCatalogoDTO();
        catalogo.setImportacionUuid(importacion.getUuid());
        catalogo.setNombreProyectoJira(proyecto.nombre());
        catalogo.setTableros(jira.tableros(credenciales, clave));
        catalogo.setEstados(jira.estados(credenciales, clave));
        catalogo.setTipos(jira.tipos(credenciales, proyecto.id()));
        List<DatosDeJira.Usuario> usuarios = jira.usuarios(credenciales, clave);
        Map<String, Usuario> deShiro = usuariosPorCorreo(usuarios.stream().map(DatosDeJira.Usuario::correo).toList());
        catalogo.setUsuarios(usuarios.stream().map(usuario -> new UsuarioDeJiraDTO(usuario.correo(), usuario.nombre(),
                usuario.correo() == null || !deShiro.containsKey(usuario.correo()) ? null
                        : deShiro.get(usuario.correo()).getUuid())).toList());
        return catalogo;
    }

    // Usuarios vigentes de Shiro por correo, para los correos recibidos (los null se ignoran), en una consulta
    public Map<String, Usuario> usuariosPorCorreo(Collection<String> correos) {
        List<String> validos = correos.stream().filter(Objects::nonNull).distinct().toList();
        Map<String, Usuario> porCorreo = new HashMap<>();
        if (!validos.isEmpty())
            usuarioRepository.findByEmailInAndStatus(validos, 0).forEach(usuario -> porCorreo.put(usuario.getEmail(), usuario));
        return porCorreo;
    }
}
