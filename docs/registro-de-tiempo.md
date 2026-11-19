# Registro de tiempo

Tiempo de trabajo por prompt (desde que llega el pedido hasta que se entrega la respuesta). Zona horaria: America/Argentina/Buenos_Aires.

Las filas 1 a 22 vienen de la sesión de análisis y diseño (Cowork). Los turnos anteriores a las 14:52 no tienen hora de fin tomada con un comando; se dejan sin duración.

| # | Fecha | Inicio | Fin | Duración | Fase | Pedido | Resultado |
|---|---|---|---|---|---|---|---|
| 1 | 2026-11-18 | 14:01–14:48 | — | sin medir | Intake | Rondas de intake del DAF | Secciones 1 a 14 relevadas |
| 2 | 2026-11-18 | 14:52 | 14:53 | 1 min | Intake | Supuestos y respaldos | Sección 9 cerrada |
| 3 | 2026-11-18 | 14:54 | 14:56 | 2 min | Intake | Consulta histórica; controles de cierre | 10 épicas, 25 historias, matriz |
| 4 | 2026-11-18 | 14:58 | 14:58 | 0 min | Intake | Clonado y versiones | Reglas actualizadas |
| 5 | 2026-11-18 | 15:07 | 15:07 | 0 min | Intake | Revisión de 21 sugerencias de otra IA | Evaluación y recomendaciones |
| 6 | 2026-11-18 | 15:12 | 15:14 | 2 min | Intake | Aplicar sugerencias | 11 épicas, 29 historias, 17 RF |
| 7 | 2026-11-18 | 15:16 | 15:16 | 0 min | Diseño | Dónde hacer el diseño | Diseño técnico en Cowork |
| 8 | 2026-11-18 | 15:18 | 15:21 | 3 min | Diseño | Cierre del DAF y enfoques | DAF v1.0.0; 3 enfoques |
| 9 | 2026-11-18 | 15:27 | 15:29 | 2 min | Diseño | Revisión de core-parent | Ajustes por el Core |
| 10 | 2026-11-18 | 15:37 | 15:37 | 0 min | Diseño | Acceso a core-back-bod | Repositorio privado |
| 11 | 2026-11-18 | 15:38 | 15:42 | 4 min | Diseño | Spec completa | Spec 1 a 7 |
| 12 | 2026-11-18 | 15:43 | 15:43 | 0 min | Plan | Plan sin pantallas | Se arma ahora |
| 13 | 2026-11-18 | 15:47 | 15:48 | 1 min | Plan | Plan de implementación | 44 tareas, cronograma |
| 14 | 2026-11-18 | 15:49 | 15:49 | 0 min | Plan | Fechas y repositorios | Cronograma comprimido propuesto |
| 15 | 2026-11-18 | 17:21 | 17:26 | 5 min | Plan | Actualizar fechas | Baja de Jira 29/1/2027 |
| 16 | 2026-11-18 | 17:28 | 17:28 | 0 min | Plan | Crear repositorios | No permitido desde la sesión |
| 17 | 2026-11-18 | 17:29 | 17:29 | 0 min | Plan | Calidad del código | Propuesta de controles |
| 18 | 2026-11-18 | 17:30 | 17:30 | 0 min | Plan | TDD | Confirmado en el plan |
| 19 | 2026-11-18 | 17:30 | 17:32 | 2 min | Plan | Calidad en el plan | T0.6 y criterios de calidad |
| 20 | 2026-11-18 | 17:35 | 17:43 | 0 min | Plan | Acceso a repositorios (4 turnos de menos de 1 minuto) | Sin escritura desde Cowork |
| 21 | 2026-11-18 | 17:46 | 17:46 | 0 min | Plan | Traspaso | Este documento |
| 22 | 2026-11-18 | 17:50 | 17:52 | 2 min | Soporte | Adendas 1 y 2 del DAF | Plan y spec actualizados |
| 23 | 2026-11-18 | 17:57 | 17:57 | 0 min | Build | Analizar el traspaso antes de F0 | Spec y plan leídos (45 tareas); bloqueos de F0 listados; registro creado |
| 24 | 2026-11-18 | 18:16 | 18:19 | 3 min | Build | Respuestas a diferencias; Core, sm-template y umbrales | Core y sm-template leídos; JDK 25 instalado; adenda de implementación 1 en el plan; títulos y repositorios corregidos |
| 25 | 2026-11-18 | 18:20 | — | sin medir | Build | Bitácora del programador; respuestas v2, git y protección de ramas | Interrumpido. Bitácora creada; plantilla leída |
| 26 | 2026-11-18 | 18:37 | 18:53 | 16 min | Build | Procede con F0 | T0.1 (c4f0a22) y T0.2 (cda618f) en verde; T0.3 espera decisión sobre el usuario de base |
| 27 | 2026-11-18 | 18:57 | 19:08 | 11 min | Build | T0.3 opción A; push de fe y be; guía local | T0.3 (cac9d1c), deploy.yml local, front T0.4 (shiro-fe 0b0cf0d), docs/levantar-en-local.md |
| 28 | 2026-11-18 | 19:31 | 19:32 | 1 min | Verificación | Resultado de la prueba local en Windows y observaciones | .gitattributes, guía para Windows, spec corregida; commits locales sin push (falta confirmación) |
| 29 | 2026-11-18 | 19:34 | 19:58 | 24 min | Build | Push autorizado; T0.6 integración continua | Workflow Calidad en be (7d47e14) y fe (7d7f0f8), GitHub Actions en verde; parches de Tomcat y Jackson; F0 completa |
| 30 | 2026-11-18 | 20:02 | 20:11 | 9 min | Plan | F0 aprobada; reescribir historial; develop; leer DAF y prototipo | Historial de shiro-be reescrito (8f6dd1a, versión 1.0.5); develop pendiente; DAF y 64 vistas leídos, diferencias listadas |
| 31 | 2026-11-18 | 20:14 | 20:23 | 9 min | Build | Force push de develop; adenda 3 del DAF; OK a la tabla de F1 | develop en c13a6be (CI verde); adenda 3 en DAF y spec; T1.1 cerrada (ce482a4, versión 1.0.6); tareas de F1 en la lista |
| 32 | 2026-11-18 | 20:24 | 20:24 | 0 min | Soporte | Consulta: nivel de esfuerzo del modelo para F1 | Recomendación dada; T1.2 interrumpido antes de empezar |
| 33 | 2026-11-18 | 20:26 | 20:26 | 0 min | Soporte | Agregar a la skill la recomendación de nivel de esfuerzo | Regla agregada a la copia local de la skill y SKILL.md listo para cargar en claude.ai |
| 34 | 2026-11-18 | 20:28 | 20:39 | 11 min | Build | Procede con T1.2 | T1.2 cerrada: proyectos con cuatro tableros y prefijo único (4341c68, versión 1.0.7) |
| 35 | 2026-11-18 | 21:01 | 21:19 | 18 min | Build | T1.3 (esfuerzo Alto): pruebas en rojo, tareas en modo rápido y edición, clave con bloqueo de fila, 50 altas concurrentes; arreglo del pool (ConfiguracionDesdeBase) | Cerrada, verify verde, push |
| 36 | 2026-11-18 | 21:23 | 23:53 | 150 min | Build | T1.4 a T1.10 sin esperar aprobación; bitácora movida al repositorio; cierre de F1. Incluye una pausa por el límite de uso y un reinicio del worker, que no se pudieron medir aparte | F1 terminada, verify verde, push; falta aprobación |
| 37 | 2026-11-19 | 00:12 | 01:05 | 53 min | Build | Adenda 4 (DAF) y ajustes de F1; F2 completa (T2.1 a T2.5); F3 T3.1 a T3.3. Primer tramo del turno: el turno sigue con F5 y F4 | F2 terminada, F3 salvo T3.4; push en cada tarea |
| 38 | 2026-11-19 | 01:05 | 01:51 | 46 min | Build | Segundo tramo del turno: F3 T3.4 dejada pendiente, F5 completa (T5.1 a T5.5) y F4 completa contra un Jira simulado (T4.1 a T4.4), sin esperar aprobación | F5 y F4 terminadas, verify verde (469 pruebas), push en cada tarea; versión 1.0.30 |
| 39 | 2026-11-19 | 01:52 | 04:36 | 164 min | Build | T6.1 (front: ingreso, menú, tablero, crear tarea y proyecto) y prueba del filtro por tablero en el backend. Incluye una pausa por el límite de uso y un reinicio del worker, que no se pudieron medir aparte | T6.1 terminada; front y back en verde, push en los dos repositorios; versión 1.0.31 |
| 40 | 2026-11-19 | 06:53 | 08:22 | 89 min | Docs | Actualizar en Claude Docs el DAF, el plan, la bitácora y el diseño técnico con las adendas como propuestas | Adendas 5 y 6 en el DAF, plan y bitácora al día, adenda técnica |
| 41 | 2026-11-19 | 08:22 | 08:23 | 1 min | Docs | Lista numerada de decisiones pendientes del usuario y de las tomadas por el agente | Lista A1 a A14 y B1 a B55 entregada |
| 42 | 2026-11-19 | 08:45 | 09:14 | 29 min | Build | Resoluciones A y B del 19/11: clon de subtarea, claves J-PREFIJO-n, solo Atlassian, estados y tipos creados en el mapeo, usuarios importados, duración de sprint, grupos de la sesión, tema oscuro y etiqueta del ingreso; /auditar | Resoluciones en back y front con pruebas en rojo antes; auditoría con 30 bloqueantes y 4 advertencias |
| 43 | 2026-11-19 | 09:15 | 09:18 | 3 min | Consulta | Pregunta sobre crear tokens de Jira para la prueba real | Explicadas las variables JIRA_PRUEBA_* en la configuración del entorno |
| 44 | 2026-11-19 | 09:21 | 09:47 | 26 min | Build | Corrección de los hallazgos de /auditar con las decisiones del usuario, nueva auditoría y prueba del importador contra el Jira real (se omite sin las variables) | verify verde, 6 bloqueantes nuevos del criterio MCP corregidos, push; versión 1.0.35 |
| 45 | 2026-11-19 | 10:05 | 10:11 | 6 min | Consulta | Pedido de un prompt para la prueba de Jira (interrumpido) y aviso de merge y de la prueba en otro chat | Sin cambios |
| 46 | 2026-11-19 | 10:11 | 10:12 | 1 min | Consulta | Qué procede en F6 y cuánto falta del plan | Resumen: 11 de 45 tareas pendientes, 5 de F6 a cargo del agente |
| 47 | 2026-11-19 | 10:13 | 10:14 | 1 min | Docs | Prompt de contexto para el agente que hace el deploy en el servidor de la empresa | Prompt entregado, sin secretos |
| 48 | 2026-11-19 | 10:17 | 10:17 | 0 min | Consulta | Resultado de la prueba del importador contra el Jira real en otra sesión | T4.1 cerrada en la bitácora; aviso de que develop de shiro-be no tenía el merge |
| 49 | 2026-11-19 | 10:25 | 10:34 | 9 min | Build | CI del PR Merge fase 1 con 3 fallos (gitleaks y OSV-Scanner) | Excepción de gitleaks para los tokens de ejemplo de los seeds; CI en verde y PR mergeado; versión 1.0.37 |
| 50 | 2026-11-19 | 11:14 | 11:27 | 13 min | Build | T6.2 en el front: detalle de tarea, clonar, relacionar, comentarios, historial y archivar | Pruebas en rojo (9e607bf) y luego 44 en verde, lint, tipos, build, 0 duplicados; versión del front 0.0.2 |
| 51 | 2026-11-19 | 12:48 | 12:57 | 9 min | Build | Resoluciones de las propuestas 42 a 45 de T6.2: usuarios del proyecto eligen responsable, Markdown con vista previa, clonar solo en el proyecto e historias desde la épica | Backend: GET /proyecto/{uuid}/usuarios con prueba en rojo antes, verify 511 pruebas, versión 1.0.39. Front: pruebas en rojo antes, 50 en verde, versión 0.0.3 |
| 52 | 2026-11-19 | 13:02 | 13:13 | 11 min | Build | T6.3 en el front: backlog y sprints, y versiones con changelog | Backend: GET /version/{uuid}/tareas con prueba en rojo antes, verify 514 pruebas, versión 1.0.41. Front: pruebas en rojo (dceb753), 67 en verde, versión 0.0.4 |
| 53 | 2026-11-19 | 13:16 | 13:52 | 36 min | Build | Resoluciones 46 y 47 (cierre de sprint guardado, cantidad de tareas y quién publicó) y T6.4 en el front: búsqueda, archivados y tokens MCP | Backend: pruebas en rojo antes, verify 525 pruebas, versión 1.0.44. Front: pruebas en rojo antes, 80 en verde, versión 0.0.6 |
| 54 | 2026-11-19 | 13:56 | 14:57 | 61 min | Build | Resoluciones 49 a 51: vista previa e incluidos de archivados, último uso y revocados de tokens, filtros de varios valores y sin sprint; documentos en Claude Docs | Backend: pruebas en rojo antes, verify 534 pruebas, versión 1.0.46. Front: pruebas en rojo antes, 82 en verde, versión 0.0.7 |

**Total acumulado medido:** 834 min
