package com.blue.open.Code.Relacion.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Relacion.Relacion;
import com.blue.open.Code.Relacion.RelacionCrearRequestDTO;
import com.blue.open.Code.Relacion.RelacionDTO;
import com.blue.open.Code.Relacion.RelacionRepository;
import com.blue.open.Code.Relacion.TipoDeRelacion;
import com.blue.open.Code.Relacion.VinculadorDeTareas;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: relaciona dos tareas vigentes del mismo proyecto (T1.9 y adenda 4 del DAF). No se relaciona una tarea consigo misma, la misma relacion no
 * se repite en ningun sentido y CLONA solo la crea clonar. Deja la fila RELACIONAR en el historial de las dos.
 * Dependencias externas: la base de datos.
 * Recibe: RelacionCrearRequestDTO. Responde: RelacionDTO. Lanza RELACION_NO_ENCONTRADA, RELACION_CONSIGO_MISMA,
 * RELACION_ENTRE_PROYECTOS, RELACION_YA_EXISTE o TIPO_DE_RELACION_NO_PERMITIDO.
 */
@Service
@RequiredArgsConstructor
public class RelacionCrearUseCase {

    private static final int VIGENTE = 0;

    private final TareaRepository tareaRepository;

    private final RelacionRepository relacionRepository;

    private final VinculadorDeTareas vinculador;

    private final Proyector proyector;

    private final ResolutorDePermisosDeRecurso permisos;

    @Transactional
    public RelacionDTO ejecutar(RelacionCrearRequestDTO dto) {
        if (dto.getTipo() == TipoDeRelacion.CLONA)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.TIPO_DE_RELACION_NO_PERMITIDO,
                    "RelacionCrearUseCase | ejecutar | tipo CLONA");
        Tarea origen = vigente(dto.getOrigenUuid(), "origenUuid");
        Tarea destino = vigente(dto.getDestinoUuid(), "destinoUuid");
        // Relacionar exige EDITAR en el tablero de origen y VER en el de destino
        permisos.exigir(NivelDePermiso.EDITAR, origen.getTablero());
        permisos.exigir(NivelDePermiso.VER, destino.getTablero());
        if (origen.getId().equals(destino.getId()))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.RELACION_CONSIGO_MISMA,
                    "RelacionCrearUseCase | ejecutar | tarea " + origen.getClave());
        if (!origen.getProyecto().getId().equals(destino.getProyecto().getId()))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.RELACION_ENTRE_PROYECTOS,
                    "RelacionCrearUseCase | ejecutar | " + origen.getClave() + " y " + destino.getClave());
        if (relacionRepository.existsByOrigenAndDestinoAndTipoAndStatus(origen, destino, dto.getTipo(), VIGENTE)
                || relacionRepository.existsByOrigenAndDestinoAndTipoAndStatus(destino, origen, dto.getTipo(), VIGENTE))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.RELACION_YA_EXISTE,
                    "RelacionCrearUseCase | ejecutar | " + origen.getClave() + " " + dto.getTipo() + " " + destino.getClave());
        Relacion relacion = vinculador.vincular(origen, destino, dto.getTipo());
        return proyector.porUuid(Relacion.class, relacion.getUuid(), RelacionDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "RelacionCrearUseCase | ejecutar | no se pudo proyectar " + relacion.getUuid()));
    }

    // Tarea vigente por uuid; si no existe lanza RELACION_NO_ENCONTRADA con el campo en datos
    private Tarea vigente(String uuid, String campo) {
        return tareaRepository.findFirstByUuidAndStatus(uuid, VIGENTE)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.RELACION_NO_ENCONTRADA,
                        "RelacionCrearUseCase | vigente | tarea " + uuid).conDato("campo", campo));
    }
}
