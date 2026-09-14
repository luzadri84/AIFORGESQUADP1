# AGENTS.md — Especificación inicial del proyecto "Booking de Espacios Físicos"

## Rol de este archivo

Este archivo es la especificación inicial (spec) del proyecto, siguiendo la metodología Spec-Driven Development (SDD) que opera el AI Forge Squad. Tu agente de IA (Copilot, Claude u otro equivalente) debe leer este archivo como contexto antes de generar código.

Se espera que **extiendas y refines este archivo** a medida que tomas decisiones de diseño — esa extensión es evidencia evaluada dentro de la dimensión "Orquestación y verificación de IA".

## Contexto del proyecto

La Dirección de Construcciones y Conservación de la Secretaría de Educación del Distrito (SED) necesita un sistema centralizado para agendar salas, auditorios y espacios de co-working, evitando colisiones de agenda entre dependencias.

Historia de usuario principal (ver también el documento adjunto Historia_Usuario_Booking_Espacios):

> Como funcionario, quiero reservar un espacio físico verificando que no haya colisión de horario, y consultar mis reservas activas.

## Alcance funcional esperado (núcleo, ~4–6 h efectivas)

1. Backend con motor de detección de colisiones y API REST.
2. Frontend funcional mínimo para crear y consultar reservas.
3. Pruebas automatizadas del motor de colisiones (solapamientos parciales, mismo horario, recurrencias).
4. Autenticación local vía HTTP Basic con usuarios de desarrollo (simula el Directorio Activo; no se requiere integración real con Azure AD en este caso).

## Fuera de alcance (no construir; no resta puntos si se omite)

- Notificaciones por correo o push.
- Integración real con el Directorio Activo de la SED.
- Panel de administración de espacios (CRUD de salas más allá de datos semilla).
- Internacionalización / multi-idioma.

## Stack técnico

Stack obligatorio para este caso (refleja el stack real del squad; no se acepta sustituirlo por otras tecnologías):

**Frontend**
- Angular 20, componentes standalone, TypeScript en modo `strict` (sin relajar flags).
- PrimeNG 20 como librería de componentes UI.
- Tailwind CSS 3.4 para estilos utilitarios.
- RxJS 7.8 para manejo de flujos asíncronos y estado reactivo.
- Angular CDK 20 (overlays, drag-drop, accesibilidad, etc. según se requiera).
- MSAL Angular 3.x integrado en el cliente, aunque en el MVP la autenticación real quede detrás de HTTP Basic (ver más abajo) — deja el punto de integración preparado para Azure AD.

**Backend**
- Java 21 (LTS) como versión objetivo.
- Spring Boot 3.3.x (preferible la última 3.3.x) o 3.5.x — mínimo exigido para Java 21.
- Spring Framework 6.1.x / 6.2.x (gestionado automáticamente por Spring Boot).
- Spring Security 6.3.x o 6.4.x (gestionado) — ten en cuenta que es una migración importante desde 5.7 si usas referencias antiguas.
- Spring OAuth2 Resource Server (incluido en Spring Security 6.x, vía `spring-boot-starter-oauth2-resource-server`) — deja preparada la integración con Azure AD aunque el MVP use Basic Auth.
- Spring Data JPA 3.3.x (gestionado) — usa el namespace `jakarta.*` (no `javax.*`).
- Hibernate 6.5.x o 6.6.x (gestionado por Boot).
- SpringDoc OpenAPI 2.6.x o 2.8.x (`springdoc-openapi-starter-webmvc-ui`) para documentar la API.

**Datos e infraestructura**
- Base de datos: Oracle Database 19c o superior.
- Servidor objetivo de despliegue: Oracle WebLogic 12.2.1.4.
- Empaquetado del backend: WAR (no JAR ejecutable independiente).

**Autenticación**
- MVP / entrega del caso: autenticación local vía HTTP Basic con usuarios de desarrollo (simula el Directorio Activo; no se requiere integración real).
- Identidad futura (fuera de alcance de este caso, pero el diseño debe dejar el punto de extensión claro): Azure AD / Office 365, vía MSAL Angular en el cliente y Spring OAuth2 Resource Server en el backend.

Se evalúa el uso correcto y justificado de este stack (versiones, convenciones `jakarta.*`, `strict` en TypeScript, empaquetado WAR), no la elegancia de alternativas fuera de este set.

## Modelo de dominio sugerido (ajústalo si tu diseño lo requiere)

- **Espacio**: id, nombre, tipo (sala | auditorio | coworking), capacidad, sede.
- **Reserva**: id, espacioId, usuarioId, fechaInicio, fechaFin, estado, (opcional) reglaRecurrencia.
- **Usuario** (simulado): id, nombre, dependencia.

## Reglas de negocio críticas

- No pueden existir dos reservas activas para el mismo espacio con solapamiento de horario, incluyendo solapamientos parciales.
- Las reservas recurrentes deben validarse contra colisiones en cada ocurrencia generada, no solo en la primera.
- Un usuario solo puede consultar, modificar o cancelar sus propias reservas (autorización por recurso, no solo autenticación).

## Convenciones de ingeniería

- Commits incrementales que reflejen la construcción real (no un único commit final).
- Sin secretos ni credenciales en el código o el historial de git.
- Toda dependencia debe existir y ser real — verifica que no haya sido "alucinada" por la IA antes de instalarla.
- Documenta en el README las decisiones de arquitectura relevantes.
- Registra en la Bitácora de decisiones dónde difieres de lo que sugirió la IA, qué aceptaste, qué rechazaste, qué verificaste y por qué (no un simple listado de prompts).

## Cómo extender este archivo

Antes de programar, añade bajo cada sección las decisiones concretas que tomes dentro del stack obligatorio (estructura de carpetas, contrato de API, estrategia de pruebas, versiones exactas usadas). Esa extensión de la spec es evidencia de tu ingeniería de contexto y forma parte de lo evaluado.

## Entregables esperados

- Código fuente completo con historial de git.
- README.md con decisiones de arquitectura.
- Bitácora de decisiones (dónde difieres de la IA, qué aceptaste/rechazaste y por qué).
- Este archivo AGENTS.md, extendido con tus decisiones.
- Instrucciones claras de ejecución.

## Decisiones de infraestructura local — 2026-09-14

Esta especificación se recuperó del adjunto original. No se ha recibido el starter:
la carpeta inicial contenía solo documentos y no tenía historial Git.
La autorización actual se limita a infraestructura provisional, sin negocio ni publicación.

- Docker Desktop/WSL2 y VS Code Dev Containers existentes se reutilizan.
- Entorno dev con JDK 21.0.10+7, Node 22.22.0 e imágenes por digest; Maven Wrapper
  3.3.4 y Maven 3.9.9 verificado por checksum. Detalles: docs/ENTORNO_LOCAL.md.
- Oracle Free 23.26.3-slim por digest, servicio oracle:1521/FREEPDB1, schema BOOKING,
  secretos locales ignorados, healthcheck y volumen persistente. No exponer Oracle.
- Las dependencias exactas están en infra/checks: BOM Boot 3.3.13, SpringDoc 2.6.0,
  Angular 20.3.0, CDK 20.2.0, PrimeNG 20.0.0, RxJS 7.8.2, Tailwind 3.4.17,
  MSAL Angular 3.1.0 / Browser 3.28.1, TypeScript 5.9.2. No forzar peers ni relajar strict.
- Las sondas no constituyen la aplicación. packaging=war, arranque del backend,
  seguridad Basic/CSRF, pruebas de negocio y defecto sembrado quedan pendientes del starter.
  Su futura incorporación debe conservar requisitos e historial original.
- No desactivar CSRF, generar reservas ficticias, usar H2 ni afirmar compatibilidad
  Boot 3/WebLogic 12.2.1.4. La incompatibilidad del runtime sigue abierta.
- Comandos reales: pwsh -NoProfile -File scripts/local.ps1
  con acciones diagnose, prepare, up, verify, status y stop.
  Dentro de dev: bash scripts/verify-local.sh.
- Consultar docs/VERIFICACION_LOCAL.md para resultados; no inferir éxito de la configuración.
  El historial nuevo solo registra trabajo ocurrido aquí.
