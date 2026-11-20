package com.blue.open.Code.OperacionDeArchivo;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista previa de un archivado (CU-ARC-02, propuesta 49 resuelta el 19/11): que es, donde estaba y que pasa al
 * restaurarlo. Lo arma OperacionDeArchivoVistaPreviaUseCase.
 */
@Setter
@Getter
@NoArgsConstructor
public class ArchivoVistaPreviaResponseDTO extends EntidadBaseDTO {

    @Descripcion("Tipo: TAREA, PROYECTO, SPRINT, VERSION o USUARIO")
    private String tipo;

    @Descripcion("Nombre visible: clave y título de la tarea, prefijo y nombre del proyecto, nombre del sprint o versión, nombre de usuario")
    private String nombre;

    @Descripcion("Dónde vuelve: en una tarea, el tablero y el estado que tenía («Principal · Por hacer»). Vacío en los demás tipos")
    private String ubicacion;

    @Descripcion("Descripción de la tarea en Markdown. Vacía en los demás tipos")
    private String descripcion;

    @Descripcion("En una tarea: si su padre sigue archivado (restaurarla responde PADRE_ARCHIVADO hasta restaurar el padre)")
    private boolean padreArchivado;

    @Descripcion("Cuántas tareas se restauran con el elemento (las archivadas en la misma operación)")
    private int cantidadQueSeRestaura;

    @Descripcion("Tareas que se restauran con el elemento, por número (hasta 50)")
    private List<ElementoDeArchivoDTO> seRestauran = new ArrayList<>();

    @Descripcion("Tareas archivadas por separado que quedan archivadas: hijas de la tarea o tareas del proyecto (hasta 50)")
    private List<ElementoDeArchivoDTO> quedanArchivadas = new ArrayList<>();

    // Vista de un sprint, version, proyecto o usuario: solo el nombre (las listas las completa el caso de uso)
    public ArchivoVistaPreviaResponseDTO(String tipo, String uuid, String nombre) {
        setUuid(uuid);
        this.tipo = tipo;
        this.nombre = nombre;
    }

    // Vista de una tarea: nombre, donde vuelve, descripcion y si el padre sigue archivado
    public ArchivoVistaPreviaResponseDTO(String tipo, String uuid, String nombre, String ubicacion, String descripcion, boolean padreArchivado) {
        this(tipo, uuid, nombre);
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.padreArchivado = padreArchivado;
    }
}
