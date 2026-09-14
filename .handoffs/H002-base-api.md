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

Reutilizar BOOKING/FREEPDB1, credenciales, scripts y sondas. No recrear la base ni el marcador. T003 incorpora entidades y esquema aditivo; consultar su evidencia antes de tocar SQL. No añadir CRUD de salas/usuarios ni Azure real.

Los criterios completos se mantienen en las tareas canónicas. Registrar comandos,
resultados, pruebas omitidas/fallidas y alcance real; no inferir aprobación humana.

## Continuación histórica al cerrar T003 (superada por el cierre H002 inferior)

H001 no ejecutó este bloque. Continuación real T003 el 2026-09-14, America/Bogota,
por Codex tras DEC-004/005; detalle técnico DEC-006. Commit documental previo
`75af194`. [Informe T003](../docs/VERIFICACION_T003.md): archivos, comandos, primer
fallo de aserción corregido, 12 pruebas Oracle correctas y WAR base generado.
Las transacciones de prueba se revirtieron; hay tres espacios y ninguna reserva.
Esquema V001 ya aplicado: no reaplicar ni borrar objetos. Marcador/secretos conservados.

Siguiente tarea pendiente: T004, identidad Basic y CSRF con credenciales externas;
no inferir que la dependencia Security del POM acredita su aceptación. T005 sigue
al contrato. T003 no arrancó HTTP, añadió UI ni implementó servicios de reservas.
Los estados se actualizan exclusivamente en tasks.md; no ejecutar todo el handoff
por disponer de este contexto. H003 continúa después de dependencias y autorización.

## Cierre restante H002 — 2026-09-14, DEC-007/008

T004/T005 incorporan Basic externo, CSRF de sesión, DTO estricto, errores y catálogo
HTTP. T003 se conserva. Maven verify: 15 tests, 0 fallos/errores; primer fallo de
renovación CSRF corregido y registrado en bitácora. SecurityContractTest verifica
HTTP con MockMvc y ruta de validación exclusiva de tests, no crea reservas reales.
Comando: docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash mvnw -B -ntp -f backend/pom.xml verify
WAR ejecutable generado; no iniciado todavía. Commit con T004 T005 DEC-008.
Siguiente H003: motor individual, propiedad y transacciones Oracle. DEC-007 autoriza
continuar sin otra confirmación; estado canónico siempre tasks.md.
