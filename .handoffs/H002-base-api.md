# H002 — Preparar datos, contrato e identidad

## Objetivo y alcance

Trabajar exclusivamente T003, T004, T005. Los criterios, dependencias y estados están en
[tasks.md](../specs/001-booking-espacios/tasks.md); no mantener otro tablero aquí.

## Contexto de entrada

H001 realizado; continuación a T003 autorizada por DEC-004, arquitectura por
funcionalidades elegida por el usuario en DEC-005. No hay dependencia del starter. Leer [AGENTS](../AGENTS.md), [constitución](../.specify/memory/constitution.md),
[spec](../specs/001-booking-espacios/spec.md), [plan](../specs/001-booking-espacios/plan.md)
y [bitácora](../docs/BITACORA.md), especialmente DEC-002/DEC-003 y decisiones posteriores.
Inspeccionar Git/código antes de actuar: aprovechar lo existente, no repetir trabajo.

## Límites y evidencia exigida

Reutilizar BOOKING/FREEPDB1, credenciales, scripts y sondas. No recrear la base ni el marcador. Entidades y esquema de negocio no existen. No añadir CRUD de salas/usuarios ni Azure real.

Los criterios completos se mantienen en las tareas canónicas. Registrar comandos,
resultados, pruebas omitidas/fallidas y alcance real; no inferir aprobación humana.

## Continuación

Sin ejecución de este bloque durante H001. Al trabajarlo registrar fecha/entorno,
responsable, archivos/diff, evidencia, referencias T/DEC y commit real si existe;
actualizar el estado solo en tasks.md. Conservar el punto de continuación si se interrumpe.
Siguiente bloque orientativo: H003, sujeto a autorización y dependencias.
