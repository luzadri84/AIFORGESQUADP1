# AGENTS.md — Especificación inicial del proyecto "Booking de Espacios Físicos"

## Alcance vigente — DEC-004, 2026-09-14

Por instrucción explícita del usuario, este repositorio y su entorno local son la
base definitiva. El starter no llegará y queda fuera del alcance: no recibir,
comparar, migrar ni investigar su supuesto defecto; no inventar uno equivalente.
T014 conserva su descripción como **No aplica por cambio de alcance**, no implementada.
Los registros anteriores se conservan como historia; sus dependencias del starter y
el límite temporal de ejecutar solo H001 quedaron superados por esta instrucción.
El usuario autoriza continuar con la siguiente tarea pendiente, T003, sin reiniciar
Spec Kit ni repetir trabajo verificado. DEC-005 registra la decisión humana de
monolito por funcionalidades: booking, space, security y errors solo si se comparte;
controlador–servicio–repositorio dentro de booking. Angular: acceso y reservas.
T015/WebLogic permanece sin cambios y no condiciona trabajos locales independientes.

Mantener Spec Kit, .handoffs, tasks.md como único estado, commits incrementales y
la bitácora real. docs/EXPLICACION_IMPLEMENTACION.md describe únicamente código y
pruebas existentes, con preguntas respondidas y ejercicios de análisis, nunca
funcionalidades extra ejecutadas para resolver esos ejercicios.

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

## Decisiones de infraestructura local — 2026-09-14 (registro histórico; alcance actualizado por DEC-004)

Esta especificación se recuperó del adjunto original. No se ha recibido el starter:
la carpeta inicial contenía solo documentos y no tenía historial Git.
La autorización actual se limita a infraestructura provisional, sin negocio ni publicación.

- Docker Desktop/WSL2 y VS Code Dev Containers existentes se reutilizan.
- Entorno dev con JDK 21.0.10+7, Node 22.22.0 e imágenes por digest; Maven Wrapper
  3.3.4 y Maven 3.9.9 verificado por checksum. Detalles: docs/ENTORNO_LOCAL.md.
- Oracle Free 23.26.3-slim por digest, servicio oracle:1521/FREEPDB1, schema BOOKING,
  secretos locales ignorados, healthcheck y volumen persistente. No exponer Oracle.
- Las dependencias exactas están en infra/checks: BOM Boot 3.3.13, SpringDoc 2.6.0,
  Angular core/compiler 20.3.31 y CLI 20.3.37, CDK 20.2.0, PrimeNG 20.0.0, RxJS 7.8.2, Tailwind 3.4.17,
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

## Transferencia solicitada — 2026-09-14

El usuario autorizo preparar la subida del historial a luzadri84/AIFORGESQUADP1
y exportar las imagenes para otro Windows 10. Esta instruccion posterior amplia
el alcance a respaldo/transferencia del repositorio; no autoriza un despliegue web.
No incluir .env, secretos, correo, volumenes o archivos de imagen en Git.
El estado real de subida, visibilidad y autenticacion debe verificarse antes de
confirmar que GitHub ya contiene los cambios.


## Integración SDD y reglas vigentes — H001, 2026-09-14

Esta ampliación registra las reglas de H001. Su límite temporal H001/T001–T002 y
la dependencia del starter fueron superados por DEC-004; no autoriza publicación. Requisitos
del evaluador e instrucciones humanas expresas prevalecen sobre ejemplos de skills,
plantillas y documentos propuestos. La propuesta DEC-003 fue concretada por la
decisión humana DEC-005: organización por funcionalidades.

- Feature única: `specs/001-booking-espacios`. Leer spec.md, plan.md, tasks.md y el
  handoff solicitado antes de trabajar. tasks.md es la única lista de tareas/estados;
  `.handoffs/` conserva objetivo, contexto, evidencia, T/DEC y siguiente paso.
- Specify 0.8.1 ya funciona; configuración y comandos en docs/SPECKIT.md. Integración
  Codex en `.agents/skills/`. Desde main usar el selector de sesión soportado
  `SPECIFY_FEATURE=001-booking-espacios` antes de los scripts oficiales; no crear otra
  feature ni sobrescribir spec/plan/tasks con generadores. No renumerar IDs existentes.
- Limitar skills al bloque autorizado. H001 se ejecutó con instrucciones directas;
  no lanzar speckit-implement sobre todo el backlog. Si una skill no permite limitar
  tareas, usar instrucciones directas con los artefactos. No workflows, agentes
  paralelos, issues ni automatización por la mera existencia de handoffs.
- Reutilizar entorno, código y configuración. En este equipo restaurado usar ambos
  Compose con imágenes importadas y `--no-build` según README. No invocar local.ps1
  up (reconstruye) ni verify (escribe otro marcador) como comprobación rutinaria de H001.
  Conservar credenciales/volúmenes y leer el marcador original con jdbc-check.sh read.
- La directriz de simplicidad exige justificar cada interfaz, capa o dependencia
  por un problema actual; comparar con una solución menor. Conservar validación del
  servidor, propiedad, CSRF, concurrencia y pruebas críticas. No inferir garantías
  de rendimiento de un diseño; no usar H2 o mocks como evidencia de Oracle real.
- Mantener la bitácora existente `docs/BITACORA.md`, sin crear otra. Conservar entradas
  previas; usar DEC-001 y siguientes para decisiones nuevas sin renumerar historia.
  Registrar problema/requisito, origen de propuesta, alternativa cuando sea útil,
  intervención humana real o su ausencia, decisor, motivos/compensaciones y vínculos T.
- Separar aceptación de decisión, implementación y verificación. Identificar pruebas
  del agente como tales. Una propuesta del plan previo no es aprobación humana;
  el silencio tampoco. No inventar discusiones, rechazos, defectos, métricas o pruebas.
- Registrar fecha/zona de la entrada y del hecho si difieren. Las reconstrucciones
  retrospectivas citan Git/log/mensaje y límites; una prueba actual no certifica cuándo
  se hizo una anterior. No repetir pruebas para fabricar historia de los 37 minutos.
- Durante el trabajo registrar fallos y correcciones pertinentes; al cambiar decisión
  conservar el motivo anterior y enlazar la nueva. Evitar logs masivos y secretos.
- Verificar criterios antes de marcar tareas; probar solo lo pertinente al riesgo.
  Al cerrar/interrumpir: actualizar tasks, evidencia/handoff y DEC, preservar diff y
  siguiente paso. No reset/stash/borrado automático ni reaplicar trabajo ya terminado.
- Crear commits coherentes durante el trabajo, con T/DEC pertinentes; revisar diff y
  secretos. No amend/rebase/fechas ficticias ni partición retrospectiva artificial.
  Vincular por DEC en mensaje; incluir hashes solo después de leerlos de Git real.
- T013/H007 exige los doce apartados, al menos doce preguntas respondidas y seis retos
  analizados de docs/ENTREGA_Y_STARTER.md en docs/EXPLICACION_IMPLEMENTACION.md contra
  código real. No completar ahora un documento de implementación inexistente ni
  implementar los retos como alcance extra.
- T014/H008 no se ejecuta: No aplica por cambio de alcance (DEC-004). Sus nueve
  pasos se conservan como procedimiento histórico sin vigencia.
  T015/WebLogic es independiente. No bloquear trabajo independiente ya autorizado,
  ni declarar cumplimiento completo sin resolver obligaciones externas.
- Pedir intervención solo para decisiones materiales no resueltas o alcance nuevo;
  continuar lo rutinario autorizado. Presentar recomendación y alternativa sin
  atribuir al usuario elecciones autónomas del agente. La siguiente fase requiere
  la decisión de arquitectura prevista en Prompt A, ya recibida en DEC-005. La
  continuación a T003 fue autorizada por el cambio de alcance DEC-004.
