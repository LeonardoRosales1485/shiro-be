package com.blue.open;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reglas de estilo verificables sobre los fuentes de app/ (lo que ArquitecturaTest no puede ver en el bytecode:
 * anotaciones de Lombok, que son source-only; strings; referencias inline; System.out). Cada test cita el id del
 * checklist de auditoria que verifica (docs/auditoria/checklist.md). Las busquedas se hacen sobre el fuente sin
 * literales de string ni comentarios salvo que el test diga lo contrario, para no marcar un JPQL con nombre completo
 * de clase ni un mensaje para el usuario con tildes. Solo audita app/: el Core es una dependencia publicada.
 */
class EstiloDeCodigoTest {

    // surefire corre con app/ como directorio de trabajo
    private static final Path MAIN = Path.of("src", "main", "java");

    private static final Path TEST = Path.of("src", "test", "java");

    private static final Path CODE_MAIN = MAIN.resolve(Path.of("com", "blue", "open", "Code"));

    private static final Path CODE_TEST = TEST.resolve(Path.of("com", "blue", "open", "Code"));

    private static final Path POM = Path.of("pom.xml");

    private static final Pattern ANOTACION_DE_BEAN = Pattern.compile(
            "@(Service|Component|RestController|Controller|Configuration|Repository)\\b");

    private static List<Fuente> fuentesDeMain;

    private static List<Fuente> fuentesDeTest;

    // Un archivo .java con su texto original y el texto sin literales de string ni comentarios (misma cantidad de lineas)
    private record Fuente(Path ruta, String original, String limpio) {
    }

    @BeforeAll
    static void leerFuentes() throws IOException {
        fuentesDeMain = leer(MAIN);
        fuentesDeTest = leer(TEST);
    }

    @Test
    void idf01_identificadoresFijos() throws IOException {
        String pom = Files.readString(POM, StandardCharsets.UTF_8);
        List<String> violaciones = new ArrayList<>();
        if (!pom.contains("<groupId>com.blue.open</groupId>")) {
            violaciones.add("app/pom.xml: groupId distinto de com.blue.open");
        }
        if (!pom.contains("<artifactId>open-data-proyect</artifactId>")) {
            violaciones.add("app/pom.xml: artifactId distinto de open-data-proyect");
        }
        Pattern paquete = Pattern.compile("^package com\\.blue\\.open(\\.|;)", Pattern.MULTILINE);
        Stream.concat(fuentesDeMain.stream(), fuentesDeTest.stream())
                .filter(fuente -> !paquete.matcher(fuente.limpio()).find())
                .forEach(fuente -> violaciones.add(fuente.ruta() + ": paquete fuera de com.blue.open"));
        assertTrue(violaciones.isEmpty(), "IDF-01: groupId, artifactId y paquete raiz no se modifican nunca"
                + " (docs/rules/identificadores-fijos.md):\n  " + String.join("\n  ", violaciones));
    }

    @Test
    void idi02_sinTildesNiEnesEnElCodigo() {
        List<String> violaciones = new ArrayList<>();
        Pattern noAscii = Pattern.compile("[^\\x00-\\x7F]");
        porLinea(Stream.concat(fuentesDeMain.stream(), fuentesDeTest.stream()).toList(), true,
                (fuente, numero) -> violaciones.add(fuente.ruta() + ":" + numero), noAscii);
        assertTrue(violaciones.isEmpty(), "IDI-02: sin tildes, enes ni caracteres especiales en identificadores ni"
                + " comentarios; solo en textos visibles por el usuario final (docs/rules/idioma-y-estilo.md)."
                + " Lineas con caracteres no ASCII fuera de un string:\n  " + String.join("\n  ", violaciones));
    }

    @Test
    void idi04_lombokProhibido() {
        List<String> violaciones = new ArrayList<>();
        porLinea(fuentesDeMain, true, (fuente, numero) -> violaciones.add(fuente.ruta() + ":" + numero + " @Data"),
                Pattern.compile("@Data\\b"));
        porLinea(fuentesDeMain, true, (fuente, numero) -> violaciones.add(fuente.ruta() + ":" + numero + " lombok.Value"),
                Pattern.compile("\\blombok\\.Value\\b"));
        porLinea(fuentesDeMain, true, (fuente, numero) -> violaciones.add(fuente.ruta() + ":" + numero + " @SneakyThrows"),
                Pattern.compile("@SneakyThrows\\b"));
        Pattern toStringOEquals = Pattern.compile("@(ToString|EqualsAndHashCode)\\b");
        Pattern allArgs = Pattern.compile("@AllArgsConstructor\\b");
        for (Fuente fuente : fuentesDeMain) {
            if (fuente.limpio().contains("@Entity")) {
                porLinea(List.of(fuente), true, (f, numero) -> violaciones.add(
                        f.ruta() + ":" + numero + " @ToString/@EqualsAndHashCode en una @Entity"), toStringOEquals);
            }
            if (ANOTACION_DE_BEAN.matcher(fuente.limpio()).find()) {
                porLinea(List.of(fuente), true, (f, numero) -> violaciones.add(
                        f.ruta() + ":" + numero + " @AllArgsConstructor en un bean de Spring"), allArgs);
            }
        }
        assertTrue(violaciones.isEmpty(), "IDI-04: Lombok prohibido: @Data, lombok.Value y @SneakyThrows en cualquier"
                + " lado; @ToString y @EqualsAndHashCode en una @Entity; @AllArgsConstructor en un bean"
                + " (docs/rules/idioma-y-estilo.md -> 'Lombok'):\n  " + String.join("\n  ", violaciones));
    }

    @Test
    void ctr08_sinReferenciasInlineConNombreCompleto() {
        List<String> violaciones = new ArrayList<>();
        Pattern nombreCompleto = Pattern.compile("\\bcom\\.blue\\.open\\.[A-Z]");
        Pattern importOPackage = Pattern.compile("^\\s*(import|package)\\b");
        for (Fuente fuente : fuentesDeMain) {
            String[] lineas = fuente.limpio().split("\n", -1);
            for (int i = 0; i < lineas.length; i++) {
                if (!importOPackage.matcher(lineas[i]).find() && nombreCompleto.matcher(lineas[i]).find()) {
                    violaciones.add(fuente.ruta() + ":" + (i + 1));
                }
            }
        }
        assertTrue(violaciones.isEmpty(), "CTR-08: los imports van arriba; no se referencia una clase inline por su"
                + " nombre completo (docs/rules/controllers-y-endpoints.md):\n  " + String.join("\n  ", violaciones));
    }

    @Test
    void di04_sinAutowiredRequiredFalse() {
        List<String> violaciones = new ArrayList<>();
        porLinea(fuentesDeMain, true, (fuente, numero) -> violaciones.add(fuente.ruta() + ":" + numero),
                Pattern.compile("@Autowired\\s*\\(\\s*required\\s*=\\s*false"));
        assertTrue(violaciones.isEmpty(), "DI-04: una dependencia opcional se declara como ObjectProvider<T>, no con"
                + " @Autowired(required = false) (docs/rules/inyeccion-de-dependencias.md):\n  "
                + String.join("\n  ", violaciones));
    }

    @Test
    void di06_sinInjectMocksEnLosTests() {
        List<String> violaciones = new ArrayList<>();
        porLinea(fuentesDeTest, true, (fuente, numero) -> violaciones.add(fuente.ruta() + ":" + numero),
                Pattern.compile("@InjectMocks\\b"));
        assertTrue(violaciones.isEmpty(), "DI-06 / TST-02: la clase bajo prueba se construye con new pasando los mocks"
                + " por constructor (o por setter si es una clase base del Core); sin @InjectMocks"
                + " (docs/rules/testing.md):\n  " + String.join("\n  ", violaciones));
    }

    @Test
    void tst04_losDatosDeUnaPruebaDeContratoNoSeCreanConSql() {
        List<String> violaciones = new ArrayList<>();
        List<Fuente> pruebasDeContrato = fuentesDeTest.stream()
                .filter(fuente -> fuente.ruta().startsWith(CODE_TEST)).toList();
        porLinea(pruebasDeContrato, true, (fuente, numero) -> violaciones.add(fuente.ruta() + ":" + numero),
                Pattern.compile("\\b(JdbcTemplate|JdbcClient|EntityManager)\\b|@Sql\\b"));
        assertTrue(violaciones.isEmpty(), "TST-04: los datos que una prueba de contrato necesita se crean por los"
                + " endpoints reales (EscenarioDe{Entidad}), nunca con SQL a mano (docs/rules/testing.md):\n  "
                + String.join("\n  ", violaciones));
    }

    @Test
    void log01_soloSlf4j() {
        List<String> violaciones = new ArrayList<>();
        porLinea(fuentesDeMain, true, (fuente, numero) -> violaciones.add(fuente.ruta() + ":" + numero),
                Pattern.compile("System\\.(out|err)\\.|\\.printStackTrace\\(|LoggerFactory\\.getLogger"));
        assertTrue(violaciones.isEmpty(), "LOG-01: se loguea con @Slf4j; prohibido System.out/err, printStackTrace y"
                + " LoggerFactory.getLogger (docs/rules/logs.md -> 'Logger'):\n  " + String.join("\n  ", violaciones));
    }

    @Test
    void log02_sinConcatenacionEnLosMensajesDeLog() {
        List<String> violaciones = new ArrayList<>();
        // Sobre el fuente original: hay que ver el literal seguido de +
        porLinea(fuentesDeMain, false, (fuente, numero) -> violaciones.add(fuente.ruta() + ":" + numero),
                Pattern.compile("\\blog\\.(trace|debug|info|warn|error)\\(\\s*\"(?:[^\"\\\\]|\\\\.)*\"\\s*\\+"));
        assertTrue(violaciones.isEmpty(), "LOG-02: los valores de un log se pasan como argumentos con {}, nunca"
                + " concatenados al mensaje (docs/rules/logs.md -> 'Formato del mensaje'):\n  "
                + String.join("\n  ", violaciones));
    }

    @Test
    void err01_losErroresDeLaApiSonExcepcionDeApi() {
        List<String> violaciones = new ArrayList<>();
        List<Fuente> code = fuentesDeMain.stream().filter(fuente -> fuente.ruta().startsWith(CODE_MAIN)).toList();
        porLinea(code, true, (fuente, numero) -> violaciones.add(fuente.ruta() + ":" + numero),
                Pattern.compile("throw new RuntimeException\\(|\\bResponseStatusException\\b"
                        + "|ResponseEntity\\.(badRequest|notFound|internalServerError)\\("
                        + "|ResponseEntity\\.status\\(\\s*HttpStatus\\.(BAD_REQUEST|NOT_FOUND|FORBIDDEN|UNAUTHORIZED|CONFLICT|INTERNAL_SERVER_ERROR)"
                        + "|new ResponseEntity<[^;]*HttpStatus\\.(BAD_REQUEST|NOT_FOUND|FORBIDDEN|UNAUTHORIZED|CONFLICT|INTERNAL_SERVER_ERROR)"));
        assertTrue(violaciones.isEmpty(), "ERR-01: todo error de la API se lanza como ExcepcionDeApi con codigo; no se"
                + " arma una ResponseEntity de error a mano ni se lanza otra excepcion (docs/rules/errores.md):\n  "
                + String.join("\n  ", violaciones));
    }

    @Test
    void arc02_losArchivosNoVanAlFilesystemNiALaBase() {
        List<String> violaciones = new ArrayList<>();
        List<Fuente> code = fuentesDeMain.stream().filter(fuente -> fuente.ruta().startsWith(CODE_MAIN)).toList();
        porLinea(code, true, (fuente, numero) -> violaciones.add(fuente.ruta() + ":" + numero),
                Pattern.compile("\\bnew File\\(|\\bFiles\\.(write|copy|newOutputStream)\\(|\\bFileOutputStream\\b|\\bFileWriter\\b|@Lob\\b"));
        Pattern byteArray = Pattern.compile("\\bbyte\\s*\\[\\s*\\]\\s+\\w+\\s*[;=]");
        for (Fuente fuente : code) {
            if (fuente.limpio().contains("@Entity")) {
                porLinea(List.of(fuente), true, (f, numero) -> violaciones.add(f.ruta() + ":" + numero + " byte[] en una @Entity"),
                        byteArray);
            }
        }
        assertTrue(violaciones.isEmpty(), "ARC-02: un archivo nunca se guarda en el filesystem del contenedor ni en la"
                + " base; va a SM3 (publico) o Crypter (privado) y la entidad persiste la referencia"
                + " (docs/rules/archivos.md -> 'Donde se guarda'):\n  " + String.join("\n  ", violaciones));
    }

    @Test
    void con04_sqlNativoSoloConComentarioQueLoJustifique() {
        List<String> violaciones = new ArrayList<>();
        Pattern nativo = Pattern.compile("nativeQuery\\s*=\\s*true");
        Pattern comentario = Pattern.compile("^\\s*(//|\\*|/\\*)");
        for (Fuente fuente : fuentesDeMain) {
            String[] lineas = fuente.original().split("\n", -1);
            for (int i = 0; i < lineas.length; i++) {
                if (!nativo.matcher(lineas[i]).find()) {
                    continue;
                }
                boolean justificado = false;
                for (int j = Math.max(0, i - 4); j < i; j++) {
                    if (comentario.matcher(lineas[j]).find()) {
                        justificado = true;
                    }
                }
                if (!justificado) {
                    violaciones.add(fuente.ruta() + ":" + (i + 1));
                }
            }
        }
        assertTrue(violaciones.isEmpty(), "CON-04: SQL nativo solo cuando JPQL no lo resuelve, con un comentario encima"
                + " que explique por que (docs/rules/consultas.md -> 'Que forma de consulta usar'):\n  "
                + String.join("\n  ", violaciones));
    }

    // Recorre las lineas de cada fuente (limpio o original) y reporta las que matchean el patron
    private static void porLinea(List<Fuente> fuentes, boolean sobreLimpio, BiConsumer<Fuente, Integer> reportar,
                                 Pattern patron) {
        for (Fuente fuente : fuentes) {
            String[] lineas = (sobreLimpio ? fuente.limpio() : fuente.original()).split("\n", -1);
            for (int i = 0; i < lineas.length; i++) {
                Matcher matcher = patron.matcher(lineas[i]);
                if (matcher.find()) {
                    reportar.accept(fuente, i + 1);
                }
            }
        }
    }

    private static List<Fuente> leer(Path raiz) throws IOException {
        if (!Files.isDirectory(raiz)) {
            return List.of();
        }
        List<Fuente> resultado = new ArrayList<>();
        try (Stream<Path> archivos = Files.walk(raiz)) {
            for (Path archivo : archivos.filter(p -> p.toString().endsWith(".java")).sorted().toList()) {
                String original = Files.readString(archivo, StandardCharsets.UTF_8);
                resultado.add(new Fuente(archivo, original, sinLiteralesNiComentarios(original)));
            }
        }
        return resultado;
    }

    /**
     * Reemplaza por espacios el contenido de los literales de string (incluidos los text blocks), de char y de los
     * comentarios, conservando los saltos de linea para que los numeros de linea no cambien. Las comillas que abren y
     * cierran un string se conservan, asi un patron puede seguir viendo que ahi habia un literal.
     */
    static String sinLiteralesNiComentarios(String fuente) {
        StringBuilder salida = new StringBuilder(fuente.length());
        int i = 0;
        int largo = fuente.length();
        while (i < largo) {
            char c = fuente.charAt(i);
            if (c == '/' && i + 1 < largo && fuente.charAt(i + 1) == '/') {
                while (i < largo && fuente.charAt(i) != '\n') {
                    salida.append(' ');
                    i++;
                }
            } else if (c == '/' && i + 1 < largo && fuente.charAt(i + 1) == '*') {
                int fin = fuente.indexOf("*/", i + 2);
                fin = fin < 0 ? largo : fin + 2;
                for (; i < fin; i++) {
                    salida.append(fuente.charAt(i) == '\n' ? '\n' : ' ');
                }
            } else if (c == '"' && fuente.startsWith("\"\"\"", i)) {
                int fin = fuente.indexOf("\"\"\"", i + 3);
                fin = fin < 0 ? largo : fin + 3;
                salida.append("\"\"\"");
                for (i += 3; i < fin; i++) {
                    char d = fuente.charAt(i);
                    salida.append(d == '\n' ? '\n' : d == '"' && i >= fin - 3 ? '"' : ' ');
                }
            } else if (c == '"' || c == '\'') {
                char cierre = c;
                salida.append(c);
                i++;
                while (i < largo && fuente.charAt(i) != cierre && fuente.charAt(i) != '\n') {
                    if (fuente.charAt(i) == '\\' && i + 1 < largo) {
                        salida.append("  ");
                        i += 2;
                    } else {
                        salida.append(' ');
                        i++;
                    }
                }
                if (i < largo) {
                    salida.append(fuente.charAt(i));
                    i++;
                }
            } else {
                salida.append(c);
                i++;
            }
        }
        return salida.toString();
    }
}
