package com.blue.open.Code.Tarea.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Comentario.Comentario;
import com.blue.open.Code.Comentario.ComentarioRepository;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Relacion.TipoDeRelacion;
import com.blue.open.Code.Relacion.VinculadorDeTareas;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Code.Tablero.TableroRepository;
import com.blue.open.Code.Tarea.DatoAClonar;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaClonarRequestDTO;
import com.blue.open.Code.Tarea.TareaDTO;
import com.blue.open.Code.Tarea.TareaEtiqueta;
import com.blue.open.Code.Tarea.TareaEtiquetaDTO;
import com.blue.open.Code.Tarea.TareaEtiquetaRepository;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Code.Tarea.TareaService;
import com.blue.open.Code.TipoTarea.TipoTareaRepository;
import com.blue.open.Code.Version.TareaVersion;
import com.blue.open.Code.Version.TareaVersionRepository;
import com.blue.open.Code.Version.Version;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Caso de uso: clona una tarea vigente a un tablero del mismo proyecto (T1.10 y adenda 1 del DAF). Titulo y tipo
 * siempre; el resto segun la seleccion (por defecto descripcion, prioridad y etiquetas). El clon nace como cualquier
 * alta (TareaService.guardar: clave nueva, estado inicial del tablero de destino, informante = quien clona). Los
 * comentarios copiados conservan autor, fecha y canal; las subtareas se clonan, con la misma seleccion, como hijas
 * del clon en el tablero de destino. El clon no hereda el padre; el de una Subtarea es una tarea propia o subtarea de otra, a eleccion. El original y el clon quedan vinculados con una relacion CLONA, que muestra el
 * estado del otro, y el clon deja una fila CLONAR en su historial.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la tarea y TareaClonarRequestDTO. Responde: TareaDTO del clon. Lanza REGISTRO_NO_ENCONTRADO,
 * RELACION_NO_ENCONTRADA o TABLERO_DE_OTRO_PROYECTO.
 */
@Service
@RequiredArgsConstructor
public class TareaClonarUseCase {

    private static final int VIGENTE = 0;

    private static final int NIVEL_SUBTAREA = 3;

    // Nivel de las tareas sin padre obligatorio que puede ser el clon de una Subtarea: Historia, Tarea y Bug
    private static final int NIVEL_DE_TAREA_PROPIA = 2;

    private final TareaRepository tareaRepository;

    private final TableroRepository tableroRepository;

    private final TareaEtiquetaRepository etiquetaRepository;

    private final ComentarioRepository comentarioRepository;

    private final TareaVersionRepository tareaVersionRepository;

    private final TareaService tareaService;

    private final VinculadorDeTareas vinculador;

    private final RegistradorDeHistorial historial;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final Proyector proyector;

    private final ResolutorDePermisosDeRecurso permisos;

    private final TipoTareaRepository tipoTareaRepository;

    @Transactional
    public TareaDTO ejecutar(String uuid, TareaClonarRequestDTO dto) {
        Tarea original = tareaRepository.findFirstByUuidAndStatus(uuid, VIGENTE)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "TareaClonarUseCase | ejecutar | tarea " + uuid));
        permisos.exigir(NivelDePermiso.VER, original.getTablero());
        Tablero destino = tableroRepository.findFirstByUuidAndStatus(dto.getTableroUuid(), VIGENTE)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.RELACION_NO_ENCONTRADA,
                        "TareaClonarUseCase | ejecutar | tablero " + dto.getTableroUuid()).conDato("campo", "tableroUuid"));
        if (!destino.getProyecto().getId().equals(original.getProyecto().getId()))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.TABLERO_DE_OTRO_PROYECTO,
                    "TareaClonarUseCase | ejecutar | tarea " + original.getClave() + " | tablero " + destino.getUuid());
        Set<DatoAClonar> copiar = Set.copyOf(dto.getCopiar() == null ? DatoAClonar.POR_DEFECTO : dto.getCopiar());
        // El clon no hereda el padre. Una Subtarea no puede quedar sin padre (adenda 4 del DAF): quien clona elige si
        // el clon es una tarea propia de nivel 2 o una subtarea de otra tarea (resolucion A2 del 19/11)
        Tarea clon = original.getTipo().getNivel() == NIVEL_SUBTAREA
                ? clonar(original, destino, copiar, destinoDeSubtarea(original, dto))
                : clonar(original, destino, copiar, new DestinoDelClon(original.getTipo().getCodigo(), null));
        vinculador.vincular(original, clon, TipoDeRelacion.CLONA);
        historial.registrar(Historial.TAREA, clon.getUuid(), AccionDeHistorial.CLONAR, null, null, original.getClave());
        return proyector.porUuid(Tarea.class, clon.getUuid(), TareaDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "TareaClonarUseCase | ejecutar | no se pudo proyectar " + clon.getUuid()));
    }

    // Crea el clon de la tarea en el tablero (hijo del padre recibido, o sin padre) y, si se pidio, sus comentarios y
    // sus subtareas. Responde la tarea clonada
    private Tarea clonar(Tarea original, Tablero destino, Set<DatoAClonar> copiar, DestinoDelClon destinoDelClon) {
        TareaDTO alta = new TareaDTO();
        alta.setTableroUuid(destino.getUuid());
        alta.setTipoCodigo(destinoDelClon.tipoCodigo());
        alta.setTitulo(original.getTitulo());
        alta.setPadreUuid(destinoDelClon.padreUuid());
        if (copiar.contains(DatoAClonar.DESCRIPCION))
            alta.setDescripcion(original.getDescripcion());
        if (copiar.contains(DatoAClonar.PRIORIDAD))
            alta.setPrioridad(original.getPrioridad());
        if (copiar.contains(DatoAClonar.PUNTOS))
            alta.setPuntos(original.getPuntos());
        if (copiar.contains(DatoAClonar.RESPONSABLE) && original.getResponsable() != null)
            alta.setResponsableUuid(original.getResponsable().getUuid());
        if (copiar.contains(DatoAClonar.ETIQUETAS))
            alta.setEtiquetas(etiquetasDe(original));
        String uuidDelClon = tareaService.guardar(alta).getUuid();
        Tarea clon = tareaRepository.findFirstByUuidAndStatus(uuidDelClon, VIGENTE)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "TareaClonarUseCase | clonar | clon " + uuidDelClon));
        if (copiar.contains(DatoAClonar.VERSIONES))
            copiarVersiones(original, clon);
        if (copiar.contains(DatoAClonar.COMENTARIOS))
            copiarComentarios(original, clon);
        if (copiar.contains(DatoAClonar.SUBTAREAS)) {
            for (Tarea hija : tareaRepository.findByPadreInAndStatus(List.of(original), VIGENTE))
                clonar(hija, destino, copiar, new DestinoDelClon(hija.getTipo().getCodigo(), uuidDelClon));
        }
        return clon;
    }

    // Etiquetas vigentes de la tarea, como DTO para el alta del clon
    private List<TareaEtiquetaDTO> etiquetasDe(Tarea tarea) {
        List<TareaEtiquetaDTO> etiquetas = new ArrayList<>();
        for (TareaEtiqueta etiqueta : etiquetaRepository.findByTareaAndStatus(tarea, VIGENTE)) {
            TareaEtiquetaDTO copia = new TareaEtiquetaDTO();
            copia.setTexto(etiqueta.getTexto());
            etiquetas.add(copia);
        }
        return etiquetas;
    }

    // Asigna el clon a las versiones sin publicar del original (una version publicada no cambia)
    private void copiarVersiones(Tarea original, Tarea clon) {
        for (Version version : tareaVersionRepository.versionesSinPublicarDe(original))
            tareaVersionRepository.save(new TareaVersion(clon, version));
    }

    // Copia los comentarios vigentes del original al clon, con su autor, fecha y canal originales
    private void copiarComentarios(Tarea original, Tarea clon) {
        for (Comentario comentario : comentarioRepository.findByTareaAndStatusOrderByFechaAsc(original, VIGENTE)) {
            Comentario copia = new Comentario();
            copia.setTarea(clon);
            copia.setAutor(comentario.getAutor());
            copia.setCuerpo(comentario.getCuerpo());
            copia.setCanal(comentario.getCanal());
            copia.setFecha(comentario.getFecha());
            copia.setIdUsuarioDeCreacion(usuarioActual.getIdUsuarioActual());
            comentarioRepository.save(copia);
        }
    }

    /*
     * Tipo y padre del clon de una Subtarea: tarea propia (tipoCodigo de nivel 2, sin padre) o subtarea de otra tarea
     * (padreUuid; TareaService valida que sea del proyecto y de nivel menor). Lanza CLON_DE_SUBTAREA_SIN_DESTINO si no
     * viene ninguno o vienen los dos, y TIPO_DE_CLON_INVALIDO si el tipo no es de nivel 2.
     */
    private DestinoDelClon destinoDeSubtarea(Tarea original, TareaClonarRequestDTO dto) {
        boolean conTipo = dto.getTipoCodigo() != null && !dto.getTipoCodigo().isBlank();
        boolean conPadre = dto.getPadreUuid() != null && !dto.getPadreUuid().isBlank();
        if (conTipo == conPadre)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CLON_DE_SUBTAREA_SIN_DESTINO,
                    "TareaClonarUseCase | destinoDeSubtarea | " + original.getClave());
        if (conPadre)
            return new DestinoDelClon(original.getTipo().getCodigo(), dto.getPadreUuid());
        String tipo = dto.getTipoCodigo().toUpperCase(Locale.ROOT);
        boolean nivelDos = tipoTareaRepository.findFirstByCodigoAndStatus(tipo, VIGENTE).filter(t -> t.getNivel() == NIVEL_DE_TAREA_PROPIA).isPresent();
        if (!nivelDos)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.TIPO_DE_CLON_INVALIDO, "TareaClonarUseCase | destinoDeSubtarea | tipo " + tipo);
        return new DestinoDelClon(tipo, null);
    }

    /**
     * Tipo y padre (o null) con que se crea un clon.
     */
    private record DestinoDelClon(String tipoCodigo, String padreUuid) {
    }
}
