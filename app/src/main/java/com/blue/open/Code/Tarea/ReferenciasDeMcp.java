package com.blue.open.Code.Tarea;

import com.blue.open.Code.Estado.Estado;
import com.blue.open.Code.Estado.EstadoRepository;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Code.Tablero.TableroRepository;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Traduce los nombres que usan la gente y los agentes en MCP (clave SHI-123, prefijo, codigo de tablero, nombre de
 * estado, nombre de usuario) a los registros de Shiro. Si no existe, el error dice que se busco (Spec 4).
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class ReferenciasDeMcp {

    private final TareaRepository tareaRepository;

    private final ProyectoRepository proyectoRepository;

    private final TableroRepository tableroRepository;

    private final EstadoRepository estadoRepository;

    private final UsuarioRepository usuarioRepository;

    // Tarea por clave (vigente o archivada). Lanza REGISTRO_NO_ENCONTRADO si no existe
    public Tarea tarea(String clave) {
        String normalizada = clave == null ? "" : clave.trim().toUpperCase(Locale.ROOT);
        return tareaRepository.findFirstByClave(normalizada)
                .orElseThrow(() -> noExiste("No existe la tarea " + normalizada + ".", "tarea " + normalizada));
    }

    // Proyecto vigente por prefijo. Lanza REGISTRO_NO_ENCONTRADO si no existe
    public Proyecto proyecto(String prefijo) {
        String normalizado = prefijo == null ? "" : prefijo.trim().toUpperCase(Locale.ROOT);
        return proyectoRepository.findFirstByPrefijoAndStatus(normalizado, 0)
                .orElseThrow(() -> noExiste("No existe el proyecto " + normalizado + ".", "proyecto " + normalizado));
    }

    // Tablero vigente del proyecto por codigo (PRINCIPAL si es null). Lanza REGISTRO_NO_ENCONTRADO si no existe
    public Tablero tablero(Proyecto proyecto, String codigo) {
        String normalizado = codigo == null || codigo.isBlank() ? "PRINCIPAL" : codigo.trim().toUpperCase(Locale.ROOT);
        return tableroRepository.findFirstByProyectoAndTipoTableroCodigoAndStatus(proyecto, normalizado, 0)
                .orElseThrow(() -> noExiste("El proyecto " + proyecto.getPrefijo() + " no tiene el tablero " + normalizado
                        + ". Los tableros son PRINCIPAL, QA, ANALISIS y DISENO.", "tablero " + normalizado));
    }

    // Estado del tablero por nombre, sin distinguir mayusculas. Lanza REGISTRO_NO_ENCONTRADO con los nombres validos
    public Estado estado(Tablero tablero, String nombre) {
        List<Estado> estados = estadoRepository.findByTipoTableroAndStatusOrderByOrden(tablero.getTipoTablero(), 0);
        return estados.stream().filter(estado -> estado.getNombre().equalsIgnoreCase(nombre == null ? "" : nombre.trim()))
                .findFirst()
                .orElseThrow(() -> noExiste("El tablero no tiene el estado " + nombre + ". Los estados son: "
                        + String.join(", ", estados.stream().map(Estado::getNombre).toList()) + ".", "estado " + nombre));
    }

    // Estados vigentes del tablero, en orden de columna
    public List<Estado> estados(Tablero tablero) {
        return estadoRepository.findByTipoTableroAndStatusOrderByOrden(tablero.getTipoTablero(), 0);
    }

    // Usuario vigente por nombre de usuario. Lanza REGISTRO_NO_ENCONTRADO si no existe
    public Usuario usuario(String nombreDeUsuario) {
        return usuarioRepository.findFirstByNombreDeUsuarioAndStatus(nombreDeUsuario, 0)
                .orElseThrow(() -> noExiste("No existe el usuario " + nombreDeUsuario + ".", "usuario " + nombreDeUsuario));
    }

    private static ExcepcionDeApi noExiste(String mensaje, String detalle) {
        return new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, mensaje, "ReferenciasDeMcp | " + detalle);
    }
}
