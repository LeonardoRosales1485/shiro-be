package com.blue.open.Code.Empresa;

import org.springframework.stereotype.Service;

/**
 * Valida el CUIT de Empresa por su digito verificador. Lo usan EmpresaService (al guardar una
 * empresa) y UsuarioService (en el registro publico de usuario + empresa).
 * Dependencias externas: ninguna.
 */
@Service
public class ValidadorDeCuitService {

    /**
     * Valida un CUIT argentino basandose en su algoritmo de digito verificador.
     *
     * @param cuit El CUIT a validar (puede contener guiones o solo numeros)
     * @return true si es valido, false de lo contrario
     */
    public boolean esValido(String cuit) {
        if (cuit == null) {
            return false;
        }

        // Eliminar guiones si existen
        String cuitLimpio = cuit.replaceAll("[^0-9]", "");

        if (cuitLimpio.length() != 11) {
            return false;
        }

        int[] pesos = { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };
        int suma = 0;

        for (int i = 0; i < 10; i++) {
            suma += Character.getNumericValue(cuitLimpio.charAt(i)) * pesos[i];
        }

        int resto = suma % 11;
        int resultado = 11 - resto;

        int digitoVerificador;
        if (resultado == 11) {
            digitoVerificador = 0;
        } else if (resultado == 10) {
            // Nota: En la practica, si el resultado es 10, el CUIT no existe con ese prefijo
            // (se cambia de 20 a 23 por ejemplo), pero para validacion tecnica:
            digitoVerificador = 9; 
        } else {
            digitoVerificador = resultado;
        }

        int digitoVerificadorRecibido = Character.getNumericValue(cuitLimpio.charAt(10));

        return digitoVerificador == digitoVerificadorRecibido;
    }
}
