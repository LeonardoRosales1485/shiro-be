package com.blue.open.Code.Tarea;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Estado.Estado;
import com.blue.open.Code.Estado.EstadoRepository;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Code.TipoTarea.TipoTarea;
import com.blue.open.Code.TipoTarea.TipoTareaRepository;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Core.Crud.AbstractService;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * ABM de tareas (CU-TAR-01 a CU-TAR-03) sobre los hooks de AbstractService. Al crear: la tarea va al proyecto de su
 * tablero, nace en el estado inicial del tablero, con prioridad Media y con quien la crea como informante, y recibe
 * la clave del contador del proyecto, bloqueado por fila. Al editar no cambian la clave, el estado ni el tablero.
 * Dependencias externas: la base de datos.
 */
@Service
@RequiredArgsConstructor
public class TareaService extends AbstractService<Tarea, TareaDTO, TareaDTO> {

    private static final int VIGENTE = 0;

    private static final int ELIMINADO = -1;

    private final TipoTareaRepository tipoTareaRepository;

    private final EstadoRepository estadoRepository;

    private final ProyectoRepository proyectoRepository;

    private final UsuarioRepository usuarioRepository;

    private final TareaEtiquetaRepository tareaEtiquetaRepository;

    /*
     * Aplica el DTO con las reglas de la tarea. Recibe el DTO validado y la tarea (nueva o vigente). Resuelve el tipo
     * por codigo (TIPO_DE_TAREA_INEXISTENTE), rechaza cambiar el tablero (TABLERO_DE_TAREA_NO_SE_CAMBIA), ignora el
     * proyecto y el estado que mande el cliente, completa prioridad e informante y, en un alta, asigna estado inicial
     * y clave. Responde la tarea lista para guardar.
     */
    @Override
    protected Tarea hookGuardarAplicarDto(TareaDTO dto, Tarea modelo) {
        boolean esAlta = modelo.getId() == null;
        if (!esAlta && !modelo.getTablero().getUuid().equals(dto.getTableroUuid()))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.TABLERO_DE_TAREA_NO_SE_CAMBIA,
                    "TareaService | hookGuardarAplicarDto | tarea " + modelo.getClave() + " | tablero pedido "
                            + dto.getTableroUuid());
        TipoTarea tipo = tipoTareaRepository.findFirstByCodigoAndStatus(dto.getTipoCodigo(), VIGENTE)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorDelProyecto.TIPO_DE_TAREA_INEXISTENTE,
                        "TareaService | hookGuardarAplicarDto | tipo " + dto.getTipoCodigo()));
        Proyecto proyectoActual = modelo.getProyecto();
        Estado estadoActual = modelo.getEstado();
        Usuario informanteActual = modelo.getInformante();
        PrioridadDeTarea prioridadActual = modelo.getPrioridad();
        // Proyecto y estado los decide el servidor: se descartan antes de que el Proyector asigne las relaciones
        dto.setProyectoUuid(null);
        dto.setEstadoUuid(null);

        Tarea aplicada = super.hookGuardarAplicarDto(dto, modelo);
        aplicada.setTipo(tipo);
        aplicada.setPrioridad(dto.getPrioridad() != null ? dto.getPrioridad()
                : esAlta ? PrioridadDeTarea.MEDIA : prioridadActual);
        if (aplicada.getInformante() == null)
            aplicada.setInformante(esAlta ? usuarioDeLaSesion() : informanteActual);
        aplicada.setEtiquetasPedidas(textosDeEtiquetas(dto));
        if (!esAlta) {
            aplicada.setProyecto(proyectoActual);
            aplicada.setEstado(estadoActual);
            return aplicada;
        }
        Tablero tablero = aplicada.getTablero();
        aplicada.setEstado(estadoRepository.findFirstByTipoTableroAndInicialTrueAndStatus(tablero.getTipoTablero(), VIGENTE)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorKernel.ERROR_INTERNO,
                        "TareaService | hookGuardarAplicarDto | el tablero " + tablero.getUuid() + " no tiene estado inicial")));
        // Bloquea la fila del proyecto hasta el commit: dos altas simultaneas no reciben el mismo numero (Spec 7)
        Proyecto proyecto = proyectoRepository.bloquearPorId(tablero.getProyecto().getId())
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.RELACION_NO_ENCONTRADA,
                        "TareaService | hookGuardarAplicarDto | el proyecto del tablero " + tablero.getUuid() + " no esta vigente")
                        .conDato("campo", "tableroUuid"));
        aplicada.setProyecto(proyecto);
        aplicada.numerar(proyecto.getPrefijo(), proyecto.siguienteNumero());
        return aplicada;
    }

    /*
     * Guarda la tarea y sincroniza sus etiquetas con las pedidas: crea las nuevas y deja con status -1 las que ya no
     * vienen (nada se borra). Recibe la tarea aplicada; responde la tarea guardada.
     */
    @Override
    protected Tarea hookGuardarEnBase(Tarea modelo) {
        Tarea guardada = super.hookGuardarEnBase(modelo);
        Set<String> pedidas = new LinkedHashSet<>(modelo.getEtiquetasPedidas());
        for (TareaEtiqueta etiqueta : tareaEtiquetaRepository.findByTareaAndStatus(guardada, VIGENTE)) {
            if (!pedidas.remove(etiqueta.getTexto())) {
                etiqueta.setStatus(ELIMINADO);
                etiqueta.setFechaDeBorrado(new Timestamp(System.currentTimeMillis()));
                etiqueta.setIdUsuarioDeBorrado(usuarioActual.getIdUsuarioActual());
                tareaEtiquetaRepository.save(etiqueta);
            }
        }
        for (String texto : pedidas) {
            TareaEtiqueta nueva = new TareaEtiqueta(guardada, texto);
            nueva.setIdUsuarioDeCreacion(usuarioActual.getIdUsuarioActual());
            tareaEtiquetaRepository.save(nueva);
        }
        return guardada;
    }

    // Usuario de la sesion, que es el informante de una tarea nueva. Lanza NO_AUTENTICADO si no hay sesion
    private Usuario usuarioDeLaSesion() {
        Long id = usuarioActual.getIdUsuarioActual();
        if (id == null)
            throw new ExcepcionDeApi(CodigoDeErrorKernel.NO_AUTENTICADO, "TareaService | usuarioDeLaSesion | sin sesion");
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorKernel.NO_AUTENTICADO,
                        "TareaService | usuarioDeLaSesion | no existe el usuario de la sesion"));
    }

    // Textos de las etiquetas del DTO, sin espacios en los extremos y sin repetir, en el orden recibido
    private static List<String> textosDeEtiquetas(TareaDTO dto) {
        Set<String> textos = new LinkedHashSet<>();
        for (TareaEtiquetaDTO etiqueta : dto.getEtiquetas()) {
            textos.add(etiqueta.getTexto().strip());
        }
        return List.copyOf(textos);
    }
}
