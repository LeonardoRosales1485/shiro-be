package com.blue.open.Code.Importacion;

import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import com.blue.open.Core.Kernel.Proyeccion.NoProyectable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Importacion de un proyecto de Jira (Spec 6): las credenciales para conectarse (el token, cifrado), la clave del
 * proyecto de Jira, el mapeo de tableros, estados y tipos, el estado del proceso y su resumen. Se relaciona con el
 * Proyecto de Shiro que creo (o reuso) al ejecutarse; lo importado queda registrado en MapeoJira.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_importacion")
public class Importacion extends EntidadConBorradoLogico {

    // URL base del sitio de Jira, sin barra final (https://empresa.atlassian.net)
    private String urlJira;

    // Correo de la cuenta de Jira con la que se conecta (autenticacion basic con el token)
    private String correo;

    // Token de API de Jira cifrado con CifradorDeCredenciales. Nunca se proyecta ni viaja al front
    @NoProyectable
    private String tokenCifrado;

    // Clave del proyecto de Jira: es el prefijo del proyecto en Shiro
    private String claveProyectoJira;

    // Nombre del proyecto en Jira (el del proyecto que se crea en Shiro)
    private String nombreProyectoJira;

    @Enumerated(EnumType.STRING)
    private EstadoDeImportacion estado = EstadoDeImportacion.CONECTADA;

    // Mapeo elegido, como JSON de MapeoDeImportacion; null hasta mapear
    @Column(columnDefinition = "TEXT")
    private String mapeo;

    // Resumen de la ultima ejecucion, como JSON de ResumenDeImportacion; null hasta ejecutar
    @Column(columnDefinition = "TEXT")
    private String resumen;

    // Proyecto de Shiro con lo importado; null hasta la primera ejecucion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;
}
