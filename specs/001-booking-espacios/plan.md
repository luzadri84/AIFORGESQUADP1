# Implementation Plan: Booking

**Feature**: 001-booking-espacios | **Git al inicio**: main, 5dcdb7c | **Date**: 2026-09-14
**Spec**: [spec.md](spec.md) | **Estado de decisiones**: [historia consolidada](../../BITACORA.md)

## Summary

Monolito local implementado bajo DEC-007, sobre la base definitiva DEC-004 y la
arquitectura humana DEC-005. API Spring Boot, UI Angular, seguridad y transacciones
Oracle verificadas; código y entorno previo conservados. Este plan se actualiza
contra la ejecución; la versión inicial y sus propuestas permanecen en Git/bitácora.

## Technical Context: configuración y versiones existentes

| Elemento | Versión/evidencia del repositorio restaurado |
|---|---|
| Host / shell | Windows 10, PowerShell 7.6.5; Spec Kit se ejecuta en host con scripts ps |
| Specify / Python de su instalación | 0.8.1 / 3.11.9; tag y commit en .specify/toolchain.json |
| Docker / Compose | Engine 29.8.0 Linux amd64 / Compose 5.5.1; Desktop 4.91.0 |
| Java / Maven | Temurin 21.0.10+7 / Maven 3.9.9, Wrapper 3.3.4 |
| Boot / Framework / Security | BOM 3.3.13 / 6.1.21 / 6.3.10 |
| Data JPA / Hibernate / SpringDoc | 3.3.13 / 6.5.3.Final / 2.6.0 |
| Oracle / JDBC | 23.26.3, BOOKING/FREEPDB1 / ojdbc11 23.26.3.0.0 (DEC-024) |
| Node / npm | 22.22.0 / 10.9.4 |
| Angular core/compiler / CLI / CDK | 20.3.31 / 20.3.37 / 20.2.0 |
| PrimeNG / Tailwind / RxJS | 20.0.0 / 3.4.17 / 7.8.2 |
| TypeScript / MSAL Angular / Browser | 5.9.2 strict / 3.1.0 / 3.28.1 |

Fuentes locales: [POM](../../infra/checks/pom.xml),
[lockfile](../../infra/checks/frontend/package-lock.json),
[Dockerfile](../../.devcontainer/Dockerfile), [verificación consolidada](../../docs/VERIFICACION.md)
y [historia consolidada](../../BITACORA.md). Las sondas conservan esas versiones;
[backend/pom.xml](../../backend/pom.xml) y [frontend/package-lock.json](../../frontend/package-lock.json)
resuelven la aplicación real con estas versiones. No reinstalar herramientas ni volver a Angular 20.3.0.

## Estructura implementada

- `backend/src/main/java/local/booking/booking`: controlador, servicio, DTO, entidad,
  repositorio y reglas de recurrencia/intervalos; `space`: catálogo y persistencia;
  `security`: Basic/CSRF y alternativa JWT; `errors`: tratamiento compartido real.
- `frontend/src/app/acceso` y `reservas`: componentes standalone, formularios tipados,
  servicios HTTP próximos, identidad en memoria y UI de resultado parcial.
- `backend/src/main/resources/db/oracle`: V001 explícito y seed insert-only, ya aplicados;
  Hibernate validate, sin create-drop. Dos tablas/dos secuencias, sin entidad Serie.
- `scripts/environment.mjs` (host Node), `app.ps1` (Windows) y `app-process.sh`: operación local con ambos Compose y --no-build.
  Se conservan Compose, devcontainer, wrapper, infra/checks y scripts anteriores.
- `.specify`, `.agents/skills`, `.handoffs`: integración H001 conservada, sin regeneración.

## Arquitectura y decisiones ejecutadas

DEC-005: monolito por funcionalidades; controlador → servicio transaccional → Spring
Data JPA dentro de booking. Alternativa de puertos/adaptadores documentada en DEC-003:
más interfaces/mapeos sin necesidad actual; no se atribuye un rechazo humano inexistente.

DEC-008/010: Basic por petición, contexto Security STATELESS y sesión exclusivamente
CSRF. El token enmascarado se solicita explícitamente, no se desactiva CSRF; interceptor
solo adjunta credenciales en /api/ propio, en memoria. La prueba HTTP descubrió rotación
de sesión tras GET paralelos y motivó la corrección descrita en bitácora.

DEC-009: propietario del principal, reserva ACTIVE/CANCELLED, listado propio con fin
posterior al reloj. OffsetDateTime/NATIVE y TIMESTAMP(9) WITH TIME ZONE. Colisión con
inicio < fin solicitado y fin > inicio solicitado, mismo espacio ACTIVE. Bloqueo
PESSIMISTIC_WRITE de Espacio antes del query bajo READ_COMMITTED; crear y cancelar
siguen el protocolo. No synchronized ni bloqueo exclusivo de reservas inexistentes.

DEC-011: occurrences 1–12 incluyendo primera, ausente=1, semanas America/Bogota.
Conflictos generan rechazos por fecha; una transacción conserva válidas y revierte
ante excepción técnica. No entidad Serie ni cron. 201 total/200 parcial/409 ninguna,
400 validación/401 identidad/403 CSRF/404 inaccesible/503 acceso a datos/500 inesperado.
[Contrato ejecutado](../../docs/CONTRATO_API.md), con DTO y errores sin datos ajenos.

DEC-012: MSAL Angular y Resource Server con adaptadores condicionados, compilables;
modo Basic inicia sin Azure. Se probaron claims, no tenant/JWKS/firma ni login empresarial.
DEC-013: WAR ejecutable local, scripts start/verify/stop/status y documentación real.

## Verificación y Constitution Check

La autorización DEC-007 concreta T004–T013 sin pausas entre handoffs. No altera la
arquitectura ni los requisitos; las elecciones técnicas siguen registradas como del
agente. Se conservan secretos, volumen, marcador original, historial e infraestructura.

Pruebas parametrizadas de intervalos y expansión, API MockMvc con Oracle, HTTP real
para sesión y carrera, rollback técnico tras flush y navegador para US1–US3.
No se usa H2 ni mocks como evidencia de persistencia. Fixtures transaccionales o
limpieza limitada a IDs creados por la propia prueba. Sin rendimiento medido.
Resultados reales en [verificación consolidada](../../docs/VERIFICACION.md); fuente de
estado única [tasks.md](tasks.md). T013 produjo una explicación con doce contenidos, quince preguntas y seis ejercicios.
DEC-022 migra el estudio fuera del repositorio; README/BITACORA/VERIFICACION conservan
la información evaluable y operativa. T025 registra la nueva entrega documental.

## Pendientes independientes

T014 **No aplica por cambio de alcance** (DEC-004): sin recepción, comparación,
migración ni investigación de defecto; no inventar sustituto. T015 conserva su
aceptación y bloqueo por aclaración externa del runtime. No bloquea la solución local.

La contradicción de Boot 3/WebLogic 12.2.1.4 quedó documentada en H001: Boot requiere
Servlet 5+ y WebLogic documenta Java EE 7. Fuentes oficiales consultadas entonces:
[Spring Boot](https://docs.spring.io/spring-boot/3.3/system-requirements.html) y
[Oracle](https://docs.oracle.com/en/middleware/fusion-middleware/weblogic-server/12.2.1.4/intro/compatibility.html).
La ejecución local usa Tomcat administrado por Boot dentro del WAR, autorizada en
DEC-007. No certifica ni sustituye la obligación de aclarar el runtime externo.
No despliegue público, push ni implementación de ejercicios de sustentación.

## Corrección posterior de T016 — DEC-014

Reporte del usuario: fechas iguales y botón esperando. Validación de rango en
FormGroup, aviso junto a Fin y bloqueo de envío inválido. BookingApi aplica timeout
RxJS de 15 segundos sin reintento automático; no implica rollback confirmado.
Tests con FormGroup real y servicio compilado, HTTP simulado/tiempo virtual;
navegador para igualdad, corrección, creación, conflicto y cancelación.
No se altera API/SQL/seguridad; no atribuir sin evidencia la espera a extensiones.

## Auditoría posterior — T017 / DEC-015

T017 no reinicia la implementación: registra diagnóstico y regresiones del estado47a6af4. Los pendientes nuevos están en tasks.md; no se ejecutan correcciones con esta autorización.

[verificación consolidada](../../docs/VERIFICACION.md) · [historia consolidada](../../BITACORA.md).

## Correcciones vigentes DEC-016–021

La autorización T018–T022 supera la etapa de auditoría sin cambios. Mantener familias
obligatorias por decisión humana DEC-019; Tomcat10.1.59 y mitigación de cabeceras,
sin aplicar alternativa Boot3.5/Security6.5/Data3.5. Arquitectura por funcionalidades
sin cambios. JSON solo tokens enteros (1.0/1e0/"1" se rechazan), MVC400/404/405 con
Allow, OpenAPI Basic AND CSRF por operación. Dev Container combina imágenes importadas
sin build; start detecta WAR obsoleto. Estado únicamente en tasks.md y evidencia en
docs/VERIFICACION.md; no T023/T024 ni atribuir WebLogic a Tomcat local.

## Consolidación vigente DEC-022/023 — T025

Documentación canónica README/AGENTS/BITACORA. Historias de H001–H007 y auditoría
se preservan en Git; sus enlaces llevan al resumen consolidado, no a una repetición
de los informes. Operador Node/Docker común; host Node>=18, Linux nuevo UID 1000.
No requiere pwsh en Linux ni cambia negocio. El estudio extenso es externo y no es
prerrequisito de operación. Pruebas y límites en docs/VERIFICACION.md (raíz del repo).
