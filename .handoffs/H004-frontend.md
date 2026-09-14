# H004 — Conectar el primer recorrido de usuario

## Objetivo y alcance

Trabajar exclusivamente T008. Los criterios, dependencias y estados están en
[tasks.md](../specs/001-booking-espacios/tasks.md); no mantener otro tablero aquí.

## Contexto de entrada

H003. Leer [AGENTS](../AGENTS.md), [constitución](../.specify/memory/constitution.md),
[spec](../specs/001-booking-espacios/spec.md), [plan](../specs/001-booking-espacios/plan.md)
y [bitácora](../docs/BITACORA.md), especialmente DEC-002/DEC-003 y decisiones posteriores.
Inspeccionar Git/código antes de actuar: aprovechar lo existente, no repetir trabajo.

## Límites y evidencia exigida

UI mínima y recorrido real de navegador hasta Oracle. No calendario complejo ni presentar mocks como E2E.

Los criterios completos se mantienen en las tareas canónicas. Registrar comandos,
resultados, pruebas omitidas/fallidas y alcance real; no inferir aprobación humana.

## Continuación

Sin ejecución de este bloque durante H001. Al trabajarlo registrar fecha/entorno,
responsable, archivos/diff, evidencia, referencias T/DEC y commit real si existe;
actualizar el estado solo en tasks.md. Conservar el punto de continuación si se interrumpe.
Siguiente bloque orientativo: H005, sujeto a autorización y dependencias.

## Ejecución H004 — 2026-09-14, DEC-010

Angular standalone strict, PrimeNG/Tailwind/RxJS, funcionalidades acceso/reservas;
Basic/token en memoria y ruta propia probada. ng build correcto, npm test 1/1;
backend regresión 27/27 y nuevo HttpSecurityTest 1/1 con servidor y cookies reales.
Navegador localhost:4200: ana creó #55 en Auditorio 20/01/2035 10–11 Bogotá; apareció
en listado, duplicado rechazado y cancelación dejó agenda vacía. Se conserva fila
cancelada, no se borra evidencia. Captura revisada visualmente. Hubo carga prematura
antes de terminar ng serve (ERR_EMPTY_RESPONSE), resuelta esperando compilación.
El 403 real tras lecturas paralelas llevó a STATELESS para contexto de seguridad;
sesión explícita solo CSRF, protección activa. Comprobación reproducible: iniciar
backend/frontend con scripts/app-process.sh dentro de dev; URL http://localhost:4200/.
Claves en .local/runtime/booking.properties, no impresas. Siguiente H005 T009/T010.
