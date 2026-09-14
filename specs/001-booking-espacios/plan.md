# Implementation Plan: Booking

**Feature**: 001-booking-espacios | **Git al inicio**: main, 5dcdb7c | **Date**: 2026-09-14
**Spec**: [spec.md](spec.md) | **Estado de decisiones**: [DEC-003/DEC-005](../../docs/BITACORA.md)

## Summary

Conservar infraestructura comprobada y proponer un monolito pequeño con API Spring
Boot y UI Angular. H001 solo integra metodología; no existe todavía aplicación.
Continuación a T003 autorizada por DEC-004; arquitectura elegida por el usuario
en DEC-005: monolito organizado por funcionalidades. Base actual definitiva.

## Technical Context: configuración y versiones existentes

| Elemento | Versión/evidencia del repositorio restaurado |
|---|---|
| Host / shell | Windows 10, PowerShell 7.6.5; Spec Kit se ejecuta en host con scripts ps |
| Specify / Python de su instalación | 0.8.1 / 3.11.9; tag y commit en .specify/toolchain.json |
| Docker / Compose | Engine 29.8.0 Linux amd64 / Compose 5.5.1; Desktop 4.91.0 |
| Java / Maven | Temurin 21.0.10+7 / Maven 3.9.9, Wrapper 3.3.4 |
| Boot / Framework / Security | BOM 3.3.13 / 6.1.21 / 6.3.10 |
| Data JPA / Hibernate / SpringDoc | 3.3.13 / 6.5.3.Final / 2.6.0 |
| Oracle / JDBC | 23.26.3, BOOKING/FREEPDB1 / ojdbc11 21.9.0.0 |
| Node / npm | 22.22.0 / 10.9.4 |
| Angular core/compiler / CLI / CDK | 20.3.31 / 20.3.37 / 20.2.0 |
| PrimeNG / Tailwind / RxJS | 20.0.0 / 3.4.17 / 7.8.2 |
| TypeScript / MSAL Angular / Browser | 5.9.2 strict / 3.1.0 / 3.28.1 |

Fuentes locales: [POM](../../infra/checks/pom.xml),
[lockfile](../../infra/checks/frontend/package-lock.json),
[Dockerfile](../../.devcontainer/Dockerfile), [verificación previa](../../docs/VERIFICACION_LOCAL.md)
y [evidencia H001](../../docs/INTEGRACION_H001.md). Son versiones de sondas,
no dependencias de una aplicación inexistente. No reinstalar ni volver a Angular 20.3.0.

## Project Structure: real y conservada

```text
.devcontainer/                 imagen dev y configuración existentes
compose.yaml                   Oracle y dev; usar override importado según README
infra/checks/pom.xml            resolución de dependencias; packaging=pom
infra/checks/JdbcCheck.java     conexión y marcador técnico (check/read/write)
infra/checks/frontend/          sonda Angular strict, no UI Booking
infra/templates/               ejemplos de properties y proxy
scripts/                       operación, transferencia y verificaciones existentes
.mvn/ y mvnw                   wrapper existente
.specify/ y .agents/skills/     metodología integrada en H001
specs/001-booking-espacios/     spec, plan, tasks y trazabilidad
.handoffs/                      continuidad, sin motor de ejecución
docs/BITACORA.md                bitácora original ampliada, no duplicada
```

No existen `backend/` ni `frontend/` de aplicación. Estas rutas son propuestas para
H002/H004: backend Maven WAR con paquetes `booking`, `space`, `security`; frontend
Angular standalone. `infra/checks` seguirá siendo infraestructura y no se renombra
para aparentar funcionalidad. No se generó ningún scaffold en H001.

## Arquitectura elegida (DEC-005; propuesta previa DEC-003)

Un solo backend por funcionalidades: `booking` reúne controlador, servicio,
repositorio, DTO, entidad y reglas de reservas; `space` reúne consulta, entidad y
persistencia; `security` configura autenticación/adaptación de identidad; `errors`
solo existe si hay errores HTTP realmente compartidos. Dentro de booking: controlador
HTTP → servicio transaccional → repositorio Spring Data JPA, con funciones pequeñas
de intervalos/recurrencias. Angular organiza `acceso` y `reservas` con componentes,
modelos y servicios HTTP próximos. No se crean carpetas vacías para aparentar capas. Configuración
por entorno, SQL versionado y tests junto a cada comportamiento. El código existente
no exige refactor: se conserva completo.

Alternativa razonable: separar dominio puro y persistencia mediante puertos y
adaptadores en el mismo monolito. Facilita aislamiento de dominio si futuras necesidades reales
lo justifican; añade interfaces, mapeos y más pruebas de integración. Con una sola
base Oracle y este alcance pequeño no hay evidencia actual que compense ese coste.
La elección humana DEC-005 favorece organización por funcionalidades, no una
infraestructura distinta. No se inventa un rechazo de puertos/adaptadores.
Ambas opciones deben conservar propiedad, CSRF, concurrencia y pruebas en Oracle.

## Propuesta técnica para la revisión humana y siguientes tareas

- Datos: Espacio semilla y Reserva por ocurrencia, ACTIVE/CANCELLED, propietario
  del principal. Fechas ISO 8601 con offset; comparar instantes y mostrar Bogotá.
  Probar round trip temporal Oracle antes de confiar en el mapeo.
- Colisión: `existente.inicio < solicitado.fin AND existente.fin > solicitado.inicio`,
  mismo espacio/ACTIVE. Adyacencias permitidas. Bloquear la fila Espacio con
  PESSIMISTIC_WRITE antes de consultar bajo READ_COMMITTED; todas las escrituras
  siguen el mismo protocolo. No bloquear solo reservas (pueden no existir) ni usar
  sincronización local Java para acreditar protección entre instancias.
- Recurrencias: semanal, count 1–12 incluyendo primera fecha; conflictos por
  ocurrencia como resultado de negocio; conservar válidas en una transacción y
  revertir el pedido ante error técnico. Límite y semántica parcial pendientes de
  confirmación; validar contra reservas y ocurrencias del mismo pedido.
- API propuesta: GET `/api/csrf`, `/api/me`, `/api/spaces`; POST/GET `/api/bookings`;
  DELETE `/api/bookings/{id}`. Usuario/estado no aceptados del body. Cancelación
  propia idempotente; ajena/ausente 404. 201 creación total, 200 parcial, 409 ninguna
  por conflicto; 400 validación, 401 identidad, 403 CSRF, 503 temporal identificado.
  Respuestas ProblemDetail y rechazos sin datos ajenos. Contrato aún sin ejecutar.
- Seguridad: Basic/PasswordEncoder y dos identidades externas; CSRF SPA real,
  interceptor limitado a API propia, credencial solo en memoria. Modo Basic inicia
  sin tenant/llamadas Azure. MSAL/Resource Server con adaptadores condicionados y
  compilables en T012, no acreditados por haber resuelto dependencias.
- UI: formulario, lista propia, cancelación, resultado parcial, loading/error/vacío;
  formularios tipados y RxJS. No reintentar automáticamente POST tras un fallo de red.

## Testing y criterios

Unitarias de intervalos y expansión; API de autenticación/CSRF/propiedad; integración
Oracle para carrera, rollback, tiempo y cancelación; navegador real para US1–US3.
No usar H2/mocks para acreditar Oracle/concurrencia. Pruebas con datos aislados:
conservar el marcador original y no eliminar volúmenes para limpiar fixtures.
Metas de rendimiento no fijadas ni medidas. Alcance: MVP local, sin despliegue público.
Los criterios de entrega/estudio y el procedimiento histórico del starter están en [ENTREGA_Y_STARTER.md](../../docs/ENTREGA_Y_STARTER.md).

## Constitution Check

H001 conserva stack, datos, código, historial y autorización limitada. Los guards
funcionales anteriores están planificados y siguen sin comprobarse. Una arquitectura
recomendada no satisface FR; consultar [tasks.md](tasks.md) como único estado.
No hay investigación/modelo/contratos generados artificialmente para dar por hecho H002.

## Dependencias y siguiente paso

T003 es la primera tarea funcional y está autorizada por DEC-004 con la arquitectura
DEC-005: preparar entidades/SQL/semillas sobre la base
existente y verificar el mapeo temporal. T004/T005 completan seguridad y contrato.
La orden actual permite continuar a T003 sin volver a integrar H001.

Starter excluido por DEC-004: T014 No aplica por cambio de alcance; no habrá
recepción, comparación, migración ni investigación de un defecto sembrado.
La aclaración WebLogic sigue pendiente exclusivamente en T015. Spring Boot 3.3 requiere
Servlet 5+; WebLogic 12.2.1.4 documenta Java EE 7. La incompatibilidad sigue abierta,
no se soluciona por cambiar a WAR. Fuentes oficiales consultadas en H001:
[Spring Boot](https://docs.spring.io/spring-boot/3.3/system-requirements.html) y
[Oracle](https://docs.oracle.com/en/middleware/fusion-middleware/weblogic-server/12.2.1.4/intro/compatibility.html).
Tomcat 10.1 es una posible prueba local propuesta, no una sustitución aprobada del
runtime objetivo. No se instala ni despliega en H001.
