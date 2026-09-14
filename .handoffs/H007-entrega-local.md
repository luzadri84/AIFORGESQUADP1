# H007 — Preparar entrega local verificable

## Objetivo y alcance

Trabajar exclusivamente T013. Los criterios, dependencias y estados están en
[tasks.md](../specs/001-booking-espacios/tasks.md); no mantener otro tablero aquí.

## Contexto de entrada

H006. Leer [AGENTS](../AGENTS.md), [constitución](../.specify/memory/constitution.md),
[spec](../specs/001-booking-espacios/spec.md), [plan](../specs/001-booking-espacios/plan.md)
y [bitácora](../docs/BITACORA.md), especialmente DEC-002/DEC-003 y decisiones posteriores.
Inspeccionar Git/código antes de actuar: aprovechar lo existente, no repetir trabajo.

## Límites y evidencia exigida

WAR, ejecución y documentación candidata. Cumplir las doce secciones, doce preguntas y seis retos de [ENTREGA_Y_STARTER.md](../docs/ENTREGA_Y_STARTER.md), referidas al código real. No implementar los retos como ampliación del MVP.

Los criterios completos se mantienen en las tareas canónicas. Registrar comandos,
resultados, pruebas omitidas/fallidas y alcance real; no inferir aprobación humana.

## Continuación

Sin ejecución de este bloque durante H001. Al trabajarlo registrar fecha/entorno,
responsable, archivos/diff, evidencia, referencias T/DEC y commit real si existe;
actualizar el estado solo en tasks.md. Conservar el punto de continuación si se interrumpe.
Siguiente bloque orientativo: H008, sujeto a autorización y dependencias.

## Ejecución y cierre — 2026-09-14, America/Bogota

Codex completó T013 bajo DEC-007/013 desde H006/0344b9a. scripts/app.ps1 ofrece
start/verify/stop/status con ambos Compose --no-build; app-process.sh valida PID
antes de detener procesos. No modifica credenciales existentes, imágenes ni volúmenes.

Evidencia: [VERIFICACION_FINAL](../docs/VERIFICACION_FINAL.md). verify completo:
50 tests backend (0 fallos/errores/omitidas), ngc/build strict y npm test 1/1.
Se añadieron 11 casos Oracle de fronteras/estado/espacio para complementar el helper.
WAR ejecutable inspeccionado. Stop/start real de ambos contenedores, Oracle healthy,
marcador original y credenciales byte a byte intactos, localhost exclusivo; navegador
posterior autentica ana y carga catálogo/lista. Cinco filas CANCELLED de pruebas
visuales preservadas; cero activas. Sin Azure real, WebLogic ni auditoría integral.

README y explicación reflejan código real; 12 secciones, 15 preguntas, 6 retos no
implementados. Spec/plan/AGENTS/constitución reconciliados; históricos conservados.
Validación de 140 enlaces locales/28 documentos y 15 IDs; precondiciones Spec Kit
correctas. Commit identificable por T013/DEC-013 en Git, sin anticipar hash.

Comprobar: http://localhost:4200/; credenciales solo .local/runtime/booking.properties.
Comandos desde raíz: pwsh -NoProfile -File scripts/app.ps1 start (o verify/stop/status).
Aplicación dejada iniciada. Próxima gestión: H008/T015, aclaración externa del runtime;
T014 no aplica y no se ejecuta. No queda otra tarea local del alcance autorizado.

## Auditoría posterior T017 — DEC-015, 2026-09-14

La auditoría de main 47a6af4 no corrige producción. [Informe](../docs/AUDITORIA_CUMPLIMIENTO_Y_SEGURIDAD.md)
y [plan](../docs/PLAN_CORRECCIONES_AUDITORIA.md); estados solo en tasks.md. Se conservan
las ejecuciones históricas anteriores, sin usarlas como certificación integral.
76 casos Java distintos:70 pasan/6 fallan (dos ejecuciones), frontend 13/13/build y comparador 3/3.
Regresiones rojas deliberadamente conservadas. Dependencias:65 coincidencias Maven/0 npm,
con condiciones de aplicabilidad; no se afirman 65 vulnerabilidades explotadas.
T018–T022 proponen correcciones; T023–T024 opcionales. T015 independiente, T014 no aplica.
Dev Container completo no acreditado; no confundirlo con Compose funcional ni reabrir H001.
La aplicación queda iniciada; verify ahora falla por los hallazgos y requiere start después.
Siguiente: presentar diagnóstico y obtener alcance para correcciones, sin ejecutarlas aún.
