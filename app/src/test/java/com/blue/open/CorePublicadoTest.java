package com.blue.open;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core publicado (CORE-01 del checklist de auditoria): el Core llega al proyecto solo como dependencia Maven publicada
 * desde sm-core-maven, nunca como codigo propio. Falla si aparece un directorio core/ en la raiz del repositorio o un
 * fuente de app/src en el paquete com.blue.open.Core (una clase del Core copiada o reimplementada). No depende de
 * servicios externos ni del contexto de Spring. Ver docs/auditoria/README.md -> "Core publicado".
 */
class CorePublicadoTest {

    // surefire corre con app/ como directorio de trabajo: la raiz del repositorio esta un nivel arriba
    private static final Path RAIZ_DEL_REPOSITORIO = Path.of("..").toAbsolutePath().normalize();

    private static final Path FUENTES = Path.of("src");

    private static final Pattern PAQUETE_DEL_CORE = Pattern.compile("^package com\\.blue\\.open\\.Core(\\.|;)",
            Pattern.MULTILINE);

    @Test
    void core01_noHayDirectorioCore() {
        Path core = RAIZ_DEL_REPOSITORIO.resolve("core");
        assertFalse(Files.exists(core), "CORE-01: existe " + core + ". Desde Core 4.0.0 el Core no se copia al"
                + " proyecto: se declara como dependencia en app/pom.xml (docs/arquitectura/core/README.md ->"
                + " 'Distribucion').");
    }

    @Test
    void core01_ningunFuenteEnElPaqueteDelCore() throws IOException {
        List<String> violaciones;
        try (Stream<Path> archivos = Files.walk(FUENTES)) {
            violaciones = archivos.filter(archivo -> archivo.toString().endsWith(".java"))
                    .filter(CorePublicadoTest::declaraPaqueteDelCore)
                    .map(archivo -> "app/" + archivo.toString().replace('\\', '/'))
                    .sorted()
                    .toList();
        }
        assertTrue(violaciones.isEmpty(), "CORE-01: el paquete com.blue.open.Core es del Core publicado; el proyecto"
                + " no declara clases ahi. Lo que el proyecto necesita distinto se resuelve por la escalera de"
                + " extension (docs/rules/controllers-y-endpoints.md -> 'Alcance de modificacion'):\n  "
                + String.join("\n  ", violaciones));
    }

    // Si el fuente declara un paquete bajo com.blue.open.Core. Recibe la ruta del .java; lanza IllegalStateException
    // si no se puede leer
    private static boolean declaraPaqueteDelCore(Path archivo) {
        try {
            return PAQUETE_DEL_CORE.matcher(Files.readString(archivo, StandardCharsets.UTF_8)).find();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + archivo, e);
        }
    }
}
