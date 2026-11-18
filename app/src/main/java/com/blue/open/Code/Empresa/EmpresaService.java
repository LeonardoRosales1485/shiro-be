package com.blue.open.Code.Empresa;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Multiempresa.Service.EmpresaBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service de empresas del proyecto: usa el CRUD heredado de core-multiempresa y agrega la
 * validacion del CUIT al guardar.
 * Dependencias externas: la base de datos.
 * Recibe: EmpresaDTO o uuid segun el endpoint del ABM. Responde: EmpresaDTO.
 */
@Service
@RequiredArgsConstructor
public class EmpresaService extends EmpresaBaseService<Empresa, EmpresaDTO> {

    private final ValidadorDeCuitService validadorDeCuitService;

    // Rechaza con 400 un CUIT cuyo digito verificador no es valido (el CUIT es opcional)
    @Override
    protected Empresa hookGuardarAplicarDto(EmpresaDTO dto, Empresa modelo) {
        if (dto.getCuit() != null && !dto.getCuit().isBlank() && !validadorDeCuitService.esValido(dto.getCuit())) {
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CUIT_INVALIDO,
                    "EmpresaService | hookGuardarAplicarDto | digito verificador invalido: " + dto.getCuit());
        }
        return super.hookGuardarAplicarDto(dto, modelo);
    }
}
