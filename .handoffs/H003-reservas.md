# H003 — Completar el ciclo individual seguro

## Objetivo y alcance

Trabajar exclusivamente T006, T007. Los criterios, dependencias y estados están en
[tasks.md](../specs/001-booking-espacios/tasks.md); no mantener otro tablero aquí.

## Contexto de entrada

H002 y dependencias concretas en tasks.md. Leer [AGENTS](../AGENTS.md), [constitución](../.specify/memory/constitution.md),
[spec](../specs/001-booking-espacios/spec.md), [plan](../specs/001-booking-espacios/plan.md)
y [bitácora](../docs/BITACORA.md), especialmente DEC-002/DEC-003 y decisiones posteriores.
Inspeccionar Git/código antes de actuar: aprovechar lo existente, no repetir trabajo.

## Límites y evidencia exigida

Propiedad, CSRF, validación y bloqueo desde el comienzo; pruebas de intervalos y ciclo individual. No implementar recurrencia en este bloque.

Los criterios completos se mantienen en las tareas canónicas. Registrar comandos,
resultados, pruebas omitidas/fallidas y alcance real; no inferir aprobación humana.

## Continuación

Sin ejecución de este bloque durante H001. Al trabajarlo registrar fecha/entorno,
responsable, archivos/diff, evidencia, referencias T/DEC y commit real si existe;
actualizar el estado solo en tasks.md. Conservar el punto de continuación si se interrumpe.
Siguiente bloque orientativo: H004, sujeto a autorización y dependencias.

## Ejecución H003 — 2026-09-14, DEC-009

T006/T007: creación 201, colisión 409, adyacencia permitida; listado propio y cancelación
propia idempotente 204, ajena 404 sin cambios; hueco liberado. 27 pruebas totales
correctas: 10 intervalos y 2 escenarios API/Oracle añadidos. MockMvc usa filtros y
servicios reales; no se presenta como navegador. Rollback de transacciones de test.
Comprobar con el comando Maven verify del README; código en booking y SpaceRepository.
Commit T006 T007 DEC-009. Siguiente H004: cliente Angular y navegador real.
