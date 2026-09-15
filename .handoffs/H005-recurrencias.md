# H005 — Conservar ocurrencias válidas de una serie

## Objetivo y alcance

Trabajar exclusivamente T009, T010. Los criterios, dependencias y estados están en
[tasks.md](../specs/001-booking-espacios/tasks.md); no mantener otro tablero aquí.

## Contexto de entrada

H004; confirmar refinamientos de recurrencia. Leer [AGENTS](../AGENTS.md), [constitución](../.specify/memory/constitution.md),
[spec](../specs/001-booking-espacios/spec.md), [plan](../specs/001-booking-espacios/plan.md)
y [historia consolidada](../BITACORA.md), especialmente DEC-002/DEC-003 y decisiones posteriores.
Inspeccionar Git/código antes de actuar: aprovechar lo existente, no repetir trabajo.

## Límites y evidencia exigida

Expansión finita y resultado por ocurrencia; no RRULE completo, cron ni entidad Serie sin necesidad. Error técnico revierte el pedido; conflicto de negocio conserva válidas si se confirma esa semántica.

Los criterios completos se mantienen en las tareas canónicas. Registrar comandos,
resultados, pruebas omitidas/fallidas y alcance real; no inferir aprobación humana.

## Continuación

Sin ejecución de este bloque durante H001. Al trabajarlo registrar fecha/entorno,
responsable, archivos/diff, evidencia, referencias T/DEC y commit real si existe;
actualizar el estado solo en tasks.md. Conservar el punto de continuación si se interrumpe.
Siguiente bloque orientativo: H006, sujeto a autorización y dependencias.

## Ejecución H005 — 2026-09-14, DEC-011

T009/T010: occurrences opcional 1–12 semanal Bogotá; {created,rejected}, 201/200/409;
un bloqueo/transacción por pedido. verify 35/35; casos de límites, mes, conflicto
interno y segunda semana. Angular build y test correctos. Navegador: Ana #72
(12/02/2035, Zona colaborativa); Bruno solicitó cuatro desde 05/02 y recibió #73–75,
rechazo de 12/02 sin datos de Ana. Listado propio confirmado; todas estas reservas
se cancelaron con su propietario, sin borrar filas. Corrección UTF-8 de textos y
--poll 1000 para detectar cambios sobre bind mount Windows. URL localhost:4200,
comandos de Maven/Angular y app-process.sh; commit T009 T010 DEC-011. Siguiente H006.
