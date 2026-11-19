package com.blue.open.Code.Comentario;

import com.blue.open.Code.Common.Canal.CanalDeCambio;
import com.blue.open.Code.Common.Canal.CanalDeLaPeticion;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Core.Crud.AbstractService;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * ABM de comentarios (CU-COM-01 a CU-COM-03) sobre los hooks de AbstractService. Al crear: el autor es el usuario
 * de la sesion, el canal el del request y la fecha, ahora. Al editar solo cambia el cuerpo, y solo lo edita su
 * autor (SIN_PERMISO). Cada guardado deja su fila COMENTAR en el historial de la tarea.
 * Dependencias externas: la base de datos.
 */
@Service
@RequiredArgsConstructor
public class ComentarioService extends AbstractService<Comentario, ComentarioDTO, ComentarioDTO> {

    private final UsuarioRepository usuarioRepository;

    private final CanalDeLaPeticion canalDeLaPeticion;

    private final RegistradorDeHistorial historial;

    /*
     * Aplica el DTO: en un alta completa autor, canal y fecha; en una edicion valida que edite el autor y conserva
     * tarea, autor, canal y fecha. Recuerda el cuerpo anterior para el historial. Lanza SIN_PERMISO si edita otro.
     */
    @Override
    protected Comentario hookGuardarAplicarDto(ComentarioDTO dto, Comentario modelo) {
        boolean esAlta = modelo.getId() == null;
        Long idUsuario = usuarioActual.getIdUsuarioActual();
        if (!esAlta && !modelo.getAutor().getId().equals(idUsuario))
            throw new ExcepcionDeApi(CodigoDeErrorKernel.SIN_PERMISO, "Solo el autor edita su comentario.",
                    "ComentarioService | hookGuardarAplicarDto | comentario " + modelo.getUuid());
        Tarea tareaActual = modelo.getTarea();
        Usuario autorActual = modelo.getAutor();
        CanalDeCambio canalActual = modelo.getCanal();
        Timestamp fechaActual = modelo.getFecha();
        modelo.setCuerpoAnterior(modelo.getCuerpo());
        // Autor, canal y fecha los decide el servidor: se descartan antes de copiar el DTO
        dto.setAutorUuid(null);
        if (!esAlta)
            dto.setTareaUuid(tareaActual.getUuid());
        Comentario aplicado = super.hookGuardarAplicarDto(dto, modelo);
        if (esAlta) {
            aplicado.setAutor(usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorKernel.NO_AUTENTICADO,
                            "ComentarioService | hookGuardarAplicarDto | sin usuario de sesion")));
            aplicado.setCanal(canalDeLaPeticion.actual());
            aplicado.setFecha(new Timestamp(System.currentTimeMillis()));
        } else {
            aplicado.setAutor(autorActual);
            aplicado.setCanal(canalActual);
            aplicado.setFecha(fechaActual);
        }
        return aplicado;
    }

    // Guarda el comentario y escribe su fila COMENTAR en el historial de la tarea
    @Override
    protected Comentario hookGuardarEnBase(Comentario modelo) {
        boolean esAlta = modelo.getId() == null;
        Comentario guardado = super.hookGuardarEnBase(modelo);
        String tarea = guardado.getTarea().getUuid();
        if (esAlta)
            historial.registrar(Historial.TAREA, tarea, AccionDeHistorial.COMENTAR, null, null, guardado.getCuerpo());
        else if (!Objects.equals(modelo.getCuerpoAnterior(), guardado.getCuerpo()))
            historial.registrar(Historial.TAREA, tarea, AccionDeHistorial.COMENTAR, "comentario",
                    modelo.getCuerpoAnterior(), guardado.getCuerpo());
        return guardado;
    }
}
