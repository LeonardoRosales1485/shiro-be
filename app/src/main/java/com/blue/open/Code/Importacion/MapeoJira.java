package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Entidad.EntidadBase;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Que elemento de Jira quedo en que registro de Shiro (Spec 6): permite reimportar actualizando en vez de duplicar.
 * Es unico por tipo e id de Jira (supone un solo sitio de Jira, adenda propuesta 31). Apunta por uuid a un Proyecto,
 * Tarea, Comentario, Sprint o Version; no tiene borrado (nada se borra).
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_mapeo_jira")
public class MapeoJira extends EntidadBase {

    @Enumerated(EnumType.STRING)
    private TipoDeEntidadJira tipoEntidad;

    // Id del elemento en Jira (la clave, para el proyecto)
    private String idJira;

    // Uuid del registro de Shiro
    private String uuidShiro;

    // Registro nuevo del mapeo, con el usuario que importa
    public MapeoJira(TipoDeEntidadJira tipoEntidad, String idJira, String uuidShiro, Long idUsuario) {
        this.tipoEntidad = tipoEntidad;
        this.idJira = idJira;
        this.uuidShiro = uuidShiro;
        setIdUsuarioDeCreacion(idUsuario);
    }
}
