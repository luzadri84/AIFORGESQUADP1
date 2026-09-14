# H008 — Aclaración del runtime; starter histórico excluido

## Objetivo y alcance

Trabajar únicamente el pendiente vigente T015. T014 se conserva como referencia
histórica, No aplica por cambio de alcance (DEC-004), sin implementación. Los criterios, dependencias y estados están en
[tasks.md](../specs/001-booking-espacios/tasks.md); no mantener otro tablero aquí.

## Contexto de entrada

Al recibir insumos, sin esperar al final de otros bloques. Leer [AGENTS](../AGENTS.md), [constitución](../.specify/memory/constitution.md),
[spec](../specs/001-booking-espacios/spec.md), [plan](../specs/001-booking-espacios/plan.md)
y [bitácora](../docs/BITACORA.md), especialmente DEC-002/DEC-003 y decisiones posteriores.
Inspeccionar Git/código antes de actuar: aprovechar lo existente, no repetir trabajo.

## Límites y evidencia exigida

Los nueve pasos de recepción de [ENTREGA_Y_STARTER.md](../docs/ENTREGA_Y_STARTER.md)
son un procedimiento histórico superado por DEC-004: no ejecutarlos, no migrar ni
inventar un defecto equivalente. Mantener exclusivamente la aceptación de T015:
aclaración del runtime y evidencia exigida, sin declarar WebLogic validado por un WAR.
La compatibilidad de despliegue permanece independiente; no publicar.

Los criterios completos se mantienen en las tareas canónicas. Registrar comandos,
resultados, pruebas omitidas/fallidas y alcance real; no inferir aprobación humana.

## Continuación

Sin ejecución de este bloque durante H001. Al trabajarlo registrar fecha/entorno,
responsable, archivos/diff, evidencia, referencias T/DEC y commit real si existe;
actualizar el estado solo en tasks.md. Conservar el punto de continuación si se interrumpe.
Siguiente bloque orientativo: verificaciones afectadas y cierre de requisitos, sujeto a autorización y dependencias.
