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

**Total acumulado medido:** 375 min
