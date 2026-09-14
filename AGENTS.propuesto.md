# AGENTS.md — Especificación inicial del proyecto "Booking de Espacios Físicos"

## Rol de este archivo

### Refinamiento propuesto

Estado de esta copia: propuesta de refinamiento antes de recibir e inspeccionar el starter. No acredita implementación, pruebas ejecutadas ni decisiones humanas ya aceptadas. Se conservan los requisitos originales. Incorporar las decisiones aceptadas al AGENTS.md real, bajo sus secciones, y actualizar lo que cambie. No usar esta copia para ocultar una contradicción del stack.

### Especificación recibida


Este archivo es la especificación inicial (spec) del proyecto, siguiendo la metodología Spec-Driven Development (SDD) que opera el AI Forge Squad. Tu agente de IA (Copilot, Claude u otro equivalente) debe leer este archivo como contexto antes de generar código.

Se espera que **extiendas y refines este archivo** a medida que tomas decisiones de diseño — esa extensión es evidencia evaluada dentro de la dimensión "Orquestación y verificación de IA".

## Contexto del proyecto

### Refinamiento propuesto

Resolver el flujo completo crear, consultar y cancelar reservas propias con una aplicación pequeña. La IA se usa durante el desarrollo, no como dependencia de ejecución del Booking. Contrastar los adjuntos con el starter antes de generar código.

### Especificación recibida


La Dirección de Construcciones y Conservación de la Secretaría de Educación del Distrito (SED) necesita un sistema centralizado para agendar salas, auditorios y espacios de co-working, evitando colisiones de agenda entre dependencias.

Historia de usuario principal (ver también el documento adjunto Historia_Usuario_Booking_Espacios):

> Como funcionario, quiero reservar un espacio físico verificando que no haya colisión de horario, y consultar mis reservas activas.

## Alcance funcional esperado (núcleo, ~4–6 h efectivas)

### Refinamiento propuesto

Implementar reserva individual y repetición semanal finita; aceptar ocurrencias válidas y señalar las que colisionan. La cancelación afecta una ocurrencia propia. No agregar edición salvo que el starter la exponga; si existe, asegurar su propiedad y colisiones o documentar su retiro. Inspeccionar el defecto sembrado y demostrar su corrección. El enlace al starter no estaba presente en el correo recibido.

### Especificación recibida


1. Backend con motor de detección de colisiones y API REST.
2. Frontend funcional mínimo para crear y consultar reservas.
3. Pruebas automatizadas del motor de colisiones (solapamientos parciales, mismo horario, recurrencias).
4. Autenticación local vía HTTP Basic con usuarios de desarrollo (simula el Directorio Activo; no se requiere integración real con Azure AD en este caso).

## Fuera de alcance (no construir; no resta puntos si se omite)

### Refinamiento propuesto

También excluir calendario drag and drop, RRULE completo, cancelación por serie, chatbot, microservicios, colas y paneles adicionales. No introducir funcionalidades para demostrar uso de una biblioteca. No implementar OAuth real ni un sistema de autenticación propio con JWT.

### Especificación recibida


- Notificaciones por correo o push.
- Integración real con el Directorio Activo de la SED.
- Panel de administración de espacios (CRUD de salas más allá de datos semilla).
- Internacionalización / multi-idioma.

## Stack técnico

### Refinamiento propuesto

Base candidata: Java 21, Boot 3.3.13 con BOM sin overrides de transitivas, SpringDoc 2.6.0 y WAR. Frontend candidato: Angular y CLI 20.3.0, CDK 20.2.0, PrimeNG 20.0.0, Tailwind 3.4.17, RxJS 7.8.2, TypeScript 5.9.2, MSAL Angular 3.1.0 y MSAL Browser 3.28.1. Mantener strict y strictTemplates. Preferir parches compatibles del starter tras revisión. Fijar versión exacta de Node 22 compatible e imagen Oracle 19c o superior al inspeccionar el entorno. Comprobar driver Oracle con Java 21, instalar desde lockfile y registrar versiones realmente resueltas antes de programar. No afirmar que estas candidatas ya compilan juntas.

Boot 3 exige Servlet 5 o superior y WebLogic 12.2.1.4 es Java EE 7: despliegue directo incompatible. Mantener WAR y consultar el runtime de demostración; Tomcat 10.1 sería una alternativa pendiente de aceptación, no cumplimiento de WebLogic. Boot 3.5 administra Security 6.5, distinto del rango escrito en la spec; no forzar una versión inferior. MSAL Angular 3 documenta Angular 15–18: probar integración compilable con Angular 20 y declarar el límite de soporte. Fuentes y tratamiento detallados en el plan técnico.

### Especificación recibida


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

### Refinamiento propuesto

Una fila Reserva por ocurrencia, con estado ACTIVE o CANCELLED. Usuario es identidad estable del principal; no crear gestión de usuarios. API ISO 8601 con offset obligatorio, comparación por instante y recurrencia/presentación en America/Bogota. Probar mapeo explícito a Oracle TIMESTAMP WITH TIME ZONE. No persistir regla ni entidad Serie si no se necesita. Mis activas significa estado ACTIVE y fin posterior al reloj actual, incluyendo reservas en curso.

### Especificación recibida


- **Espacio**: id, nombre, tipo (sala | auditorio | coworking), capacidad, sede.
- **Reserva**: id, espacioId, usuarioId, fechaInicio, fechaFin, estado, (opcional) reglaRecurrencia.
- **Usuario** (simulado): id, nombre, dependencia.

## Reglas de negocio críticas

### Refinamiento propuesto

Intervalos [inicio, fin): hay colisión si inicioExistente < finSolicitado y finExistente > inicioSolicitado, con mismo espacio y estado ACTIVE. Adyacencias permitidas. Validar inicio < fin. Recurrencia WEEKLY con count de 1 a 12 incluyendo primera fecha; el límite es una decisión técnica propuesta. Validar todas las ocurrencias y posibles colisiones internas. Crear válidas y devolver resultados por ocurrencia; un error técnico revierte el pedido completo.

Bloquear la fila de Espacio con PESSIMISTIC_WRITE dentro de la transacción antes de consultar colisiones y mantener hasta commit. Todas las escrituras siguen ese protocolo. No sustituirlo por synchronized ni por bloquear solo reservas existentes. Propietario siempre desde principal. Cancelación lógica por id y propietario, 404 para ajena/inexistente y 204 idempotente para propia ya cancelada. No exponer datos de reservas ajenas en errores.

### Especificación recibida


- No pueden existir dos reservas activas para el mismo espacio con solapamiento de horario, incluyendo solapamientos parciales.
- Las reservas recurrentes deben validarse contra colisiones en cada ocurrencia generada, no solo en la primera.
- Un usuario solo puede consultar, modificar o cancelar sus propias reservas (autorización por recurso, no solo autenticación).

## Convenciones de ingeniería

### Refinamiento propuesto

Monolito: backend y frontend dentro del starter. Paquetes por booking, space y security; controlador HTTP, servicio transaccional, repositorios JPA, DTO explícitos y funciones pequeñas para tiempo y recurrencia. Evitar interfaces sin necesidad, repositorios genéricos y capas repetidas. Constructor injection; no serializar entidades JPA como contrato.

Contrato propuesto: GET /api/csrf, GET /api/me, GET /api/spaces, POST y GET /api/bookings, DELETE /api/bookings/{id}. POST devuelve 201 si todas se crean, 200 parcial, 409 ninguna por colisión. Errores de validación 400, autenticación 401, CSRF 403, recurso ausente/ajeno 404; error temporal identificado 503. Para escrituras sin CSRF el filtro puede responder 403 antes de autenticación. No aceptar propietario o estado desde el DTO.

Basic con credenciales externas, sin valores predeterminados versionados; contraseñas efímeras en pruebas de Basic real. Credencial cliente solo en memoria e interceptor limitado a API propia. Mantener CSRF con token cookie/cabecera y configuración SPA verificada para la versión usada. Proxy local /api para mismo origen. No desactivar CSRF por usar stateless. Adaptadores MSAL y Resource Server compilables y condicionados; modo Basic inicia sin Azure.

Pruebas: JUnit para rangos y expansión; API para validación, Basic, CSRF y autorización por propietario; integración Oracle para concurrencia, rollback y persistencia temporal; recorrido Angular completo. Una prueba simultánea debe empezar sin reservas y usar dos transacciones independientes. No usar H2 para acreditar Oracle ni mocks para acreditar concurrencia. Priorizar pruebas de riesgos sobre getters y porcentajes arbitrarios.

Después de inspeccionar el starter, registrar aquí los comandos reales de arranque, unitarias, integración Oracle, frontend y WAR. mvnw verify y npm ci/build son candidatos, no ejecuciones acreditadas. Verificar que los tests IT se ejecutan en el ciclo. Registrar evidencia, fallos y bloqueos con honestidad; nunca omitir tests para conseguir un resultado verde.

### Especificación recibida


- Commits incrementales que reflejen la construcción real (no un único commit final).
- Sin secretos ni credenciales en el código o el historial de git.
- Toda dependencia debe existir y ser real — verifica que no haya sido "alucinada" por la IA antes de instalarla.
- Documenta en el README las decisiones de arquitectura relevantes.
- Registra en la Bitácora de decisiones dónde difieres de lo que sugirió la IA, qué aceptaste, qué rechazaste, qué verificaste y por qué (no un simple listado de prompts).

## Cómo extender este archivo

### Refinamiento propuesto

Mantener el texto inicial y añadir decisiones concretas en cada sección. Los detalles explicativos extensos van en docs, con una única definición consistente de contrato y reglas. Cada cambio de criterio actualiza spec, prueba y documentación. La bitácora distingue propuesta de IA, juicio humano confirmado, evidencia ejecutada y pendientes. No inventar rechazos ni defectos. Commits durante el trabajo real, sin reescritura ficticia de historia.

### Especificación recibida


Antes de programar, añade bajo cada sección las decisiones concretas que tomes dentro del stack obligatorio (estructura de carpetas, contrato de API, estrategia de pruebas, versiones exactas usadas). Esa extensión de la spec es evidencia de tu ingeniería de contexto y forma parte de lo evaluado.

## Entregables esperados

### Refinamiento propuesto

Añadir evidencia de criterios, defecto sembrado y limitaciones de compatibilidad al README/bitácora. Demostrar ejecución devcontainer con Oracle y WAR en runtime acordado. No presentar WebLogic ni Azure real como validados si no se probaron. Entregar repo privado o ZIP autocontenido con historial .git y sin secretos; no usar git archive como único ZIP de entrega. Publicación, invitaciones y envío corresponden a una instrucción expresa de la candidata.

### Especificación recibida


- Código fuente completo con historial de git.
- README.md con decisiones de arquitectura.
- Bitácora de decisiones (dónde difieres de la IA, qué aceptaste/rechazaste y por qué).
- Este archivo AGENTS.md, extendido con tus decisiones.
- Instrucciones claras de ejecución.
