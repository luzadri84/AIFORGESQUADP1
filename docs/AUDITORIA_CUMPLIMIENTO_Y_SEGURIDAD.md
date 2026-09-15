# Auditoría de cumplimiento y seguridad — T017 / DEC-015

**Dictamen:** la solución local realiza los flujos principales, pero no está lista para certificar cumplimiento integral. Hay validación numérica incorrecta, errores HTTP mal clasificados y OpenAPI incompleto. Los controles de identidad, propiedad, CSRF y concurrencia pasaron las comprobaciones descritas. La seguridad queda condicionada a resolver los avisos de dependencias aplicables; no se certifica ausencia de vulnerabilidades. La ejecución local del WAR está comprobada; WebLogic y un arranque completo por Dev Container no están acreditados.

## Revisión, autorización y límites

- Fecha: 2026-09-14, America/Bogota. Revisor: Codex, pruebas ejecutadas por el agente.
- Repositorio: `C:\PruebaAIFORGESQUAD`, rama `main`, **HEAD auditado `47a6af4dbd497e0b2eb4aa6e4ec757032fb283e8`**, árbol inicial limpio. 18 commits accesibles, repositorio no superficial. El commit de auditoría es posterior y se identifica en Git por `T017 / DEC-015`; no confundirlo con la revisión auditada.
- Origen: instrucción adjunta del usuario «Actúa como revisor técnico…». Autoriza inspección, pruebas e informes; prohíbe corregir producción/dependencias/configuración en esta fase. No se hicieron push, publicación, rotación, borrado de volúmenes ni reescritura de historia.
- Diferencia introducida: pruebas de auditoría, herramientas offline e informes/trazabilidad. Sin cambios en `backend/src/main`, frontend, POM, lockfiles, Compose ni configuración de ejecución. Las seis regresiones fallidas se conservan; no se excluyen del build.
- Starter y defecto sembrado: fuera de alcance por DEC-004. Ningún hallazgo de este informe representa aquel defecto.
- Fixtures: transacciones rollback o espacios propios eliminados por ID. UI creó #204–207; reproducción HTTP decimal creó #208–209, conservadas CANCELLED. Espacio inocuo #170 retirado exclusivamente tras verificar que no tenía reservas. No se cancelaron reservas preexistentes. Las cancelaciones se contrastaron en [SQL final](audit-evidence/final-fixtures.txt). Credenciales originales y marcador no se sustituyeron.

## Fuentes y precedencia

A = requisito original; B = decisión humana posterior explícita; C = concreción técnica del proyecto; D = recomendación adicional de esta auditoría. Un estado de C no reduce una obligación A. Las notas del agente no se presentan como aprobaciones individuales del usuario.

Se leyeron `C:\PruebaAIFORGESQUAD-apoyo\AGENTS.md`, su copia `referencias/AGENTS_original_de_la_prueba.md` y `Historia_Usuario_Booking_Espacios.docx` (texto extraído del OOXML). Se buscaron variantes AGENTS(1)/Historia…(1); se usaron los originales localizados sin sufijo. **El correo original no se localizó entre los materiales pertinentes**, por lo que sus condiciones adicionales no se consideran verificadas. Un registro histórico que diga que se leyó antes no sustituye su consulta en esta auditoría.

Se contrastaron [AGENTS vigente](../AGENTS.md), [constitución](../.specify/memory/constitution.md), [spec](../specs/001-booking-espacios/spec.md), [plan](../specs/001-booking-espacios/plan.md), [tasks](../specs/001-booking-espacios/tasks.md), [README](../README.md), [bitácora](BITACORA.md), [.handoffs](../.handoffs/README.md), [contrato](CONTRATO_API.md), [explicación](EXPLICACION_IMPLEMENTACION.md) e informes anteriores. Estos últimos sirven de contexto histórico; los resultados de abajo provienen de ejecuciones e inspecciones actuales.

B relevantes: DEC-004 base definitiva, DEC-005 monolito por funcionalidades, DEC-007 continuación de solución local. C relevantes: DEC-008/010 Basic/CSRF, DEC-009 transacciones/propiedad, DEC-011 recurrencia, DEC-012 identidad preparada, DEC-013 operación, DEC-014 validación visual. La aceptación parcial está ya en la historia original; semanas 1–12, estados HTTP y significado de activa son concreciones C.

## Evidencia y reproducción

Los comandos se ejecutaron desde la raíz, usando siempre ambos Compose y las imágenes importadas. Logs de trabajo ignorados en `.local/audit`; extractos redactados versionados en [executions.txt](audit-evidence/executions.txt), [inventario Maven](audit-evidence/maven-tree.txt), [historial/secretos](audit-evidence/secrets-history.json), [imágenes/puertos](audit-evidence/images-check.json) y [coerción decimal en servidor real](audit-evidence/decimal-live.json). [Procedimiento reproducible](../scripts/audit/README.md).

| Ejecución | Esperado y observado | Resultado |
|---|---|---|
| Maven `verify`, Oracle real | 50 pruebas existentes pasan; AuditSecurityTest añade 25 casos, 19 pasan y 6 fallan | 75 ejecutadas, 6 fallos, 0 errores/omitidas; exit 1 |
| OracleConcurrencyTest, nueva espera acotada | lock JDBC independiente; HTTP de serie espera, devuelve 503 en 8–14 s, cero filas | 1/1 adicional; exit 0 |
| Frontend `npm test` | ngc y pruebas del código real, incluidas fechas y timeout virtual | 13/13; exit 0 |
| `npm run build` | Compilación Angular strict y bundle | Correcto; 735.01 kB inicial sin comprimir, no es benchmark |
| Maven dependency:tree / perfiles; npm ls --all | Dependencias transitivas resueltas, sin perfiles Maven adicionales ni errores npm | 112 coordenadas Maven; 446 pares npm instalados; lock 560 entradas |
| Catálogos completos OSV, cruce offline | No enviar inventario privado; conservar test/dev/provided | 65 coincidencias Maven; 0 npm; no equivalen a explotación |
| Navegador US1–US3 | Creación, recurrencia parcial, conflicto total, listado propio, cancelación | Correcto; detalle abajo |
| Semillas `booking-db.ps1 seed` dos veces | No reemplazar catálogo existente | 0 filas afectadas en cada ejecución |
| JDBC read / Compose ps | BOOKING/FREEPDB1, marcador original, Oracle healthy; puertos localhost | Correcto; sin modo write ni reinicio destructivo |
| Dev Container CLI read-configuration | Examinar configuración fusionada, sin recrear contenedores | Lee configuración con aviso de parser; solo Compose base, AUD-05 |

El comparador auxiliar offline pasó además 3/3 pruebas de fronteras/unión de intervalos.

Total **76 casos Java distintos ejecutados en dos ejecuciones: 70 pasan, 6 fallan**. No se afirma que una ejecución completa de 76 haya pasado. El WAR ya existente se inspeccionó y volvió a arrancar; `verify` actual termina antes de empaquetar por las regresiones. Los tests utilizan Oracle real; MockMvc no es evidencia de transporte HTTP, que se cubre aparte con HttpSecurityTest/OracleConcurrencyTest y el navegador.

## Matriz de requisitos

Referencias abreviadas: B = `backend/src/main/java/local/booking`; F = `frontend/src/app`; JT = `backend/src/test/java/local/booking`. Las clases, métodos y pruebas citadas existen en esas rutas.

| ID | Fuente y criterio | Tipo A/B/C/D | Código/configuración | Prueba y evidencia | Estado | Hallazgo relacionado |
|---|---|---|---|---|---|---|
| R01 | Historia: API crear/listar/cancelar propias | A | B/booking/BookingController, BookingService | BookingApiTest + navegador | Cumple verificado | — |
| R02 | Historia: frontend funcional | A | F/acceso, F/reservas | E2E con Ana/Bruno | Cumple verificado | — |
| R03 | Historia/AGENTS: identidad local verificable Basic | A | B/security/SecurityConfig | HttpSecurityTest, AuditSecurityTest.authenticationIsCheckedAfterValidCsrf | Cumple verificado | — |
| R04 | Historia: colisión exacta y parcial ambos extremos | A | BookingRepository.collisions, TimeRange | OraclePersistenceTest, TimeRangeTest | Cumple verificado | — |
| R05 | Historia: contenido/envolvente, adyacencia, espacios distintos | A | BookingRepository, TimeRange | Casos parametrizados OraclePersistenceTest | Cumple verificado | — |
| R06 | Historia: cancelada libera horario | A | BookingService.cancel, filtro ACTIVE | BookingApiTest, ownershipBothDirectionsWithValidAttackerCsrf | Cumple verificado | — |
| R07 | Historia: cada recurrencia y aceptación parcial | A | BookingService.create, WeeklyRecurrence | Segunda colisión de 4: 3 creadas/1 rechazo; Oracle + UI | Cumple verificado | — |
| R08 | Política: 1–12, ninguna válida, colisión interna | C | WeeklyRecurrence, BookingController | BookingApiTest.recurrenceBoundsAndInternalOverlap y secondOccurrenceConflictPreservesThreeAndReportsNoOtherOwner | Cumple verificado | — |
| R09 | Historia: rango inválido/espacio inexistente | A | BookingRequest, TimeRange, lockById | Iguales/inversas/ausentes/malformadas/sin offset: 400; inexistente 404 | Cumple verificado | — |
| R10 | Contrato: identificadores y cantidad enteros | C | BookingRequest + Jackson | Decimal 1.75 devuelve 201 y confirma una fila en Oracle | No cumple | AUD-02 |
| R11 | Historia: propiedad en servidor, ambos sentidos | A | principal, own, ownedSpace, findByIdAndOwnerId | A/B válidos y CSRF válido; ajena 404, filas intactas; filtros usuario no eluden propiedad | Cumple verificado | AUD-03 solo ruta inexistente |
| R12 | Identidad/estado/ID no controlados por cliente | C | DTO + fail-on-unknown-properties | Seis campos forjados: 400 y count sin cambio | Cumple verificado | — |
| R13 | Política activa y pasado | C | own: ACTIVE y endsAt > reloj | Incluye en curso; excluye terminadas/canceladas; pasado permitido pero no listado | Cumple verificado | — |
| R14 | Política de zona horaria | C | OffsetDateTime, TIMESTAMP WITH TIME ZONE, UI -05:00 | OraclePersistenceTest, AuditSecurityTest, UI 2040 y SQL -05:00 | Cumple verificado | — |
| R15 | Contrato 201/200/409/204 y repetición cancelación | C | BookingController/Service | API total/parcial/ninguna; cancelar dos veces 204 | Cumple verificado | — |
| R16 | Contrato errores 400/404/405 frente a 500 | C | B/errors/ApiErrors | Tres regresiones fallan por catch-all 500 | No cumple | AUD-03 |
| R17 | Historia/AGENTS: API documentada OpenAPI | A | POM SpringDoc, BookingController | JSON real solo POST 200; sin securitySchemes | Cumple parcialmente | AUD-04 |
| R18 | Seguridad: CSRF creación/cancelación | C | SecurityConfig, IdentityController, F/acceso | Ausente/inválido/cruzado 403 sin cambios; válido funciona; cambio de usuario UI | Cumple verificado | — |
| R19 | Seguridad: CORS y credenciales limitadas a API | C | SecurityConfig, auth.interceptor, api-path | Origen ajeno sin ACAO; prueba de ruta y CSRF adicional | Cumple verificado | — |
| R20 | Seguridad: sin datos ajenos en colisiones/errores | A | BookingResult, ApiErrors | Mensajes/DTO genéricos; pruebas de propietario y HTTP | Cumple verificado | — |
| R21 | Seguridad: renderizado de entradas | A | Interpolaciones Angular | Texto `<b>` inocuo literal, sin nodo b; sin innerHTML/bypass en código | Cumple verificado | — |
| R22 | Seguridad: no secretos en código/historial | A | Git accesible y archivos locales | 274 blobs/18 commits, patrones y valores efectivos sin coincidencias; límites declarados | Cumple verificado | — |
| R23 | Secretos externos, PasswordEncoder | C | SecurityConfig, configtree/runtime | BCrypt, sin fallback operativo; WAR/metadatos imagen sin valores conocidos | Cumple verificado | — |
| R24 | Restauración: puertos solo localhost y marcador | B | Compose + override importado | ps/inspect: 127.0.0.1:4200/8080; Oracle sin publicación; JDBC read coincide | Cumple verificado | — |
| R25 | Seguridad: parametrizar SQL/validar servidor | A | JPQL/repositorios y DTO | Parámetros enlazados; cadena SQL en spaceId 400; sin concatenación de entradas | Cumple verificado | AUD-02 validación numérica |
| R26 | Sin dobles reservas concurrentes Oracle | A | lock espacio → colisiones → flush → commit | Dos HTTP/conexiones coordinadas: 201+409, una ACTIVE | Cumple verificado | — |
| R27 | Integridad de series y rollback técnico | C | Una transacción create; flush por ocurrencia | Fallo tras flush real: 503, cero filas; lock antes de toda serie | Cumple verificado | — |
| R28 | Espera de bloqueo acotada | C | SpaceRepository.lockById, hint 10000 | Nueva prueba HTTP real, 503 y cero escrituras en 8–14 s | Cumple verificado | — |
| R29 | Dependencias reales y versiones resueltas | A | POM/wrapper, package-lock | Maven/npm resuelven y compilan; inventarios transitivos | Cumple verificado | — |
| R30 | Seguridad de dependencias, sin fallos comunes | A | Runtime Maven, catálogo OSV | Versiones afectadas verificadas; varias condiciones inactivas y otras por resolver | Cumple parcialmente | AUD-01 |
| R31 | Angular 20 standalone/strict, PrimeNG20, Tailwind3.4, RxJS7.8, CDK20 | A | package.json/lock, tsconfig, componentes | ngc/build; familias exactas tabla siguiente | Cumple verificado | — |
| R32 | MSAL3.x y Resource Server preparados | A | azure-identity, identity.config, JwtSecurityConfig | Adaptadores compilados, claims probados; Basic arranca sin Azure | Cumple verificado | — |
| R33 | Java21/Boot3.3 o3.5/Framework/Security/Data/Hibernate/SpringDoc | A | Árbol Maven y Java real | Familias actuales corresponden al original; parcheo pendiente | Cumple verificado | AUD-01 |
| R34 | Oracle19+ y WAR real | A | Oracle23.26.3; backend/target/*.war | JDBC, contenido WAR y arranque local repetido | Cumple verificado | — |
| R35 | Despliegue WebLogic12.2.1.4 | A | Boot3/Jakarta vs runtime JavaEE7 | Runtime no disponible ni acuerdo; contradicción oficial | No verificable | AUD-06 / T015 |
| R36 | Flujo Dev Container limpio de T013 | C | .devcontainer/devcontainer.json | No usa override importado; no Reopen in Container ejecutado | Cumple parcialmente | AUD-05 |
| R37 | Condiciones adicionales del correo | A | Original no localizado | No se sustituyen por guía derivada | No verificable | Límite de fuentes |
| R38 | Semillas/persistencia sin recreación | C | MERGE insert-only, validate/init never | Dos seed: 0 filas; JDBC read y fixtures delimitadas | Cumple verificado | — |
| R39 | Monolito por funcionalidades/base definitiva | B | B/booking,space,security,errors; F/acceso,reservas | Inspección y diff productivo vacío | Cumple verificado | — |
| R40 | SDD, decisiones reales e historial incremental | A | .specify/specs/.handoffs/BITACORA/Git | 18 commits accesibles; decisiones humanas distinguidas; T017 conserva historia | Cumple verificado | — |
| R41 | Documentación de solución real | A | README/contrato/explicación/script verificación | Código y 12 secciones existen; OpenAPI/script contienen discrepancias | Cumple parcialmente | AUD-04, AUD-05 |
| R42 | Error visual de fechas iguales y recuperación | B | booking-range.validator, BookingApi.timeout | UI bloquea igualdad; 13 tests, timeout virtual sin reintento | Cumple verificado | Causa inicial no acreditada |
| R43 | Navegación básica por teclado | D | labels/input/button nativos | Tab de usuario a contraseña; controles identificables; no auditoría WCAG completa | Cumple verificado | — |
| R44 | Mínimo privilegio de cuenta runtime | D | BOOKING con DB_DEVELOPER_ROLE | 29 privilegios efectivos y cuota USERS ilimitada; no DML restringido | Cumple parcialmente | AUD-07 |
| R45 | Consumo/listas y endurecimiento frontend | D | own().toList, lazy space; Vite HTTP local | Riesgos inspeccionados, no SLA/DoS exigido o ejecutado | Cumple parcialmente | AUD-08 |
| R46 | Starter, migración y defecto sembrado | B | DEC-004/T014 | Excluidos por instrucción humana; no implementados | No aplica justificado | — |
| R47 | Azure real, notificaciones, CRUD admin, i18n | A | Fuera de alcance en historia | No se exigen componentes inexistentes | No aplica justificado | — |

## Superficie, confianza y controles reales

Navegador → Vite/proxy mismo origen `localhost:4200` → API `localhost:8080` → Oracle `oracle:1521/FREEPDB1`. Publicación Docker restringida a **127.0.0.1**; Oracle no publica puertos. El contenedor dev monta el repositorio y secretos en lectura: un proceso comprometido del usuario de desarrollo puede alcanzarlos. No hay frontera de producción ni TLS en este entorno local. No se inspeccionó una exposición pública porque no existe un despliegue autorizado.

| Ruta/método real | Sin autenticación | Con Basic válido | Observación |
|---|---|---|---|
| GET / en 4200 | 200 | 200 | UI pública de desarrollo |
| GET /api/csrf | 200 | 200 | Solo token enmascarado; sesión necesaria para CSRF |
| GET /api/me, /api/spaces, /api/bookings | 401 | 200 | Identidad, catálogo autorizado, lista propia |
| POST /api/bookings | 401 con CSRF válido | 201/200/409; 400 entradas; 403 CSRF | Se probó autenticación separada de CSRF |
| DELETE /api/bookings/{id} | Filtros Basic/CSRF | 204 propia, 404 ajena; 403 sin CSRF | Idempotente; no operación masiva |
| GET /v3/api-docs y /swagger-ui/index.html | 401 | 200 | Documentación protegida; esquema de autenticación ausente |
| GET /api/bookings/{id}; PUT /api/bookings/{id} | Protegida | 500 en ruta/método no implementados | No se exige crear edición; AUD-03 |
| GET /actuator, /actuator/health, ruta inventada | 401 | 500 | No hay dependencia Actuator; catch-all oculta el 404 |
| OPTIONS /api/bookings con Origin ajeno | 401, sin ACAO | No origen habilitado | Complementado con POST autenticado sin CSRF 403 |

Filtros Spring Security activos: Basic, contexto sin persistencia de autenticación, CSRF por HttpSession y cabeceras. No perfiles Maven adicionales. Modo Basic actual; JWT condicionado e inactivo. No `EnableMethodSecurity`; la propiedad se impone en consultas/servicio. Ningún parámetro ownerId/userId decide identidad. El catálogo de espacios contiene datos compartidos intencionalmente, no reservas de terceros.

Cookie de sesión observada `HttpOnly; SameSite=Strict`, sin Secure en HTTP localhost. No cookie XSRF legible por JavaScript: Angular obtiene el token del JSON `/api/csrf` y lo envía en `X-CSRF-TOKEN`. El contexto es stateless para identidad, no para CSRF. Logout vacía credenciales/estado de Angular; con Basic no revoca la contraseña ni una sesión de identidad del servidor. Las pruebas de cambio de usuario no mostraron la lista anterior. Interceptor limita Authorization a rutas relativas `/api/`; credenciales en memoria, sin local/sessionStorage en código propio.

API sirve `nosniff`, `DENY` y `no-store` en las respuestas observadas; frontend de desarrollo carece de CSP/frame protections equivalentes. `X-XSS-Protection: 0` no se interpreta como defecto por sí solo. No se impone HSTS a HTTP. Antes de exposición externa serían necesarios transporte TLS y revisión de origen, cookies, servidor estático y secretos; no se afirma seguridad externa a partir de localhost.

Pruebas de archivos por Vite: `/@fs/workspace/.local/runtime/booking.properties`, app-password, restore-key y `.git/config` devolvieron 403. `/.env`/ruta con `..` devolvieron HTML del fallback SPA, sin contenido del archivo ni coincidencia con secretos conocidos. No se confundió status 200 con divulgación. El DOM mostró literalmente `<b>AUDIT-XSS-47a6af4</b>` y ningún elemento `option b`. No se usó código activo malicioso. Logs de navegador muestreados: conexión Vite y modo desarrollo; sin secretos en esos mensajes.

Secretos: búsqueda sobre 274 blobs accesibles (2,246,850 bytes) y 18 commits, valores efectivos conocidos de Oracle/Basic más patrones de claves privadas, GitHub y AWS: cero coincidencias. No se certifican objetos Git inalcanzables, formatos desconocidos o todos los backups externos. WAR sin rutas privadas/valores conocidos; imagen inspect/history redactados sin coincidencias ni modo privileged. No se escanearon todas las capas binarias de imágenes: ausencia en metadatos no prueba ausencia universal. `.dockerignore` limita el contexto a Dockerfile. `.local/` y `*.pem` ignorados, además de comprobar ausencia en archivos candidatos al commit.

BOOKING usa **DB_DEVELOPER_ROLE**, heredando RESOURCE/SODA_APP: 29 privilegios de sesión, entre ellos CREATE TABLE/PROCEDURE/JOB/MLE, DEBUG CONNECT SESSION; cuota USERS ilimitada. No aparece DBA ni privilegios ANY en la consulta. Es una cuenta de desarrollo más poderosa de lo necesario para ejecutar el CRUD, AUD-07. No se retiraron permisos ni cambiaron credenciales. [Privilegios observados](audit-evidence/oracle-roles.txt), [comparación privada/ignorados](audit-evidence/final-checks.json).

SQL/recursos: colisiones con parámetros enlazados, índices por espacio/status/fechas y propietario/status/fin. No concatenación de valores de petición en JPQL/SQL. `own()` devuelve todos los resultados y puede cargar espacios lazy (hasta una consulta adicional por espacio distinto); no se midió una cantidad N+1 real. Recurrencia acotada 1–12, pero no límite funcional de duración ni prohibición de pasado: son políticas documentadas, no defectos inventados. Timeout cliente 15 s no garantiza rollback del servidor. Espera de lock comprobada y fallo técnico revierte toda la serie. ORM registra ORA-00054/40097 en el log local de la prueba, mientras el cliente recibe 503 genérico sin SQL, stack o credenciales. No se afirma que los logs técnicos carezcan de información interna.

## Navegador y persistencia

E2E nueva sobre aplicación real: Ana creó #204 para 08/06/2040 10–11; Bruno no la vio en su listado. Bruno solicitó cuatro semanas desde 01/06/2040: UI informó tres creadas (#205–207) y una rechazada para 08/06, sin propietario/ID ajeno. Repetición mostró cuatro horarios no disponibles y mantuvo las reservas propias. Al volver a Ana, la UI mostró su reserva y no las de Bruno. Cada propietario canceló sus fixtures; consulta Oracle final confirmó #204–209 CANCELLED. Los clics de cancelación se verificaron individualmente: el guard busy puede ignorar clics mientras otra petición está pendiente, por lo que no se dio la limpieza por hecha tras emitir acciones.

Fechas mostradas coinciden con SQL `-05:00`. Igualdad de inicio/fin presentó aviso junto al campo y botón desactivado. Tab desde Usuario llevó realmente a Contraseña. No se hizo una evaluación completa WCAG ni corte real de red; la regresión de timeout usa tiempo virtual y HTTP simulado, según T016. El origen exacto del bloqueo de la captura anterior sigue no confirmado; no se atribuye a extensiones.

## Stack, WAR y entrega

| Elemento | Versión real / comprobación |
|---|---|
| Java / Maven / Node / npm | Temurin 21.0.10+7 / 3.9.9 / 22.22.0 / 10.9.4, comandos dentro de dev Linux amd64 |
| Boot / Framework / Security | 3.3.13 / 6.1.21 / 6.3.10, árbol resuelto |
| Data JPA / Hibernate / SpringDoc | 3.3.13 / 6.5.3.Final / 2.6.0 |
| Oracle / JDBC | 23.26.3 / ojdbc11 21.9.0.0; conexión BOOKING/FREEPDB1 |
| Angular core/compiler / CLI / CDK | 20.3.31 / 20.3.37 / 20.2.0 |
| PrimeNG / Tailwind / RxJS | 20.0.0 / 3.4.17 / 7.8.2 |
| TypeScript / MSAL Angular / Browser | 5.9.2 / 3.1.0 / 3.28.1 |
| WAR local | Tomcat 10.1.42 en WEB-INF/lib-provided; Main-Class de Boot; clases propias presentes |

TypeScript/Angular strict activados y build pasa; `skipLibCheck=true` limita revisión de declaraciones de librerías, no desactiva strict del código propio. PrimeNG Button se usa; CDK está resuelto y no se inventa un uso innecesario. MSAL tiene factory/providers y adaptador real condicionado; Resource Server tiene decoder/validadores/principal. No se confunde dependencia con login Azure real: no tenant/JWKS/firma contra Azure ejecutados. `javax.sql.DataSource` pertenece al JDK y no es incumplimiento Jakarta.

[Spring Boot 3.3](https://docs.spring.io/spring-boot/3.3/system-requirements.html) requiere Java17+ y contenedor Servlet5+; [WebLogic12.2.1.4](https://docs.oracle.com/en/middleware/fusion-middleware/weblogic-server/12.2.1.4/intro/compatibility.html) documenta JavaEE7/Servlet3.1. El cambio javax→jakarta no se resuelve generando un WAR. No existe prueba de despliegue allí; T015 sigue bloqueada por aclaración externa y es independiente del starter. No se forzaron transitivas.

La opción Boot3.5 permitida en AGENTS no resuelve todas las familias originales: la [tabla oficial consultada](https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/coordinates.html) gestiona Security6.5.11 y DataJPA3.5.13, frente a Security6.3/6.4 y DataJPA3.3 del original. Es una discrepancia de requisitos que debe acordarse si se elige esa vía de parcheo; el stack actual sí cumple esas familias, pero tiene avisos pendientes. No se asume que un BOM más nuevo autorice reducir requisitos.

Dev Container: solo se ejecutó `read-configuration` de la CLI instalada. Se encontró un aviso al interpretar `node:tag@digest` (no se atribuye fallo de Docker por ello), y la configuración fusionada contiene solo compose.yaml, initialize prepare y postCreate verify-local.sh. No se abrió/recreó el entorno mediante VS Code: eso podría reconstruir/alterar el entorno que debía conservarse. Compose funcional no equivale a Dev Container acreditado. La aceptación previa de T013 queda matizada por AUD-05; no se borra la evidencia histórica.

## Hallazgos priorizados y cierre esperado

### AUD-01 — Dependencias de backend con avisos vigentes

- Requisito R30 (A), categoría seguridad/cadena de suministro. **Alto para priorización de entrega**, no explotación crítica reproducida. Evidencia cierta de versiones; alcanzabilidad variable. Ubicación: backend/pom.xml, árbol resuelto, WAR/Tomcat y SecurityConfig.
- Reproducción segura: procedimiento offline de scripts/audit; 65 coincidencias por rango, con scopes y fuentes en [anexo](AUDITORIA_DEPENDENCIAS.md). Dos avisos son test; provided de Tomcat se usa en ejecución. No se hicieron PoC destructivas.
- Esperado: dependencias sin avisos aplicables sin tratar. Observado: varios prerrequisitos ausentes (HTTP2, Digest, JSP, método security, Windows runtime); otros requieren cierre, particularmente cabeceras Security y recursos/multipart. Cabeceras presentes en la muestra no prueban que CVE-2026-22732 sea inaplicable. Impacto potencial de confidencialidad/integridad/disponibilidad condicionado; exposición actual localhost.
- Corrección mínima propuesta T018: resolver aplicabilidad de casos pendientes y elegir conjunto de parches compatible/BOM soportado. Tomcat10.1.59 es candidato de misma familia, no actualizar Spring transitivo aisladamente para aparentar cumplimiento. Documentar soporte Enterprise o acuerdo de familias si corresponde; no se ejecutó ningún cambio.
- Regresión de cierre: repetir inventarios/catalogación, explicar cada aviso retenido, pruebas de cabeceras éxito/error, Basic/CSRF/propiedad, WAR y Oracle; no basta npm=0.

### AUD-02 — JSON decimal se convierte silenciosamente en entero

- R10 (C), validación/integridad, **Medio**, reproducido. Ubicación: BookingRequest (Long spaceId/Integer occurrences) y ObjectMapper/configuración Jackson por defecto.
- Reproducción: con Basic y CSRF válidos, enviar cuerpo válido con `spaceId:1.75` o `occurrences:1.75`. Esperado 400 y cero escrituras; observado 201 y una reserva confirmada, espacio1. HTTP real #208–209 visibles en GET posterior; canceladas por su dueño. [Evidencia](audit-evidence/decimal-live.json).
- Impacto: API acepta una entrada que no representa el identificador/cantidad contratado; cliente puede reservar un espacio distinto del valor enviado. No demuestra elusión de propiedad o colisiones.
- Corrección mínima T019: rechazar coerción de flotante a tipos enteros en deserialización, conservando positivos/límites. Probar ambos campos, enteros válidos, negativos, nulos y límites; no confiar solo en Angular.
- Regresión conservada: AuditSecurityTest.fractionalIntegersMustNotCreateReservations (2 fallos esperados actuales).

### AUD-03 — Catch-all convierte errores de cliente en 500

- R16 (C), contrato/diagnóstico, **Medio**, reproducido. Ubicación: errors/ApiErrors, handler Exception.
- Reproducción autenticada: GET a recurso no implementado →500; DELETE con ID no numérico y CSRF válido →500; PUT a ruta de cancelación con CSRF válido →500. Esperado 404/400/405 respectivamente. No hay endpoint de edición que deba implementarse. GET de IDs ajenos conocidos también devolvió500 sin campos de reserva en ambos sentidos; se comprobó tras cancelar los fixtures, no como lectura de una reserva activa. [Evidencia](audit-evidence/known-id-reads.json).
- Impacto: clasificación engañosa, observabilidad y mensajes del cliente; no se expuso stack ni dato ajeno en cuerpo. `/actuator` autenticado también aparenta error500 aunque no existe Actuator.
- Corrección mínima T020: handlers específicos de excepciones MVC o delegación estándar, manteniendo errores redactados, estado semántico y Allow cuando corresponda. Conservar 500 solo para inesperados.
- Regresiones: missingResourceShouldBe404Not500, malformedIdentifierShouldBe400Not500, unsupportedEditShouldBe405Not500.

### AUD-04 — OpenAPI no describe autenticación ni respuestas efectivas

- R17 (A), entrega/contrato, **Medio**, reproducido. Ubicación: BookingController, IdentityController y configuración SpringDoc inexistente para seguridad.
- Reproducción: consultar `/v3/api-docs` autenticado. Esperado esquema Basic/CSRF documentado y 201/200/409/400/401/403/404/503 reales; observado POST solo200 y ausencia securitySchemes. Markdown del contrato es más preciso, pero no corrige el documento generado.
- Impacto: consumidores y Swagger no pueden inferir/usar correctamente el contrato; no es bypass de autenticación (Swagger protegido).
- Corrección mínima T021: metadatos OpenAPI de seguridad/respuestas/esquemas manteniendo autorización real. Documentar token y cookie sin poner valores. Regresión: openApiDocumentsSecurityAndRealPostStatuses, ampliarla a 200 parcial, DELETE204 y errores.

### AUD-05 — Dev Container no refleja la restauración y sonda obsoleta

- R36 (C), R41 (A), reproducibilidad/documentación, **Medio**, configuración confirmada; arranque completo no verificado. Ubicación: .devcontainer/devcontainer.json y scripts/verify-local.sh.
- Reproducción: CLI read-configuration produce dockerComposeFile único compose.yaml; omite .local/transfer/compose.images.yaml. Además la sonda imprime “Starter application and WAR are not available.” pese a existir aplicación/WAR y haberse excluido el starter. Esperado reproducir el entorno autorizado con imágenes importadas y mensajes actuales.
- Impacto: apertura por VS Code puede tomar una ruta de build distinta de la restauración; T013 no acredita completamente su criterio Dev Container. No se afirma haber reproducido pérdida de datos ni fallo total del editor.
- Corrección mínima T022: definir y documentar perfil de Dev Container restaurado compatible con ambos Compose, conservar ruta de instalación original cuando proceda, actualizar mensajes sin reintroducir starter. Verificar mediante apertura real sin tocar volúmenes/credenciales y JDBC read. Propuesta no aplicada.

### AUD-06 — Entrega WebLogic no acreditada y requisitos incompatibles

- R35 (A), compatibilidad/entrega, **Alto como bloqueo externo**, no vulnerabilidad reproducida. Ubicación: POM/Jakarta y requisito WebLogic12.2.1.4; fuentes oficiales anteriores.
- Reproducción disponible: cotejo de versiones/namespace; no hay runtime WebLogic local probado. Esperado despliegue en runtime acordado; observado WAR ejecutable con Tomcat, sin evidencia WebLogic.
- Impacto: no puede certificarse entrega completa al evaluador. Corrección mínima: **T015 existente**, obtener aclaración explícita y ejecutar prueba del destino acordado. No bajar a Boot2/JavaEE ni migrar por cuenta del auditor. Regresión: despliegue, login, reservas y JDBC reales en ese runtime.

### AUD-07 — Privilegios amplios de cuenta de desarrollo

- R44 (D), endurecimiento, **Medio condicionado** a compromiso de la app/cuenta. Ubicación: BOOKING y aprovisionamiento Oracle local. Riesgo por inspección, sin abuso ejecutado.
- Reproducción segura: USER_ROLE_PRIVS, SESSION_ROLES, SESSION_PRIVS y USER_TS_QUOTAS; DB_DEVELOPER_ROLE, 29 privilegios, cuota ilimitada. Esperado para runtime endurecido: solo permisos necesarios; observado mezcla de dueño/desarrollador/runtime.
- Impacto: una credencial comprometida permitiría modificar objetos propios y consumir más recursos que el CRUD. No se exige separar cuentas como condición original del MVP local.
- Propuesta opcional T023: diseñar permisos de ejecución separados del aprovisionamiento preservando datos/credenciales actuales hasta autorizar migración. Regresión futura: CRUD funciona, DDL no autorizado falla; no ejecutar DDL destructivo de prueba contra tablas reales.

### AUD-08 — Límites y cabeceras del entorno de desarrollo

- R45 (D), rendimiento/endurecimiento, **Bajo**, riesgo por inspección. Ubicación: BookingService.own/BookingView, frontend servido por Vite y login sin limitador. No DoS ni acceso indebido demostrado.
- Reproducción segura: inspección de toList/lazy space y respuesta raíz sin CSP/frame policy. Esperado solo si aumenta exposición/volumen: límites medidos y servidor endurecido. Observado listado sin paginación, posibles lecturas extra por espacio, cliente desarrollo, sin cuota de intentos.
- Impacto condicionado a volumen/abuso; no se inventan SLA, WAF o infraestructura distribuida como requisito. Propuesta opcional T024: medir con fixtures pequeños, elegir fetch/página solo con evidencia y definir cabeceras/transporte del destino. Regresión: volumen y consultas documentados, flujo UI/CSRF sin romperse; no añadir componentes innecesarios.

## Límites abiertos y siguiente acción

No verificables: condiciones del correo ausente; despliegue WebLogic; arranque íntegro mediante Dev Container; firma/login contra Azure (fuera del MVP); explotación de todos los avisos o escaneo exhaustivo de OS/capas; causa original de la espera de la captura. No se sustituyen estos límites por porcentajes de cumplimiento.

El análisis remoto de paquetes OSV fue rechazado por la revisión automática de permisos porque habría enviado nombres/versiones derivados del repositorio privado. No se realizó ni se reintentó por otro servicio. Se completó el análisis de versiones descargando catálogos públicos completos y comparando localmente; no se necesita autorización adicional para finalizar este diagnóstico.

Siguiente acción propuesta: T018 para resolver avisos aplicables y estrategia compatible de parches; T019–T022 son correcciones locales acotadas; T015 requiere aclaración externa independiente. T023–T024 son opcionales, no obligaciones inventadas. El [plan de correcciones](PLAN_CORRECCIONES_AUDITORIA.md) enlaza el único estado en tasks.md. Ninguna corrección productiva fue ejecutada durante T017.

La aplicación queda iniciada con el WAR existente y Oracle saludable. `pwsh -NoProfile -File scripts/app.ps1 stop` detiene la app; `pwsh -NoProfile -File scripts/app.ps1 start` la inicia con ambos Compose y --no-build. `verify` ahora **debe fallar** por las seis regresiones pendientes y puede dejar la app detenida: usar start después. No borrar el WAR para eludir ese resultado ni excluir las pruebas.

## Enlace posterior (el informe precedente conserva su revisión y resultados)

Correcciones posteriores: [anexo y verificaciones](CIERRE_CORRECCIONES_AUDITORIA.md). No modifica los hallazgos históricos de47a6af4.
