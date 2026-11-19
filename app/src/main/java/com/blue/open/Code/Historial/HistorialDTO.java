package com.blue.open.Code.Historial;

import com.blue.open.Code.Common.Canal.CanalDeCambio;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Fila del historial tal como la ve el front: accion, campo, valores, autor y canal. fechaDeCreacion es la fecha del
 * cambio. Lo arma HistorialRepository con una proyeccion.
 */
@Setter
@Getter
@NoArgsConstructor
public class HistorialDTO extends EntidadBaseDTO {

    @Descripcion("Tipo de acción: CREAR, EDITAR, MOVER, CREAR_SUBTAREA, ARCHIVAR, RESTAURAR, COMENTAR, RELACIONAR o CLONAR")
    private AccionDeHistorial accion;

    @Descripcion("Campo que cambió (titulo, prioridad, estado, ...). Vacío en las acciones que no son de un campo")
    private String campo;

    @Descripcion("Valor anterior, como texto. Vacío si no tenía")
    private String valorAnterior;

    @Descripcion("Valor nuevo, como texto. En CREAR es la clave; en CREAR_SUBTAREA, la clave de la subtarea")
    private String valorNuevo;

    @Descripcion("Canal del cambio: WEB, MCP o IMPORTACION. El front muestra los de MCP como «Usuario (vía MCP)»")
    private CanalDeCambio canal;

    @Descripcion("Uuid del usuario que hizo el cambio")
    private String usuarioUuid;

    @Descripcion("Nombre del usuario que hizo el cambio")
    private String usuarioNombre;

    // Constructor de la proyeccion (HistorialRepository)
    public HistorialDTO(String uuid, Timestamp fecha, AccionDeHistorial accion, String campo, String valorAnterior,
            String valorNuevo, CanalDeCambio canal, String usuarioUuid, String usuarioNombre) {
        setUuid(uuid);
        setFechaDeCreacion(fecha);
        this.accion = accion;
        this.campo = campo;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.canal = canal;
        this.usuarioUuid = usuarioUuid;
        this.usuarioNombre = usuarioNombre;
    }
}
