# Explicación de la implementación construida

> Auditoría T017: hay seis regresiones pendientes; verify falla actualmente. Consultar [dictamen y límites](AUDITORIA_CUMPLIMIENTO_Y_SEGURIDAD.md) antes de interpretar las verificaciones históricas como cumplimiento integral.

Referencia de entrega local del 2026-09-14 (America/Bogota). Los estados se mantienen
solo en [tasks.md](../specs/001-booking-espacios/tasks.md); decisiones e historia en
[BITACORA](BITACORA.md). Este documento describe código ejecutado; los retos de la
sección 11 son ejercicios de análisis y no amplían lo implementado.

## 1. Estado y alcance real

Booking permite entrar con dos identidades, consultar tres espacios semilla, crear
reservas individuales o semanales, ver las propias activas y cancelarlas. Detecta
colisiones y acepta las ocurrencias válidas de una solicitud parcialmente ocupada.
Backend Spring Boot, UI Angular y Oracle funcionan dentro del entorno Docker existente.
Se conserva el marcador original de persistencia, independiente de las tablas Booking.

No se implementaron notificaciones, administración de espacios/usuarios, edición,
RRULE general, cancelación de serie ni integración Azure real. La ejecución local
usa el WAR con Tomcat; la aclaración del runtime WebLogic (T015) continúa externa.
No se desplegó públicamente ni se subieron estos commits. Starter excluido, T014 no
implementada. Las etapas anteriores sin aplicación permanecen en Git y sus informes.

## 2. Ejecución y versiones

En `C:\PruebaAIFORGESQUAD`, PowerShell 7: `pwsh -NoProfile -File scripts/app.ps1 start`;
verificación completa: acción `verify`; parada que conserva datos: `stop`; consulta:
`status`. [README](../README.md) contiene comandos completos y recuperación del entorno.
UI http://localhost:4200/, API http://localhost:8080/api, OpenAPI protegido
http://localhost:8080/v3/api-docs. Credenciales de ana/bruno exclusivamente en
`.local/runtime/booking.properties`. Recargar borra la identidad en memoria del navegador.

| Componente | Versión resuelta |
|---|---|
| Java / Maven / Wrapper | 21.0.10 / 3.9.9 / 3.3.4 |
| Boot / Spring / Security / Data JPA | 3.3.13 / 6.1.21 / 6.3.10 / 3.3.13 |
| Hibernate / ojdbc11 / Oracle | 6.5.3.Final / 21.9.0.0 / 23.26.3 |
| SpringDoc | 2.6.0 |
| Node / npm / TypeScript | 22.22.0 / 10.9.4 / 5.9.2 |
| Angular core / CLI-build / CDK | 20.3.31 / 20.3.37 / 20.2.0 |
| PrimeNG y themes / Tailwind / RxJS | 20.0.0 / 3.4.17 / 7.8.2 |
| MSAL Angular / Browser | 3.1.0 / 3.28.1 |

Maven y npm usan sus cachés locales; la primera descarga puede necesitar Internet.
`app.ps1` no reconstruye imágenes. `verify` sí reconstruye WAR y frontend y ejecuta
pruebas; `start` usa WAR ya generado. Angular devserver es la ejecución local,
mientras `npm run build` valida el empaquetado de producción; no se publica ese dist.

## 3. Arquitectura concreta

DEC-005 es elección del usuario: un monolito por funcionalidades, con separación
controlador–servicio–repositorio dentro de booking. DEC-008 a DEC-013 son decisiones
técnicas de Codex dentro de la autorización DEC-007, sin atribuir aprobación individual.
Una alternativa de puertos/adaptadores añadiría interfaces y mapeos sin una segunda
persistencia ni necesidad actual; no se inventa un rechazo humano de esa alternativa.

| Responsabilidad | Código real (rutas desde raíz) |
|---|---|
| Entrada y arranque WAR | `backend/src/main/java/local/booking/BookingApplication.java` |
| Reservas, transacción, DTO y reglas | `backend/src/main/java/local/booking/booking/` |
| Catálogo y fila bloqueada | `backend/src/main/java/local/booking/space/` |
| Basic, CSRF, JWT e identidad | `backend/src/main/java/local/booking/security/` |
| Errores HTTP comunes | `backend/src/main/java/local/booking/errors/ApiErrors.java` |
| Acceso Angular e interceptor | `frontend/src/app/acceso/` |
| Formulario/lista/servicio HTTP | `frontend/src/app/reservas/` |
| DDL y semillas explícitas | `backend/src/main/resources/db/oracle/` |
| Operación local | `scripts/app.ps1`, `scripts/app-process.sh` |

`BookingService` depende de `BookingRepository`, `SpaceRepository` y `Clock`;
no hay bus, microservicios ni repositorio genérico propio. `errors` existe porque
el tratamiento se comparte entre controladores. Hibernate valida el esquema;
`application.properties` desactiva creación automática y open-in-view. Dos tablas,
dos secuencias, FK/checks e índices; semilla insert-only conserva modificaciones.

## 4. Recorridos completos

Acceso: `login.component.ts` → `AuthService.login` → `/api/me` → `/api/csrf`.
`auth.interceptor.ts` adjunta identidad únicamente a rutas relativas propias `/api/`;
al escribir añade el token. `api-path.ts` excluye URLs externas/absolutas. No hay
persistencia de Basic en localStorage ni envío de usuario en el DTO de reservas.

Crear: `BookingsComponent.create` valida el formulario tipado; `bookingRangeValidator`
rechaza igualdad/rango inverso con aviso junto a Fin antes de enviar (T016/DEC-014). Agrega offset -05:00
y llama `BookingApi.create`. `BookingController` valida `BookingRequest`; obtiene
`Principal.getName()` y llama `BookingService.create`. La transacción expande
semanas con `WeeklyRecurrence.expand`, bloquea Espacio con `lockById`, consulta
`BookingRepository.collisions`, guarda con `saveAndFlush` las válidas y devuelve
`BookingResult`. El controlador elige 201/200/409 y la pantalla muestra creadas y
rechazos por fecha, luego actualiza la lista. BookingApi limita la espera a 15 segundos;
finalize libera los controles. Un timeout no demuestra rollback: consultar la lista
antes de volver a reservar. No se reintenta POST automáticamente.

Consultar: `BookingApi.own` → GET `/api/bookings` → `BookingService.own` → consulta
por propietario, ACTIVE y `endsAt > now(clock)`, ordenada. Incluye reservas iniciadas
que todavía no terminan. `BookingView.of` evita serializar directamente entidades JPA.

Cancelar: DELETE `/api/bookings/{id}` → `BookingService.cancel`: busca spaceId por
id/propietario, bloquea Espacio, vuelve a cargar la reserva propia y cambia a CANCELLED.
El horario queda libre; repetir conserva 204. Ajena o inexistente responde el mismo
404. No borra la fila ni permite cancelar toda una serie con una sola acción.

## 5. Reglas y ejemplos

Intervalos semiabiertos `[inicio,fin)`. Colisión si `existente.inicio < pedido.fin`
y `existente.fin > pedido.inicio`, mismo espacio y ACTIVE. Frente a 10:00–12:00:
09:00–11:00, 11:00–13:00, igualdad y contenciones chocan; 08:00–10:00 y 12:00–14:00
se permiten. SQL JPA es la decisión persistida; `TimeRange` expresa la misma regla
pura y sus pruebas se complementan con pruebas del query contra Oracle.

OffsetDateTime/NATIVE y TIMESTAMP(9) WITH TIME ZONE conservan el instante y precisión;
la UI muestra Bogotá. Recurrencia semanal 1–12, primera incluida; null/ausente=1.
`WeeklyRecurrence` convierte a America/Bogota y suma semanas. Cada ocurrencia genera
una reserva independiente, sin entidad Serie. Si solo la segunda de cuatro choca:
201 no corresponde; se devuelven 200, tres creaciones y un rechazo. Si todas chocan,
409 y cero creaciones. Una ocurrencia previa del mismo pedido también participa en
la consulta después del flush: no se ignoran conflictos internos.

Una colisión esperada no lanza excepción: permite aceptación parcial dentro de una
transacción. Una excepción técnica sale del servicio y revierte todas las escrituras;
`ApiErrors` produce respuesta genérica. `flush` envía SQL pero NO confirma la transacción.
No se ha añadido prohibición del pasado, duración máxima ni margen entre reservas.

## 6. Seguridad y concurrencia

`SecurityConfig` autentica dos usuarios externos con BCrypt. STATELESS se aplica al
contexto de seguridad; el repositorio CSRF sí usa sesión, HttpOnly/SameSite Strict.
La cookie sola no autentica. Se conserva validación CSRF en cada escritura y se evita
rotar el token por cada Basic correcto (`NullAuthenticatedSessionStrategy` dentro
CSRF). La prueba HTTP real cubre consultas paralelas y escrituras posteriores, caso
que reveló un fallo de sesión no observado con MockMvc (DEC-010).

El propietario viene del principal; DTO rechaza campos desconocidos. No se expone
propietario ajeno al rechazar colisiones. Secretos originales y clave permanecen
excluidos de Git; las credenciales de aplicación tampoco se empaquetan. HTTP Basic
está limitado a desarrollo localhost; una exposición futura exigiría revisar TLS,
gestión de identidades y operación, fuera de la certificación actual.

La fila Espacio existe incluso vacía. PESSIMISTIC_WRITE la retiene hasta commit o
rollback con aislamiento READ_COMMITTED. Tanto creación como cancelación siguen
ese protocolo: otra creación espera y consulta el estado confirmado. Bloquear solo
reservas no protege un espacio inicialmente vacío; synchronized no protege otras JVM.
Este protocolo cubre escritores de la aplicación: SQL externo que lo ignore puede
violar la regla; no existe constraint Oracle de exclusión de intervalos. No se midió
rendimiento ni se promete una latencia máxima por el hint de timeout.

Extensión empresarial preparada, sin activar: `identity.config.ts` deja configuración
undefined; `provideAzureIdentity` devuelve providers vacíos. Si se configurase, el
adaptador `azure-identity.ts` usa MsalService initialize/loginPopup y memoria; AuthService
expone loginEnterprise. Backend `JwtSecurityConfig` depende de `booking.auth.mode=jwt`,
`booking.jwt.issuer`, `booking.jwt.jwk-set-uri`, `booking.jwt.audience`; Nimbus configura
firma y validadores de issuer, tiempo, audience y subject. Principal `jwt:` + SHA-256
(issuer y subject) separa identidades y cabe en OWNER_ID. Basic es el modo ejecutado.
No se validaron tenant, permisos, interacción Azure, descarga JWKS ni firmas reales;
`JwtValidationTest` solo prueba claims y derivación de identidad. No hay botón Azure
en el recorrido Basic ni equivalencia automática entre un usuario Basic y uno JWT.

## 7. Pruebas y evidencia

[VERIFICACION_FINAL](VERIFICACION_FINAL.md) conserva comandos/resultados y límites.
Todas las clases Java se ubican bajo `backend/src/test/java/local/booking/`.

| Requisito / riesgo | Prueba / evidencia |
|---|---|
| FR002/009 persistencia, fechas, FK/checks, semillas | `booking/OraclePersistenceTest`, Oracle real y rollback |
| FR004 intervalos y estado/espacio | `booking/TimeRangeTest` y query real en `OraclePersistenceTest` |
| FR003/006 propietario, cancelar, entradas, colisión | `booking/BookingApiTest`, MockMvc + servicios/JPA/Oracle reales |
| FR007 primera incluida, límites, semanas y parcial | `booking/WeeklyRecurrenceTest`, `BookingApiTest`, navegador H005 |
| FR001 Basic/CSRF/campos prohibidos | `security/SecurityContractTest`; su ruta de contrato es solo de test |
| Sesión/cookie en HTTP real | `security/HttpSecurityTest`, servidor en puerto aleatorio y clientes HTTP |
| FR005 carrera y rollback técnico | `booking/OracleConcurrencyTest`, dos clientes HTTP/Oracle, fixture aislada |
| FR010 extensión compilable y claims | `security/JwtValidationTest`, build Java/Angular; no Azure real |
| FR008/011 UI strict y límite credenciales | `npm run check`, `npm run build`, `npm test`; navegador US1–US3 |
| T016 fechas y recuperación de espera | `frontend/test/booking-range.test.mjs`, `booking-timeout.test.mjs`; Angular FormGroup y BookingApi reales, HTTP simulado/tiempo virtual; navegador para intervalo inválido y válido |
| Infraestructura original | `jdbc-check.sh read`, comparación privada y ciclo stop/start |

La carrera usa bloqueo JDBC externo para observar dos entradas al repositorio antes
de liberarlo: resultados 201 y 409 y una fila. Un spy observa/delega al repositorio
real; no simula la base. Para rollback el spy falla después del primer flush real:
una fila visible dentro de la transacción y cero fuera tras 503. Es inyección de fallo,
no una caída física de Oracle ni ensayo de recuperación de desastres.

En navegador: ana creó/canceló #55 y verificó rechazo duplicado. Ana reservó la segunda
semana #72; bruno obtuvo #73–75 y rechazo solo de esa segunda fecha; cada propietario
canceló sus reservas. Quedaron cinco registros CANCELLED en H007 y se añadió #157 CANCELLED al comprobar
la corrección T016; se conserva esa historia sin borrar
historia. Las pruebas automatizadas revierten transacciones o limpian exclusivamente
sus espacios de fixture; las secuencias avanzan aunque haya rollback.

## 8. Proceso con IA y decisiones

La bitácora distingue propuestas de IA de decisiones humanas. DEC-004 adoptó por
instrucción del usuario el repositorio definitivo. DEC-005 prefirió monolito por
funcionalidades frente a agrupar capas globalmente. DEC-007 autorizó completar las
tareas locales; Codex concretó seguridad (008), lock/transacción (009), UI y corrección
CSRF (010), recurrencia (011), prueba concurrente y extensión (012), operación (013).
Los fallos reales de CSRF, codificación UTF-8, polling y spy se conservan allí.
DEC-014 registra el reporte posterior de fechas iguales, el diagnóstico limitado de
la espera inicial y la corrección de validación cliente/timeout, sin inventar su causa.

| Hito | Commit real |
|---|---|
| Integración H001 | b977ca3, 8ba745a |
| Cambio de alcance y arquitectura | 75af194 |
| Base Oracle T003 | 992bc41 |
| Identidad/contrato H002 | 14fa96d |
| Reservas H003 | 8ba5cf3 |
| UI H004 | 7360c03 |
| Recurrencias H005 | bc5a56a |
| Concurrencia/extensión H006 | 0344b9a |

El commit de cierre se identifica por T013/DEC-013 en Git; no se anticipa un hash.
No se fabrican tiempos humanos, reuniones ni aprobaciones de detalles de implementación.

## 9. Base definitiva y procedencia

Repositorio privado AIFORGESQUADP1, restaurado desde imágenes/respaldo del commit
31782c5 y documentación posterior de main; evidencias en
[TRANSFERENCIA_VERIFICADA](TRANSFERENCIA_VERIFICADA.md) e [INTEGRACION_H001](INTEGRACION_H001.md).
Se conservan Git, configuración Compose/devcontainer, scripts originales y bitácora.
La infraestructura inicial no incluía Booking: el código de negocio se construyó aquí
con trazabilidad incremental. DEC-004: el starter no estará disponible; recepción,
comparación, migración y defecto sembrado son **No aplica por cambio de alcance**.
Los defectos hallados durante esta implementación no se presentan como ese defecto.

## 10. Preguntas respondidas para sustentación

1. **¿Por qué monolito por funcionalidades?** Reúne cada cambio de negocio en booking
   y separa catálogo/identidad. Mostrar BookingService y BookingsComponent. Error:
   creer que organización por funcionalidades implica microservicios.
2. **¿Dónde se decide si hay choque?** En BookingRepository.collisions dentro del
   lock/transacción; TimeRange expresa la fórmula. Error: confiar solo en la UI.
3. **¿Por qué la igualdad choca y la adyacencia no?** Ambas desigualdades estrictas
   se cumplen para igualdad; falla una en el borde contiguo. Mostrar TimeRangeTest y
   oracleCollisionQueryCoversEveryBoundary. Error: cambiar < por <= sin cambiar contrato.
4. **¿Qué evita dos reservas de una sala vacía?** El bloqueo de su fila Espacio.
   Mostrar SpaceRepository.lockById y OracleConcurrencyTest. Error: bloquear filas
   de reservas inexistentes o usar synchronized como bloqueo distribuido.
5. **¿Por qué READ_COMMITTED?** La consulta posterior al lock ve lo confirmado por
   el escritor anterior. Mostrar BookingService.create. Error: pensar que el aislamiento
   por sí solo detecta y evita este conflicto sin protocolo de bloqueo.
6. **¿Cómo distingue parcial de fallo técnico?** La colisión produce Rejected; una
   excepción propaga rollback. Mostrar BookingResult/BookingService/OracleConcurrencyTest.
   Error: capturar toda excepción y continuar o creer que flush equivale a commit.
7. **¿De dónde sale el propietario?** Authentication del controlador, nunca del body.
   Mostrar BookingController, BookingRequest y BookingApiTest. Error: usar un userId
   enviado por Angular como autorización.
8. **¿Por qué 404 al cancelar una ajena?** Misma respuesta que inexistente para no
   revelar existencia/propiedad; consulta filtrada. Mostrar ownedSpace/cancel y su test.
   Error: obtener cualquier reserva por id y solo ocultar el botón en pantalla.
9. **¿STATELESS elimina toda sesión?** No: Security no almacena identidad; CSRF usa
   su sesión. Mostrar SecurityConfig/HttpSecurityTest. Error: aceptar cookie como login
   o desactivar CSRF porque se usa Basic.
10. **¿Qué persiste del acceso del navegador?** Nada de identidad en almacenamiento;
    queda en AuthService en memoria. Mostrar auth.interceptor/api-path y npm test.
    Error: enviar Authorization a cualquier URL o guardar contraseñas en localStorage.
11. **¿Cómo se conserva hora/instante?** Oracle almacena OffsetDateTime con zona;
    WeeklyRecurrence suma semanas Bogotá, la UI envía -05:00. Mostrar entidad Booking,
    WeeklyRecurrence y persistsExactInstantAndRelationship. Error: quitar offset o
    confundir hora de presentación con instante.
12. **¿Qué significa occurrences=4?** Primera más tres semanas. Cada una se valida,
    incluida una que choque con otra del mismo pedido. Mostrar expand y BookingApiTest.
    Error: crear cinco ocurrencias o comprobar solo la primera.
13. **¿Qué prueba la carrera y qué no?** Dos solicitudes HTTP, Oracle real, un 201/un
    409/una fila; no mide carga ni demuestra escritores SQL externos. Mostrar
    OracleConcurrencyTest. Error: atribuir garantía distribuida a dos llamadas secuenciales.
14. **¿Está Azure integrado de verdad?** Hay adaptadores/configuración compilados y
    validadores de claims probados, pero no tenant ni login real. Mostrar identity.config,
    azure-identity, JwtSecurityConfig y JwtValidationTest. Error: equiparar dependencia
    instalada o token construido en test con firma empresarial comprobada.
15. **¿Un WAR acredita WebLogic?** No: aquí lo ejecuta Tomcat administrado por Boot;
    T015 requiere aclarar el runtime. Mostrar pom.xml, BookingApplication y plan.
    Error: confundir formato de archivo con compatibilidad Servlet/Jakarta/Java.

## 11. Retos de análisis, sin implementar

Cada ejercicio requiere una decisión de alcance antes de codificar; no existe como
funcionalidad adicional en esta entrega. Rutas Java relativas a
`backend/src/main/java/local/booking/`, Angular a `frontend/src/app/`; pruebas en sus
carpetas actuales. No se ofrecen estimaciones como mediciones reales.

### R1. Limitar duración a dos horas

Comportamiento: rechazar intervalos de más de dos horas con 400, también en recurrencia.
Regla/DTO: BookingRequest; servicio WeeklyRecurrence mantiene rangos; POST conserva
contrato. UI `reservas/bookings.component.ts/html` agrega ayuda/validación. SQL no
necesita tabla nueva; un CHECK opcional exigiría revisar datos antiguos antes de migrar.
Archivos: `booking/BookingRequest.java`, componente y BookingApiTest/WeeklyRecurrenceTest.
Riesgos: no sustituir control de propietario/CSRF ni sacar validación de transacción;
lock por espacio sigue igual. Casos: 1:59, 2:00, 2:00:01 y mismo instante con offsets distintos.
Secuencia mínima: acordar unidad/límite → tests → validación servidor → UI → Oracle/API.

### R2. Filtrar mis reservas por espacio

Comportamiento: GET `/api/bookings?spaceId=...` filtra manteniendo dueño/ACTIVE/fin futuro.
Regla/endpoint: BookingController/Service/Repository; DTO de respuesta sin cambio;
UI BookingApi y componente agregan filtro. SQL ajusta consulta, no esquema salvo índice
justificado por medición. Archivos: esos tres Java y `reservas/booking-api.service.ts`,
componente, BookingApiTest. Riesgo: olvidar owner al introducir otro método de consulta;
lectura no modifica locks de escritores. Casos: propio, ajeno, inexistente, sin filtro,
finalizada/cancelada. Secuencia: contrato → test dos usuarios → query → UI → regresión.

### R3. Prohibir reservas que empiezan en el pasado

Comportamiento: POST 400 si startsAt anterior al reloj del servidor; acordar igualdad
exacta y tratamiento de ocurrencias antes de aplicar. Regla en BookingService usando
Clock, DTO no confía en reloj cliente; UI orientación, SQL sin DDL necesario.
Archivos: `booking/BookingService.java`, `BookingApplication.java` (Clock existente),
BookingApiTest y componente. Riesgo: validación solo cliente o carrera con el tiempo
mientras espera lock; definir instante de evaluación. Propiedad y CSRF se conservan.
Casos: un instante antes/igual/después, offset distinto y espera de bloqueo.
Secuencia: fijar semántica → tests con Clock fijo → servicio → UI → regresión Oracle.

### R4. Añadir repetición diaria finita

Comportamiento: elegir DAILY o WEEKLY manteniendo límite y primera incluida.
DTO/POST agrega frecuencia validada; regla WeeklyRecurrence podría generalizarse con
nombre acorde; UI selector. SQL mantiene filas por ocurrencia, sin Serie por necesidad
actual. Archivos: `booking/BookingRequest.java`, `WeeklyRecurrence.java`, BookingService,
`reservas/booking-api.service.ts` y componente, WeeklyRecurrenceTest/BookingApiTest.
Riesgos: expansión sin límite, choques internos y cambios de zona; mismo lock durante
todo el pedido y mismo principal. Casos: mes/año, 1/12/0/13, frecuencia inválida,
intervalo mayor a un día y parcial no inicial. Secuencia: contrato → expansión/test →
servicio → UI → caso Oracle concurrente/rollback.

### R5. Exigir margen entre reservas

Comportamiento: por ejemplo 15 minutos libres entre usos, previa decisión sobre
ambos extremos. Regla cambia desigualdades de collisions y TimeRange; POST/DTO pueden
mantenerse; UI explica el margen. SQL ajusta query, esquema solo si se decide guardar
margen por espacio. Archivos: BookingRepository, TimeRange, Space si fuese configurable,
componente y ambas pruebas de frontera. Riesgos: asimetría entre reserva previa/nueva,
revelar ocupante o consultar fuera del lock; mismo bloqueo de Espacio. Casos: 14:59,
15:00, 15:01, reservas canceladas, distinto espacio y dos solicitudes simultáneas.
Secuencia: definir regla → tabla de bordes → tests Oracle/helper → query/UI → carrera.

### R6. Editar una reserva propia

Comportamiento: nuevo PUT/PATCH acordado para cambiar espacio/horario; hoy no existe.
DTO específico sin owner/status, controller/service y UI acción editar; SQL consulta
de choques excluye el id actual, sin asumir esquema nuevo. Archivos: BookingController,
BookingService, BookingRepository, BookingRequest o DTO nuevo justificado, BookingApi,
componente y BookingApiTest/OracleConcurrencyTest. Riesgos: autorización por recurso,
choque consigo misma y deadlock al bloquear espacio origen/destino; adquirir ambos
por id ordenado y validar de nuevo. Casos: propia/ajena, sin cambio, ocupada, cambio
simultáneo en sentidos opuestos y rollback técnico. Secuencia: contrato y política
canceladas → tests → locks ordenados/exclusión → endpoint → UI → pruebas concurrentes.

## 12. Límites y continuación

Primera gestión pendiente dentro del seguimiento existente: T015, aclarar con el
evaluador runtime y evidencia de despliegue. No se supone WebLogic compatible ni se
cambia unilateralmente el stack. T014 queda excluida; no recibir ni migrar un starter.
Las seis modificaciones anteriores solo evalúan entendimiento. Azure real, TLS y
operación pública requieren alcance/configuración independientes; no bloquean el MVP local.
No hay pruebas de carga, auditoría de seguridad integral Java ni certificación productiva.
El audit npm informa sobre el lockfile consultado, no sobre todo el sistema ni garantías futuras.

## Auditoría posterior — T017 / DEC-015

La auditoría posterior de la revisión47a6af4 matiza el cumplimiento: código local funcional, seis regresiones de contrato pendientes, dependencias y Dev Container por resolver; WebLogic no acreditado. Esta explicación conserva las preguntas/retos y no certifica por sí sola la entrega.

[Diagnóstico y evidencia](AUDITORIA_CUMPLIMIENTO_Y_SEGURIDAD.md) · [Plan de correcciones](PLAN_CORRECCIONES_AUDITORIA.md).

## Correcciones de auditoría sobre la solución real (DEC-017–021)

El monolito por funcionalidades y sus reglas no cambian. application.properties
rechaza coerción de float/string a enteros antes de BookingService; ApiErrors clasifica
excepciones MVC y conserva errores técnicos500/503 redactados. OpenApiConfig declara
esquemas/respuestas por operación y Basic+csrfToken como AND; SecurityConfig protege
Swagger con desafío Basic. EagerSecurityHeaders configura el filtro real para
cabeceras anticipadas; no sustituye un parche de versión para otros avisos.
Tomcat10.1.59 está en POM, árbol y WAR, incluso core/websocket provided. El bootstrap
Dev Container conserva secretos, y backend-artifact.sh vincula fuentes/WAR por hash.
Evidencia nueva: AuditSecurityTest y OracleConcurrencyTest dentro de una ejecución
completa88/88; frontend13/13/build; HTTP contra WAR e859c80. Véase cierre para límites,
verificación visual y avisos que continúan pendientes. La auditoría original conserva
sus seis fallos reales; no se reescribe como si hubieran pasado entonces.

### Preguntas respondidas para sustentación

1. ¿Por qué @Positive no detectaba1.75? Jackson lo truncaba a1 antes de Bean Validation.
   Rechazar ACCEPT_FLOAT_AS_INT evita perder la información; ALLOW_COERCION_OF_SCALARS
   también impide "1". BookingRequest sigue recibiendo enteros válidos, dates con offset
   y occurrences null/ausente=1. AuditSecurityTest comprueba400 sin nuevas filas.
2. ¿Por qué1.0 tampoco se acepta? El contrato requiere token JSON entero, no solo un
   valor matemáticamente integral. Se documenta igual para1e0; no redondear en servicio.
3. ¿Por qué GET /api/bookings/123 devuelve405? La ruta existe paraDELETE, pero GET no.
   ApiErrors conserva405 y Allow DELETE; una ruta desconocida devuelve404 y un ID no
   convertible400. La corrección del test se justificó primero en DEC-017.
4. ¿Una librería vulnerable equivale a un ataque explotable? No: versión/rango es una
   coincidencia. Hay que comprobar configuración efectiva y entrada alcanzable, incluso
   usos indirectos por framework. No basta buscar llamadas propias ni ignorar provided.
5. ¿Mitigación y actualización son lo mismo? No. eager headers aplica una alternativa
   oficial con efecto medido antes/después; Security6.3.10 sigue en el catálogo. Tomcat
   sí cambia a una versión corregida. Los demás avisos se analizan individualmente.
6. ¿Por qué Swagger exige dos autorizaciones? Basic identifica cada solicitud; CSRF
   relaciona la mutación con la sesión cuyo token se obtuvo por GET/api/csrf. La cookie
   sola no sustituye Basic y el token solo tampoco. Ambos esquemas en un mismo objeto
   OpenAPI significan AND. El mecanismo real y la política de sesión permanecen.
7. ¿Cómo se sabe qué WAR está corriendo? La construcción limpia registra commit de
   fuentes, hash, fecha y estado; start comprueba fuentes y artefacto. Un WAR anterior
   recuperado manualmente no valida el código corregido, aunque vuelva el servicio.
8. ¿Qué acredita Dev Container? CLI up/exec y compilación/pruebas en ese entorno.
   No prueba una instalación limpia del evaluador ni apertura visual de VS Code.

### Ejercicios de análisis, no funcionalidades implementadas

- Predecir qué cambia si solo se permite float-as-int manteniendo validación @Positive;
  identificar qué regresión vuelve a fallar y por qué una validación posterior no basta.
- Analizar el efecto de agregar GET porID al contrato:405 dejaría de ser correcto, y
  habría que diseñar autorización propia. No implementar esa ruta en esta fase.
- Evaluar activar caché compartida de recursos o JWT: qué estados "no aplicable" del
  informe T018 requieren reevaluación y qué nuevas pruebas serían necesarias.
- Explicar por qué reemplazar Cache-Control después del filtro eager no elimina otras
  cabeceras ya escritas; diseñar mentalmente una prueba que distinga antes/después.
