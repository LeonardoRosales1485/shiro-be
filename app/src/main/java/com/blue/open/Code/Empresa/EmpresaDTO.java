package com.blue.open.Code.Empresa;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Multiempresa.DTO.EmpresaBaseDTO;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de empresa del proyecto: agrega el CUIT a EmpresaBaseDTO (core-multiempresa).
 * Se usa en el ABM de /empresa y en el registro publico de usuario + empresa.
 */
@Getter
@Setter
public class EmpresaDTO extends EmpresaBaseDTO {
    @Descripcion("CUIT de la empresa (opcional). Al guardar se valida el dígito verificador")
    @Pattern(regexp = "^\\d{2}-?\\d{8}-?\\d$", message = "El CUIT debe tener 11 dígitos, con o sin guiones")
    private String cuit;
}
