# H006 — Cerrar riesgos técnicos e identidad futura

## Objetivo y alcance

Trabajar exclusivamente T011, T012. Los criterios, dependencias y estados están en
[tasks.md](../specs/001-booking-espacios/tasks.md); no mantener otro tablero aquí.

## Contexto de entrada

H005 y dependencias concretas en tasks.md. Leer [AGENTS](../AGENTS.md), [constitución](../.specify/memory/constitution.md),
[spec](../specs/001-booking-espacios/spec.md), [plan](../specs/001-booking-espacios/plan.md)
y [bitácora](../docs/BITACORA.md), especialmente DEC-002/DEC-003 y decisiones posteriores.
Inspeccionar Git/código antes de actuar: aprovechar lo existente, no repetir trabajo.

## Límites y evidencia exigida

Pruebas Oracle de carrera y rollback, pruebas negativas de seguridad y adaptadores compilables. No Azure real, benchmark masivo ni seguridad total inferida de npm audit.

Los criterios completos se mantienen en las tareas canónicas. Registrar comandos,
resultados, pruebas omitidas/fallidas y alcance real; no inferir aprobación humana.

## Continuación

Sin ejecución de este bloque durante H001. Al trabajarlo registrar fecha/entorno,
responsable, archivos/diff, evidencia, referencias T/DEC y commit real si existe;
actualizar el estado solo en tasks.md. Conservar el punto de continuación si se interrumpe.
Siguiente bloque orientativo: H007, sujeto a autorización y dependencias.

## Ejecución H006 — 2026-09-14, DEC-012

T011/T012: verify 39/39; OracleConcurrencyTest usa servidor HTTP, clientes separados
y un lock JDBC retenido hasta observar ambas entradas: un 201, un 409 y una fila.
Spy delega al proxy Spring Data real. Fallo técnico inyectado después de flush real
y consulta interna de una fila; respuesta 503 y cero filas externas tras rollback.
Fixtures aisladas en espacios de test propios, limpieza por su ID, sin tocar semillas
ni marcador. Primer error de delegación abstracta del spy corregido; ver bitácora.
JwtValidationTest valida claims/identidad; Nimbus configurado para firma, issuer,
tiempo, audiencia/subject. Providers/adaptador MSAL reales compilan, modo Basic sin
instancia Azure. No prueba de firma contra Azure ni tenant real. npm ls correcto y
npm audit 0 vulnerabilidades; alcance frontend/lockfile, no auditoría integral Java.
Angular build/test correctos. Siguiente H007: operación, prueba final y explicación.
