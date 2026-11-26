package com.blue.open;

import com.blue.open.Code.Autorizacion.PermisoDeRecurso;
import com.blue.open.Code.Autorizacion.SoloSesion;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catalogo de permisos (MJ-05 del plan de mejoras). core-test escribe x-permiso solo cuando @Contrato nombra roles o
 * permisos; las operaciones AUTENTICADO y PUBLICO salen sin el. Este generador corre despues de las pruebas (paso
 * antrun de la fase test en app/pom.xml): completa x-permiso en docs/api/openapi.yaml y en el JSON de
 * docs/api/index.html con lo que declaran @PermisoDeRecurso y @SoloSesion ("EDITAR@TABLERO", "AUTENTICADO"), o
 * "AUTENTICADO"/"PUBLICO" para los endpoints del Core y los publicos segun pidan sesion (security vacio es publico), y escribe docs/idp/catalogo-de-permisos.md.
 * No toca las operaciones que ya traen x-permiso y correrlo dos veces no cambia nada.
 * Dependencias externas: ninguna (lee y escribe archivos del repositorio).
 */
public final class CatalogoDePermisos {

    private static final String PAQUETE = "com.blue.open.Code";

    private static final String AUTENTICADO = "AUTENTICADO";

    private static final String PUBLICO = "PUBLICO";

    private static final YAMLMapper YAML = new YAMLMapper();

    private CatalogoDePermisos() {
    }

    // Permiso de una operacion para el contrato y su aclaracion para el catalogo
    public record Permiso(String permiso, String detalle) {
    }

    /*
     * Completa el contrato y escribe el catalogo. Recibe el directorio de docs/api, el de docs/idp y la version.
     * Si no existe openapi.yaml (corrida sin contrato) no hace nada.
     */
    public static void main(String[] args) throws IOException {
        Path api = Path.of(args[0]);
        Path openapi = api.resolve("openapi.yaml");
        if (!Files.exists(openapi))
            return;
        Map<String, Permiso> declarados = declarados();
        String yaml = completarYaml(Files.readString(openapi, StandardCharsets.UTF_8), declarados);
        Files.writeString(openapi, yaml, StandardCharsets.UTF_8);
        Path index = api.resolve("index.html");
        if (Files.exists(index))
            Files.writeString(index, completarHtml(Files.readString(index, StandardCharsets.UTF_8), permisosDelYaml(yaml)), StandardCharsets.UTF_8);
        Path idp = Path.of(args[1]);
        Files.createDirectories(idp);
        Files.writeString(idp.resolve("catalogo-de-permisos.md"), catalogo(yaml, declarados, args[2]), StandardCharsets.UTF_8);
    }

    // Controllers de Shiro (com.blue.open.Code con @RestController), por nombre
    public static List<Class<?>> controllers() {
        ClassPathScanningCandidateComponentProvider escaner = new ClassPathScanningCandidateComponentProvider(false);
        escaner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<Class<?>> controllers = new ArrayList<>();
        for (BeanDefinition definicion : escaner.findCandidateComponents(PAQUETE)) {
            try {
                controllers.add(Class.forName(definicion.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("No se pudo cargar " + definicion.getBeanClassName(), e);
            }
        }
        controllers.sort(Comparator.comparing(Class::getSimpleName));
        return controllers;
    }

    // Permiso que declara un metodo con @PermisoDeRecurso o @SoloSesion; null si no declara ninguno
    public static Permiso permisoDe(Method metodo) {
        PermisoDeRecurso recurso = metodo.getAnnotation(PermisoDeRecurso.class);
        if (recurso != null)
            return new Permiso(recurso.nivel().name() + "@" + recurso.recurso().enElContrato(), recurso.detalle());
        SoloSesion sesion = metodo.getAnnotation(SoloSesion.class);
        return sesion == null ? null : new Permiso(AUTENTICADO, sesion.value());
    }

    // Permisos declarados en los controllers, por id de operacion (Controller.metodo, como core-test)
    public static Map<String, Permiso> declarados() {
        Map<String, Permiso> declarados = new TreeMap<>();
        for (Class<?> controller : controllers()) {
            for (Method metodo : controller.getDeclaredMethods()) {
                Permiso permiso = permisoDe(metodo);
                if (permiso != null)
                    declarados.put(controller.getSimpleName() + "." + metodo.getName(), permiso);
            }
        }
        return declarados;
    }

    // Agrega x-permiso, debajo de x-caso-de-uso, a cada operacion del YAML que no lo tiene
    public static String completarYaml(String yaml, Map<String, Permiso> declarados) {
        List<String> lineas = new ArrayList<>(Arrays.asList(yaml.split("\n", -1)));
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (!linea.trim().startsWith("operationId: "))
                continue;
            int sangria = sangria(linea);
            String operacion = valor(linea);
            int casoDeUso = siguiente(lineas, i + 1, sangria, "x-caso-de-uso:");
            if (casoDeUso < 0 || lineas.get(casoDeUso + 1).trim().startsWith("x-permiso:"))
                continue;
            Permiso declarado = declarados.get(operacion);
            int seguridad = siguiente(lineas, casoDeUso + 1, sangria, "security:");
            boolean pideSesion = seguridad >= 0 && !"security: []".equals(lineas.get(seguridad).trim());
            String permiso = declarado != null ? declarado.permiso() : pideSesion ? AUTENTICADO : PUBLICO;
            lineas.add(casoDeUso + 1, " ".repeat(sangria) + "x-permiso: \"" + permiso + "\"");
        }
        return String.join("\n", lineas);
    }

    // Agrega "x-permiso" al JSON del contrato embebido en index.html, debajo de "x-caso-de-uso" de cada operacion
    public static String completarHtml(String html, Map<String, String> permisos) {
        List<String> lineas = new ArrayList<>(Arrays.asList(html.split("\n", -1)));
        String operacion = null;
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (linea.startsWith("\"operationId\" : "))
                operacion = valor(linea.replace(" : ", ": ").replaceAll(",$", ""));
            if (!linea.startsWith("\"x-caso-de-uso\" : ") || operacion == null || !permisos.containsKey(operacion)
                    || (i + 1 < lineas.size() && lineas.get(i + 1).contains("\"x-permiso\"")))
                continue;
            String sangria = " ".repeat(sangria(lineas.get(i)));
            boolean ultima = !linea.endsWith(",");
            if (ultima)
                lineas.set(i, lineas.get(i) + ",");
            lineas.add(i + 1, sangria + "\"x-permiso\" : \"" + permisos.get(operacion) + "\"" + (ultima ? "" : ","));
        }
        return String.join("\n", lineas);
    }

    // x-permiso de cada operacion del YAML, por id de operacion
    public static Map<String, String> permisosDelYaml(String yaml) {
        Map<String, String> permisos = new LinkedHashMap<>();
        for (JsonNode operacion : operaciones(yaml).values())
            permisos.put(operacion.path("operationId").asString(), operacion.path("x-permiso").asString(""));
        return permisos;
    }

    // Catalogo en Markdown: una fila por operacion con metodo, ruta, permiso, caso de uso, resumen y aclaracion
    public static String catalogo(String yaml, Map<String, Permiso> declarados, String version) {
        StringBuilder texto = new StringBuilder();
        texto.append("# Catálogo de permisos\n\n")
                .append("Generado por la suite completa del backend (`CatalogoDePermisos`, MJ-05 del plan de mejoras) a partir del contrato ")
                .append("`docs/api/openapi.yaml`, versión ").append(version).append(". No se edita a mano.\n\n")
                .append("- `SYSADMIN, ADMIN` u otro rol o permiso: lo exige `@Contrato` antes de entrar al endpoint.\n")
                .append("- `NIVEL@RECURSO` (por ejemplo `EDITAR@TABLERO`): exige sesión y ese nivel sobre el proyecto o el tablero ")
                .append("del elemento, según las reglas de permiso (`ResolutorDePermisosDeRecurso`). El super admin y los Administradores pueden todo.\n")
                .append("- `AUTENTICADO`: alcanza con la sesión. `PUBLICO`: no pide sesión.\n\n")
                .append("| Método | Ruta | Permiso | Caso de uso | Resumen | Aclaración |\n")
                .append("| --- | --- | --- | --- | --- | --- |\n");
        for (Map.Entry<String, JsonNode> entrada : operaciones(yaml).entrySet()) {
            JsonNode operacion = entrada.getValue();
            String[] metodoYRuta = entrada.getKey().split(" ", 2);
            Permiso declarado = declarados.get(operacion.path("operationId").asString());
            texto.append("| ").append(metodoYRuta[0]).append(" | `").append(metodoYRuta[1]).append("` | `")
                    .append(operacion.path("x-permiso").asString("")).append("` | ")
                    .append(operacion.path("x-caso-de-uso").asString("")).append(" | ")
                    .append(celda(operacion.path("summary").asString(""))).append(" | ")
                    .append(celda(declarado == null ? "" : declarado.detalle())).append(" |\n");
        }
        return texto.toString();
    }

    // Operaciones del contrato por "METODO /ruta", ordenadas por ruta y metodo
    private static Map<String, JsonNode> operaciones(String yaml) {
        Map<String, JsonNode> operaciones = new TreeMap<>(Comparator.comparing((String clave) -> clave.split(" ", 2)[1])
                .thenComparing(clave -> clave.split(" ", 2)[0]));
        JsonNode rutas = YAML.readTree(yaml).path("paths");
        for (String ruta : rutas.propertyNames())
            for (String metodo : rutas.path(ruta).propertyNames())
                operaciones.put(metodo.toUpperCase() + " " + ruta, rutas.path(ruta).path(metodo));
        return operaciones;
    }

    // Primera linea desde "desde" con la sangria y el prefijo recibidos, antes de que termine la operacion; -1 si no hay
    private static int siguiente(List<String> lineas, int desde, int sangria, String prefijo) {
        for (int i = desde; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank())
                continue;
            if (sangria(linea) < sangria || (sangria(linea) == sangria && linea.trim().startsWith("operationId:")))
                return -1;
            if (sangria(linea) == sangria && linea.trim().startsWith(prefijo))
                return i;
        }
        return -1;
    }

    private static int sangria(String linea) {
        return linea.length() - linea.stripLeading().length();
    }

    // Valor entre comillas de una linea "clave: \"valor\""
    private static String valor(String linea) {
        String resto = linea.substring(linea.indexOf(':') + 1).trim();
        return resto.startsWith("\"") && resto.endsWith("\"") ? resto.substring(1, resto.length() - 1) : resto;
    }

    // Texto apto para una celda de tabla Markdown
    private static String celda(String texto) {
        return texto.replace("|", "\\|").replace("\n", " ");
    }
}
