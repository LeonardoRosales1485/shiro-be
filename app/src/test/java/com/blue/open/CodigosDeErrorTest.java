package com.blue.open;

import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica por reflexion todos los enums CodigoDeError del classpath (los de cada modulo del Core y el del proyecto):
 * toda constante lleva @Descripcion, que es la columna "Cuando ocurre" del catalogo de errores del contrato,
 * y el valor de codigo() no se repite entre ningun enum. Ver docs/rules/errores.md (reglas 7 y 12).
 */
class CodigosDeErrorTest {

    @Test
    void todaConstanteTieneDescripcion() throws Exception {
        List<Class<?>> enums = enumsDeCodigoDeError();
        List<String> sinDescripcion = new ArrayList<>();
        for (Class<?> clase : enums) {
            for (Object constante : clase.getEnumConstants()) {
                String nombre = ((Enum<?>) constante).name();
                Descripcion descripcion = clase.getField(nombre).getAnnotation(Descripcion.class);
                if (descripcion == null || descripcion.value().isBlank()) {
                    sinDescripcion.add(clase.getName() + "." + nombre);
                }
            }
        }

        assertFalse(enums.isEmpty(), "No se encontro ningun enum CodigoDeError en el classpath");
        assertTrue(sinDescripcion.isEmpty(),
                "Constantes de CodigoDeError sin @Descripcion (cuando ocurre el error):\n- "
                        + String.join("\n- ", sinDescripcion));
    }

    // Regla 12 de errores.md: el front discrimina por codigo(), asi que un mismo valor en dos constantes
    // (del mismo enum o de enums distintos) haria indistinguibles dos situaciones.
    @Test
    void ningunCodigoSeRepite() throws Exception {
        List<Class<?>> enums = enumsDeCodigoDeError();
        Map<String, List<String>> constantesPorCodigo = new LinkedHashMap<>();
        for (Class<?> clase : enums) {
            for (Object constante : clase.getEnumConstants()) {
                String codigo = ((CodigoDeError) constante).codigo();
                constantesPorCodigo.computeIfAbsent(codigo, c -> new ArrayList<>())
                        .add(clase.getName() + "." + ((Enum<?>) constante).name());
            }
        }
        List<String> duplicados = new ArrayList<>();
        constantesPorCodigo.forEach((codigo, constantes) -> {
            if (constantes.size() > 1) {
                duplicados.add(codigo + ": " + String.join(", ", constantes));
            }
        });

        assertFalse(enums.isEmpty(), "No se encontro ningun enum CodigoDeError en el classpath");
        assertTrue(duplicados.isEmpty(),
                "Codigos de error repetidos entre enums CodigoDeError:\n- " + String.join("\n- ", duplicados));
    }

    // Enums que implementan CodigoDeError en com.blue.open, igual que el catalogo de errores de core-test.
    // Recibe: nada. Responde: las clases de esos enums
    private static List<Class<?>> enumsDeCodigoDeError() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(CodigoDeError.class));
        List<Class<?>> enums = new ArrayList<>();
        for (BeanDefinition definicion : scanner.findCandidateComponents("com.blue.open")) {
            Class<?> clase = Class.forName(definicion.getBeanClassName(), false,
                    Thread.currentThread().getContextClassLoader());
            if (clase.isEnum()) {
                enums.add(clase);
            }
        }
        return enums;
    }
}
