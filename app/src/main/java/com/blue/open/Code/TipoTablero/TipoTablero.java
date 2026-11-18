package com.blue.open.Code.TipoTablero;

import com.blue.open.Code.Estado.Estado;
import com.blue.open.Core.Kernel.Entidad.EntidadConBorradoLogico;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Tipo de tablero de un proyecto: Principal, QA, Analisis o Diseno (seccion 4 del DAF). Es un catalogo sembrado por
 * Liquibase, no un enum, para poder sumar sub-tableros despues de la v1 sin migrar el esquema (spec, decisiones
 * transversales). Se relaciona con sus Estado (uno a muchos); cada proyecto tiene un tablero de cada tipo.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "shr_tipo_tablero")
public class TipoTablero extends EntidadConBorradoLogico {

    // Codigo estable del tipo: PRINCIPAL, QA, ANALISIS o DISENO
    private String codigo;

    // Nombre visible del tablero
    private String nombre;

    // Posicion del tablero en las pestanas del proyecto (1 = principal)
    private Integer orden;

    // Estados del tablero, en orden de columna
    @OneToMany(mappedBy = "tipoTablero", fetch = FetchType.LAZY)
    private List<Estado> estados = new ArrayList<>();
}
