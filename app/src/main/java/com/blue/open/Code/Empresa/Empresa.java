package com.blue.open.Code.Empresa;

import com.blue.open.Core.Multiempresa.Entidad.EmpresaBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Empresa del proyecto, extiende {@link EmpresaBase} (core-multiempresa) agregando el CUIT.
 * La relacion con usuarios y roles vive en auth_usuarios_empresas (core-multiempresa).
 */
@Setter
@Getter
@Table(name = "empresas")
@Entity()
public class Empresa extends EmpresaBase {
    // CUIT argentino de la empresa, validado por ValidadorDeCuitService cuando se envia
    private String cuit;
}
