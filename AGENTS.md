# AGENTS.md — Booking de espacios físicos

## Alcance vigente — consolidación DEC-022/023

El repositorio actual es la base definitiva (DEC-004). Arquitectura humana DEC-005:
monolito por funcionalidades, con controlador/servicio/repositorio dentro de booking;
space, security y errors compartido; Angular acceso/reservas. No starter, migración
ni defecto equivalente. T014 conserva "No aplica por cambio de alcance", no implementada.
El usuario autoriza consolidar documentación/estudio y ajustes operativos mínimos para
Windows/Linux; no negocio, arquitectura, push ni publicación. T018 parcial, T021 escritura
Swagger pendiente y T015/WebLogic independiente. No ejecutar T023/T024.

README.md, AGENTS.md y BITACORA.md son los documentos principales. Esta instrucción
posterior migra la bitácora desde docs y retira la explicación extensa del checkout.
No mantener dos bitácoras activas. El historial original de extensiones se conserva en
Git; la consolidación no convierte propuestas del agente en decisiones del usuario.

## Requisitos originales de la prueba (texto conservado)

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


## Evolución de la especificación — hechos y decisiones

Las ampliaciones no sustituyen el stack original ni ocultan sus contradicciones.
Fechas históricas2026-09-14; desarrollo por etapas, descrito en BITACORA con commits.

| Etapa | Ampliación real |
|---|---|
| Infraestructura, antes de DEC-001 | Docker/WSL existentes; JDK21/Node22, OracleFree23.26.3, secretos ignorados, sondas JDBC/ngc. Aún no aplicación/WAR. |
| Transferencia31782c5 | Imágenes y estado Oracle cifrado; credenciales conservadas, clave separada, marcador leído tras restauración. |
| DEC-001/002, H001 | Specify0.8.1 existente; fusión selectiva de recursos, feature001-booking-espacios, tareas/handoffs. Sin copiar overlay encima. |
| DEC-003 | Propuesta de monolito simple frente a puertos/adaptadores; pendiente de decisión entonces. |
| DEC-004/005 | Usuario excluye starter y elige organización por funcionalidades; T015 no cambia. |
| DEC-006/007 | Oracle/JPA/WAR y autorización de completar T004–T013; SQL explícito, validate, sin H2. |
| DEC-008–013 | Basic+CSRF, dueño del principal, bloqueo por espacio, recurrencias parciales, Angular y WAR local; Azure preparado/inactivo. |
| DEC-014/015 | Reporte de fechas/espera, corrección UI; auditoría posterior descubre seis regresiones reales y avisos de dependencias. |
| DEC-016–021 | Usuario autoriza T018–T022 y mantiene stack: coerción/MVC/OpenAPI corregidos, Tomcat10.1.59 y eager headers, Dev Container/artefacto verificable. |
| DEC-022/023 | Consolidar entrega/estudio; operador Node/Docker portable, sin cambiar negocio ni cerrar pendientes por inferencia. |

## Decisiones implementadas y garantías que deben conservarse

- API/DTO, respuestas y Basic+CSRF en docs/CONTRATO_API.md. Instantes con offset;
  Oracle TIMESTAMP(9) WITH TIME ZONE, Hibernate NATIVE, ddl-auto=validate, sin create-drop.
- Intervalos semiabiertos, mismo espacio ACTIVE; PESSIMISTIC_WRITE sobre Espacio bajo
  READ_COMMITTED para crear/cancelar. Una transacción por pedido semanal1–12.
  Conflicto parcial es negocio; error técnico revierte todo. No confiar solo en UI.
- Propietario del principal, cancelación propia idempotente, ajena/ausente indistinguible;
  consultas propias activas cuyo fin no ha pasado. No añadir reglas ni rutas no pedidas.
- Basic en memoria del cliente; sesión solo CSRF. No desactivar CSRF para Swagger.
  JWT/MSAL preparados pero no identidad empresarial acreditada. No revelar secretos.
- Stack efectivo en README/POM/lock. DEC-019 mantiene familias obligatorias: alternativa
  Boot3.5/Security6.5/Data3.5 no aplicada. Tomcat local no resuelve WebLogic/Jakarta.
- Operación portable: node scripts/environment.mjs desde el host. Reutilizar .local,
  imágenes y volúmenes existentes; --no-build. prepare-new/images/schema solo instalación
  nueva explícita. No jdbc write para comprobar restauración ni down -v.

## Reglas de continuidad y herramientas

- Leer specs/001-booking-espacios/spec.md, plan.md, tasks.md y handoff pertinente.
  tasks.md es la única lista de estados; .handoffs mantiene contexto, DEC/T y próximo paso.
- Mantener .specify, .agents/skills y plantillas oficiales. No regenerar ni iniciar otra
  feature. docs/SPECKIT.md fija selector de sesión y versión; no reinstalar herramientas
  que funcionan. Las plantillas no anulan decisiones humanas ni AGENTS.
- No lanzar workflows, issues ni agentes paralelos por existir handoffs. Si una skill
  no puede limitarse al bloque autorizado, trabajar directamente con esos artefactos.
- Simplicidad: justificar capas/interfaces/dependencias por una necesidad actual;
  no prometer rendimiento sin medición. No usar H2/mocks como prueba de Oracle real.
- BITACORA.md conserva DEC consecutivos, origen humano/agente, alternativas reales,
  motivo y pruebas. Distinguir propuesta, decisión, implementación y verificación.
  Registros retrospectivos citan evidencia/fechas; no inventar tiempos, aprobaciones
  o desacuerdos. Los37minutos antiguos eran un reporte, no cronometraje auditable.
- Commits incrementales coherentes, sin amend/rebase/reset ni fechas ficticias. Revisar
  diff/secretos; pedir intervención solo por decisión material o riesgo de datos ajenos.
- No repetir pruebas funcionales por cambios solo documentales. Cambios operativos
  requieren probar el recorrido correspondiente y separar mocks de ejecución real.
- Los ejercicios de estudio son propuestas de análisis, no funcionalidades autorizadas.
  T013 conserva su evidencia histórica; ubicación externa de estudio por DEC-022.
- Fuentes/estado/verificaciones autocontenidos en README, BITACORA, docs/VERIFICACION.md
  y docs/TRATAMIENTO_DEPENDENCIAS_T018.md. Nunca exigir una carpeta personal de apoyo
  para instalar o evaluar el checkout. Respaldo documental previo y trazabilidad en Git.
