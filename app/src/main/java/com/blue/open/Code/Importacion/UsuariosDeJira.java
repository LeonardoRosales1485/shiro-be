package com.blue.open.Code.Importacion;

import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Resuelve el usuario de Shiro de cada persona de Jira (resolucion B34 del 19/11): el que tiene el mismo correo, el
 * que ya se creo en una importacion anterior o uno nuevo sin acceso (sin contrasena ni grupos) llamado «Usuario
 * importado» y el nombre de Jira, que queda registrado en shr_mapeo_jira. Si despues esa persona entra con Google con
 * el mismo correo, entra como ese usuario y conserva sus tareas.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class UsuariosDeJira {

    private static final String PREFIJO_DEL_NOMBRE = "Usuario importado ";

    private static final int LARGO_MAXIMO = 255;

    private final UsuarioRepository usuarioRepository;

    private final MapeosDeJira mapeos;

    private final CatalogoDeJira catalogo;

    // Busca de una vez los usuarios de Shiro por correo y los ya importados de las personas recibidas
    public void precargar(ContextoDeImportacion contexto, Collection<DatosDeJira.Usuario> personas) {
        List<DatosDeJira.Usuario> conDatos = personas.stream().filter(Objects::nonNull).toList();
        contexto.getUsuariosPorCorreo().putAll(catalogo.usuariosPorCorreo(conDatos.stream().map(DatosDeJira.Usuario::correo).toList()));
        List<String> ids = conDatos.stream().map(DatosDeJira.Usuario::identificacion).filter(Objects::nonNull).distinct().toList();
        Map<String, String> yaImportados = mapeos.uuids(TipoDeEntidadJira.USUARIO, ids);
        if (yaImportados.isEmpty())
            return;
        Map<String, Usuario> porUuid = new HashMap<>();
        usuarioRepository.findByUuidIn(yaImportados.values()).forEach(usuario -> porUuid.put(usuario.getUuid(), usuario));
        yaImportados.forEach((id, uuid) -> {
            if (porUuid.containsKey(uuid))
                contexto.getUsuariosImportados().put(id, porUuid.get(uuid));
        });
    }

    // Usuario de Shiro de la persona de Jira (null si no viene ninguna). Llamar antes a precargar
    public Usuario resolver(ContextoDeImportacion contexto, DatosDeJira.Usuario persona) {
        if (persona == null || persona.identificacion() == null)
            return null;
        if (persona.correo() != null && contexto.getUsuariosPorCorreo().containsKey(persona.correo()))
            return contexto.getUsuariosPorCorreo().get(persona.correo());
        Usuario importado = contexto.getUsuariosImportados().get(persona.identificacion());
        if (importado != null)
            return importado;
        Usuario nuevo = crear(persona, contexto.idUsuario());
        mapeos.registrar(TipoDeEntidadJira.USUARIO, persona.identificacion(), nuevo.getUuid(), contexto.idUsuario());
        contexto.getUsuariosImportados().put(persona.identificacion(), nuevo);
        contexto.getResumen().importado(nuevo.getNombre());
        return nuevo;
    }

    // Usuario sin acceso: sin contrasena ni grupos, con el correo de Jira (si lo muestra) y un nombre de usuario jira-{id}
    private Usuario crear(DatosDeJira.Usuario persona, Long idUsuario) {
        String visible = persona.nombre() != null ? persona.nombre() : persona.correo() != null ? persona.correo() : persona.identificacion();
        Usuario usuario = new Usuario();
        usuario.setNombre(recortar(PREFIJO_DEL_NOMBRE + visible));
        usuario.setNombreDeUsuario(recortar("jira-" + persona.identificacion().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9@._-]", "-")));
        usuario.setEmail(persona.correo());
        usuario.setIdUsuarioDeCreacion(idUsuario);
        return usuarioRepository.save(usuario);
    }

    // Recorta el texto al largo maximo de la columna de nombre del usuario
    private static String recortar(String texto) {
        return texto.length() > LARGO_MAXIMO ? texto.substring(0, LARGO_MAXIMO) : texto;
    }
}
