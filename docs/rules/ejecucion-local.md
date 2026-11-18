# Ejecución del Proyecto

- En los proyectos debe existir una carpeta `deploy` en la raíz del proyecto.
- Si la carpeta `deploy` no existe, el proyecto no se puede ejecutar. El agente debe aclararle al usuario que copie la carpeta `sm-template\deploy` y el archivo `sm-template\deploy.yml` al proyecto actual.
- Cuando la carpeta `deploy` y el archivo `deploy.yml` existen, el proyecto se ejecuta con Docker mediante:
  ```bash
  python deploy/deploy.py --local
  ```
  Este comando reinicia el contenedor actual (si existe) y levanta los cambios nuevos.
- La documentación completa del deploy, sus parámetros y formas de ejecución está en `deploy/deploy.md`.

## Build multi-módulo

- `mvn` se ejecuta siempre desde la raíz. El `pom.xml` raíz es un agregador con un solo módulo, `app`.
- Los módulos del Core no están en el repositorio: Maven los descarga de `https://sm-soluciones.github.io/sm-core-maven` (declarado en `app/pom.xml`), sin token. La primera compilación necesita acceso a internet.
- **El jar del proyecto se genera en `app/target/`, no en `target/`.** Un `Dockerfile` heredado de Core 1.x que copie `target/*.jar` hay que ajustarlo.
