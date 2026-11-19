package com.blue.open.Code.Comentario;

import com.blue.open.Code.Common.Canal.CanalDeCambio;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Comentario de una Tarea, escrito por un Usuario (el autor) por un canal (web, MCP o importacion). La fecha es la
 * del comentario: al crearlo es ahora; un comentario copiado al clonar (T1.10) o importado (F4) conserva la original.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_comentario")
public class Comentario extends EntidadConBorradoLogico {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarea")
    private Tarea tarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor")
    private Usuario autor;

    // Cuerpo en Markdown
    @Column(columnDefinition = "TEXT")
    private String cuerpo;

    @Enumerated(EnumType.STRING)
    private CanalDeCambio canal;

    // Fecha del comentario (la original en los copiados o importados)
    private Timestamp fecha;

    // Operacion de archivo que lo archivo; null si esta vigente
    private Long idOperacionArchivo;

    // Cuerpo antes de editarlo, para el historial. No se persiste: lo completa ComentarioService
    @Transient
    private String cuerpoAnterior;
}
