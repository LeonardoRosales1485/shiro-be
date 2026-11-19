package com.blue.open.Code.OperacionDeArchivo;

import com.blue.open.Core.Kernel.Entidad.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

/**
 * Operacion de archivo (Spec 1 y T1.8): agrupa todo lo que se archivo o restauro en cascada en una misma accion. Las
 * tablas shr_ guardan en id_operacion_archivo la operacion que las archivo; restaurar devuelve solo lo de esa
 * operacion. Quien y cuando: idUsuarioDeCreacion y fechaDeCreacion. Solo se inserta, por eso hereda de EntidadBase.
 * No tiene relaciones JPA: la entidad raiz se identifica por nombre y uuid, como en Historial.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_operacion_archivo")
public class OperacionDeArchivo extends EntidadBase {

    @Enumerated(EnumType.STRING)
    private TipoDeOperacionDeArchivo tipo;

    // Entidad raiz de la operacion: TAREA, PROYECTO, ...
    private String entidad;

    // Uuid del registro raiz
    @Column(length = 36)
    @JdbcTypeCode(Types.CHAR)
    private String uuidEntidad;

    // Crea una operacion del tipo recibido sobre el registro raiz
    public OperacionDeArchivo(TipoDeOperacionDeArchivo tipo, String entidad, String uuidEntidad, Long idUsuario) {
        this.tipo = tipo;
        this.entidad = entidad;
        this.uuidEntidad = uuidEntidad;
        setIdUsuarioDeCreacion(idUsuario);
    }
}
