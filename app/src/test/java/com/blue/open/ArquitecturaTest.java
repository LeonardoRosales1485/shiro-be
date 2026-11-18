package com.blue.open;

import com.blue.open.Core.Crud.AbstractService;
import com.blue.open.Core.Kernel.DTO.EntidadBaseDTO;
import com.blue.open.Core.Kernel.Entidad.EntidadBase;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeError;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reglas de arquitectura verificables sobre el bytecode (ArchUnit), sin contexto de Spring. Cada test cita el id del
 * checklist de auditoria que verifica (docs/auditoria/checklist.md): dependencias entre Core y Code, inyeccion
 * por constructor, estructura de paquetes por entidad, herencia de entidades y DTOs, relaciones LAZY, forma de los
 * UseCase y de los repositorios. Lo que no se puede ver en el bytecode (Lombok, que es source-only; strings; textos)
 * lo cubre EstiloDeCodigoTest sobre los fuentes.
 */
class ArquitecturaTest {

    private static final String RAIZ = "com.blue.open";

    private static final String CODE = RAIZ + ".Code";

    private static final String CORE = RAIZ + ".Core";

    // Carpetas de Code que no son una entidad: no se les aplica la estructura "plana + CasosDeUso/Notificaciones"
    private static final Set<String> CARPETAS_FIJAS = Set.of("Autorizacion", "Common");

    private static final Set<String> SUBCARPETAS_DE_ENTIDAD = Set.of("CasosDeUso", "Notificaciones");

    // Nombres de carpeta "por tipo" que la regla prohibe a cualquier nivel de Code (comparados en minusculas)
    private static final Set<String> CARPETAS_POR_TIPO = Set.of(
            "controller", "controllers", "controladores", "service", "services", "servicios", "repository",
            "repositories", "repositorios", "entity", "entities", "entidad", "entidades", "model", "models", "modelo",
            "modelos", "dto", "dtos", "usecase", "usecases");

    private static final Set<String> RELACIONES_EAGER_POR_DEFECTO = Set.of(
            "jakarta.persistence.ManyToOne", "jakarta.persistence.OneToOne");

    private static final Set<String> RELACIONES = Set.of(
            "jakarta.persistence.ManyToOne", "jakarta.persistence.OneToOne",
            "jakarta.persistence.OneToMany", "jakarta.persistence.ManyToMany");

    private static final Set<String> TIPOS_DE_FECHA = Set.of(
            "java.sql.Timestamp", "java.util.Date", "java.time.LocalDateTime");

    private static final String JSON_FORMAT = "com.fasterxml.jackson.annotation.JsonFormat";

    private static final String PATRON_DE_FECHA = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private static final String ZONA_HORARIA = "America/Argentina/Buenos_Aires";

    private static JavaClasses clasesDeMain;

    private static JavaClasses clasesDeTest;

    @BeforeAll
    static void importarClases() {
        clasesDeMain = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(RAIZ);
        clasesDeTest = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.ONLY_INCLUDE_TESTS)
                .importPackages(RAIZ);
    }

    @Test
    void core02_elCoreNoDependeDeCode() {
        noClasses().that().resideInAPackage(CORE + "..")
                .should().dependOnClassesThat().resideInAPackage(CODE + "..")
                .because("CORE-02: el Core nunca importa com.blue.open.Code; lo que necesita del proyecto lo pide por"
                        + " un puerto (docs/arquitectura/core/README.md -> 'Las dos reglas')")
                .check(clasesDeMain);
    }

    @Test
    void di01_sinAutowiredSobreCampos() {
        // core-test (com.blue.open.Core.Test) son clases base de @SpringBootTest: ahi @Autowired sobre un campo es la
        // forma estandar de obtener un bean del contexto y la regla lo exceptua ("Que no cambia")
        noFields().that().areDeclaredInClassesThat().resideInAPackage(RAIZ + "..")
                .and().areDeclaredInClassesThat().resideOutsideOfPackage(CORE + ".Test..")
                .should().beAnnotatedWith(Autowired.class)
                .because("DI-01: esta prohibido @Autowired sobre un campo; las dependencias entran por constructor"
                        + " (docs/rules/inyeccion-de-dependencias.md)")
                .check(clasesDeMain);
    }

    @Test
    void di03_sinInyeccionPorSetterEnCode() {
        noMethods().that().areDeclaredInClassesThat().resideInAPackage(CODE + "..")
                .should().beAnnotatedWith(Autowired.class)
                .because("DI-03: la inyeccion por setter es solo para clases base del Core; en Code va por"
                        + " constructor (docs/rules/inyeccion-de-dependencias.md -> 'Excepcion')")
                .check(clasesDeMain);
    }

    @Test
    void est01_codeNoSeOrganizaPorTipo() {
        List<String> violaciones = new ArrayList<>();
        for (JavaClass clase : clasesDeCode(clasesDeMain)) {
            for (String segmento : segmentosBajoCode(clase)) {
                if (CARPETAS_POR_TIPO.contains(segmento.toLowerCase(Locale.ROOT))) {
                    violaciones.add(clase.getName() + " (carpeta '" + segmento + "')");
                    break;
                }
            }
        }
        assertTrue(violaciones.isEmpty(), "EST-01: Code se organiza por entidad, nunca por tipo de archivo"
                + " (docs/rules/estructura-de-carpetas.md -> 'Principio general'). Clases en una carpeta por tipo:\n  "
                + String.join("\n  ", violaciones));
    }

    @Test
    void est02_dentroDeUnaEntidadSoloCasosDeUsoYNotificaciones() {
        Set<String> paquetesConClases = clasesDeCode(clasesDeMain).stream()
                .map(JavaClass::getPackageName).collect(Collectors.toSet());
        List<String> violaciones = new ArrayList<>();
        for (JavaClass clase : clasesDeCode(clasesDeMain)) {
            List<String> segmentos = segmentosBajoCode(clase);
            if (segmentos.size() <= 1) {
                continue;
            }
            String primera = segmentos.get(0);
            if (CARPETAS_FIJAS.contains(primera)) {
                // Autorizacion es plana; Common/{Concern} admite un nivel
                if (primera.equals("Autorizacion") || segmentos.size() > 2) {
                    violaciones.add(clase.getName());
                }
                continue;
            }
            // Code/{Entidad}/{Sub}: Sub es CasosDeUso o Notificaciones. Si Code/{Primera} no tiene clases, es una
            // carpeta de dominio (ver "Como crecer") y la entidad esta un nivel mas abajo
            boolean primeraEsDominio = !paquetesConClases.contains(CODE + "." + primera);
            int indiceDeSubcarpeta = primeraEsDominio ? 2 : 1;
            if (segmentos.size() - 1 < indiceDeSubcarpeta) {
                continue;
            }
            if (segmentos.size() - 1 > indiceDeSubcarpeta
                    || !SUBCARPETAS_DE_ENTIDAD.contains(segmentos.get(indiceDeSubcarpeta))) {
                violaciones.add(clase.getName());
            }
        }
        assertTrue(violaciones.isEmpty(), "EST-02: dentro de Code/{Entidad}/ todo va plano; las unicas subcarpetas son"
                + " CasosDeUso/ y Notificaciones/ (docs/rules/estructura-de-carpetas.md). Clases fuera de lugar:\n  "
                + String.join("\n  ", violaciones));
    }

    @Test
    void est05_losTestsEspejanLaEstructuraDeMain() {
        Set<String> paquetesDeMain = clasesDeCode(clasesDeMain).stream()
                .map(JavaClass::getPackageName).collect(Collectors.toSet());
        List<String> violaciones = clasesDeCode(clasesDeTest).stream()
                .filter(clase -> !paquetesDeMain.contains(clase.getPackageName()))
                .map(JavaClass::getName).sorted().toList();
        assertTrue(violaciones.isEmpty(), "EST-05: app/src/test espeja exactamente la estructura de app/src/main; un"
                + " paquete de test tiene que existir en main (docs/rules/estructura-de-carpetas.md -> 'Tests')."
                + " Tests en un paquete que no existe en main:\n  " + String.join("\n  ", violaciones));
    }

    @Test
    void ent02_todaEntidadHeredaDeEntidadBase() {
        classes().that().areAnnotatedWith(Entity.class).and().resideInAPackage(CODE + "..")
                .should().beAssignableTo(EntidadBase.class)
                .because("ENT-02: toda @Entity hereda de EntidadConBorradoLogico (borrado logico, el default) o de"
                        + " EntidadBase (borrado fisico) (docs/rules/entidades-y-dtos.md -> 'Entities')")
                .check(clasesDeMain);
    }

    @Test
    void ent03_todoDtoHeredaDeEntidadBaseDto() {
        classes().that().haveSimpleNameEndingWith("DTO").and().resideInAPackage(CODE + "..")
                .and().areNotInterfaces()
                .should().beAssignableTo(EntidadBaseDTO.class)
                .because("ENT-03: todos los DTO heredan de EntidadBaseDTO (docs/rules/entidades-y-dtos.md -> 'DTOs')")
                .check(clasesDeMain);
    }

    @Test
    void ent06_fechasDeDtoConJsonFormat() {
        List<String> violaciones = new ArrayList<>();
        for (JavaClass clase : clasesDeCode(clasesDeMain)) {
            if (!clase.getSimpleName().endsWith("DTO")) {
                continue;
            }
            for (JavaField campo : clase.getFields()) {
                if (!TIPOS_DE_FECHA.contains(campo.getRawType().getName())) {
                    continue;
                }
                Optional<JavaAnnotation<JavaField>> formato = campo.tryGetAnnotationOfType(JSON_FORMAT);
                boolean correcto = formato.isPresent()
                        && PATRON_DE_FECHA.equals(formato.get().get("pattern").orElse(null))
                        && ZONA_HORARIA.equals(formato.get().get("timezone").orElse(null));
                if (!correcto) {
                    violaciones.add(clase.getName() + "." + campo.getName());
                }
            }
        }
        assertTrue(violaciones.isEmpty(), "ENT-06: toda fecha de un DTO lleva @JsonFormat(shape = STRING, pattern = \""
                + PATRON_DE_FECHA + "\", timezone = \"" + ZONA_HORARIA + "\") (docs/rules/entidades-y-dtos.md ->"
                + " 'Fechas'). Campos sin la anotacion o con otro patron:\n  " + String.join("\n  ", violaciones));
    }

    @Test
    void con01_todaRelacionEsLazy() {
        List<String> violaciones = new ArrayList<>();
        for (JavaClass clase : clasesDeCode(clasesDeMain)) {
            for (JavaField campo : clase.getFields()) {
                for (JavaAnnotation<JavaField> anotacion : campo.getAnnotations()) {
                    String tipo = anotacion.getRawType().getName();
                    if (!RELACIONES.contains(tipo)) {
                        continue;
                    }
                    Optional<Object> fetch = anotacion.get("fetch");
                    boolean lazy = fetch.map(valor -> ((JavaEnumConstant) valor).name().equals("LAZY"))
                            .orElse(!RELACIONES_EAGER_POR_DEFECTO.contains(tipo));
                    if (!lazy) {
                        violaciones.add(clase.getName() + "." + campo.getName() + " (@" + anotacion.getRawType().getSimpleName() + ")");
                    }
                }
            }
        }
        assertTrue(violaciones.isEmpty(), "CON-01: toda relacion JPA se declara con fetch = FetchType.LAZY; @ManyToOne"
                + " y @OneToOne son EAGER por defecto (docs/rules/consultas.md -> 'Carga de relaciones')."
                + " Relaciones EAGER:\n  " + String.join("\n  ", violaciones));
    }

    @Test
    void con07_losRepositoriosNoDevuelvenNullNiOptionalDeLista() {
        List<String> violaciones = new ArrayList<>();
        for (JavaClass clase : clasesDeCode(clasesDeMain)) {
            boolean esRepositorio = clase.isInterface()
                    && clase.isAssignableTo("org.springframework.data.repository.Repository");
            if (esRepositorio) {
                for (JavaMethod metodo : clase.getMethods()) {
                    JavaClass retorno = metodo.getRawReturnType();
                    if (retorno.isAssignableTo(EntidadBase.class) || retorno.isAssignableTo(EntidadBaseDTO.class)) {
                        violaciones.add(metodo.getFullName() + " devuelve " + retorno.getSimpleName()
                                + " a secas: para 0 o 1 registro va Optional<" + retorno.getSimpleName() + ">");
                    }
                    if (retorno.isEquivalentTo(Optional.class)
                            && metodo.getReturnType() instanceof JavaParameterizedType parametrizado
                            && parametrizado.getActualTypeArguments().get(0).toErasure().isAssignableTo(Collection.class)) {
                        violaciones.add(metodo.getFullName() + " devuelve Optional de una coleccion: va List<T> vacia");
                    }
                }
            }
            for (JavaField campo : clase.getFields()) {
                if (campo.getRawType().isEquivalentTo(Optional.class)) {
                    violaciones.add(clase.getName() + "." + campo.getName() + " es un campo Optional");
                }
            }
        }
        assertTrue(violaciones.isEmpty(), "CON-07: un repositorio nunca devuelve null (Optional<T> para 0 o 1, List<T>"
                + " para N, nunca Optional<List>) y Optional no se usa en campos (docs/rules/consultas.md ->"
                + " 'Ausencia de resultado'). Violaciones:\n  " + String.join("\n  ", violaciones));
    }

    @Test
    void ctr03_losControllersNoHeredan() {
        List<String> violaciones = clasesDeCode(clasesDeMain).stream()
                .filter(clase -> clase.isAnnotatedWith(RestController.class))
                .filter(clase -> !clase.getRawSuperclass().map(JavaClass::getName).orElse("java.lang.Object")
                        .equals("java.lang.Object"))
                .map(JavaClass::getName).sorted().toList();
        assertTrue(violaciones.isEmpty(), "CTR-03: los controllers no heredan de ninguna clase; el ABM se escribe con la"
                + " receta de core-crud (docs/rules/controllers-y-endpoints.md). Controllers con extends:\n  "
                + String.join("\n  ", violaciones));
    }

    @Test
    void ctr04_unUseCaseEsUnServiceConUnSoloMetodoEjecutar() {
        List<String> violaciones = new ArrayList<>();
        for (JavaClass clase : clasesDeCode(clasesDeMain)) {
            if (!clase.getSimpleName().endsWith("UseCase")) {
                continue;
            }
            if (!clase.isAnnotatedWith(Service.class)) {
                violaciones.add(clase.getName() + " no lleva @Service");
            }
            List<String> publicos = metodosPublicosDeclarados(clase).stream().map(JavaMethod::getName).sorted().toList();
            if (!publicos.equals(List.of("ejecutar"))) {
                violaciones.add(clase.getName() + " tiene los metodos publicos " + publicos + "; tiene que tener solo ejecutar");
            }
        }
        assertTrue(violaciones.isEmpty(), "CTR-04: cada caso de uso es una clase @Service {Entidad}{CasoDeUso}UseCase"
                + " con un unico metodo publico ejecutar (docs/rules/controllers-y-endpoints.md -> 'ABM vs. DTO por"
                + " caso de uso'). Violaciones:\n  " + String.join("\n  ", violaciones));
    }

    @Test
    void ctr05_elServiceDeLaEntidadSoloTieneHooks() {
        List<String> violaciones = new ArrayList<>();
        for (JavaClass clase : clasesDeCode(clasesDeMain)) {
            if (!clase.isAssignableTo(AbstractService.class) || clase.isEquivalentTo(AbstractService.class)) {
                continue;
            }
            Set<String> heredados = clase.getAllRawSuperclasses().stream()
                    .flatMap(superclase -> superclase.getMethods().stream())
                    .map(JavaMethod::getName).collect(Collectors.toSet());
            clase.getAllRawInterfaces().forEach(interfaz -> interfaz.getMethods().forEach(m -> heredados.add(m.getName())));
            for (JavaMethod metodo : metodosPublicosDeclarados(clase)) {
                if (!metodo.getName().startsWith("hook") && !heredados.contains(metodo.getName())) {
                    violaciones.add(metodo.getFullName());
                }
            }
        }
        assertTrue(violaciones.isEmpty(), "CTR-05: el Service de una entidad (extiende AbstractService) no crece con"
                + " casos de uso: solo hooks del ABM y overrides de su clase base. Un caso de uso va en su UseCase"
                + " (docs/rules/controllers-y-endpoints.md). Metodos publicos propios:\n  "
                + String.join("\n  ", violaciones));
    }

    @Test
    void err02_unSoloEnumDeCodigosDeErrorDelProyecto() {
        List<String> enums = clasesDeCode(clasesDeMain).stream()
                .filter(clase -> clase.isAssignableTo(CodigoDeError.class) && !clase.isInterface())
                .map(JavaClass::getName).sorted().toList();
        assertTrue(enums.isEmpty() || enums.equals(List.of(CODE + ".Common.Errores.CodigoDeErrorDelProyecto")),
                "ERR-02: los codigos de error del proyecto viven en un unico enum"
                        + " Code/Common/Errores/CodigoDeErrorDelProyecto; no se crea un enum por entidad"
                        + " (docs/rules/errores.md, regla 6). Enums encontrados: " + enums);
    }

    // Clases del proyecto (com.blue.open.Code..), sin las anonimas ni las generadas
    private static List<JavaClass> clasesDeCode(JavaClasses clases) {
        return clases.stream()
                .filter(clase -> clase.getPackageName().startsWith(CODE))
                .filter(clase -> !clase.isAnonymousClass())
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .toList();
    }

    // Segmentos del paquete por debajo de Code: para Code.Usuario.CasosDeUso -> [Usuario, CasosDeUso]
    private static List<String> segmentosBajoCode(JavaClass clase) {
        String resto = clase.getPackageName().substring(CODE.length());
        if (resto.isEmpty()) {
            return List.of();
        }
        return List.of(resto.substring(1).split("\\."));
    }

    // Metodos publicos declarados por la propia clase, sin los sinteticos (bridges) ni los estaticos
    private static List<JavaMethod> metodosPublicosDeclarados(JavaClass clase) {
        return clase.getMethods().stream()
                .filter(metodo -> metodo.getModifiers().contains(JavaModifier.PUBLIC))
                .filter(metodo -> !metodo.getModifiers().contains(JavaModifier.SYNTHETIC))
                .filter(metodo -> !metodo.getModifiers().contains(JavaModifier.BRIDGE))
                .filter(metodo -> !metodo.getModifiers().contains(JavaModifier.STATIC))
                .toList();
    }
}
