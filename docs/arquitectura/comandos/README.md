# Comandos de Claude Code

El template trae comandos personalizados de [Claude Code](https://docs.claude.com/claude-code) en `.claude/commands/`. Son archivos Markdown versionados en git: cualquier proyecto creado desde este template los hereda automáticamente, sin ningún paso de configuración (no usan ningún placeholder `REMPLAZAR`).

## `/test-completo`

Archivo: [`.claude/commands/test-completo.md`](../../../.claude/commands/test-completo.md).

Instala el Core (`mvn install -DskipTests -q`), corre la suite completa (`mvn -pl app test`: unitarios + pruebas de contrato + completitud) y resume el resultado: totales, duración y una tabla con cada test fallido y su causa. Sin Docker: la base es el PostgreSQL embebido de `core-test` (ver [`testing/README.md`](../testing/README.md)).

Si la suite queda en verde, la corrida regenera `docs/api`.

## `/auditar`

Archivo: [`.claude/commands/auditar.md`](../../../.claude/commands/auditar.md).

Compara el código contra las reglas de la empresa y reporta los desvíos, con id de regla, severidad, archivo y
propuesta. Por defecto audita el diff de la rama contra `develop`; `/auditar todo` audita el proyecto entero (para
calibrar un proyecto existente) y `/auditar {ruta}` una carpeta. Corre primero los tests determinísticos
(`CorePublicadoTest`, `ArquitecturaTest`, `EstiloDeCodigoTest`, `CodigosDeErrorTest`) y después revisa por
criterio las filas del checklist marcadas `agente`. Respeta `docs/excepciones.md`. No corrige: informa.

Cómo funciona todo el sistema (capas, severidades, excepciones, Core publicado):
[`../../auditoria/README.md`](../../auditoria/README.md).

## Agregar un comando nuevo

Un archivo `.claude/commands/{nombre}.md` con frontmatter `description` alcanza para que Claude Code lo reconozca como `/{nombre}`. Antes de crear uno, verificar que no exista ya algo similar en esta carpeta.
