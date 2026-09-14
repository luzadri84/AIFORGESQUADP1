# Plan técnico para la prueba de Booking de Espacios Físicos

> Actualización de alcance — DEC-004 (2026-09-14): este documento conserva el
> registro/propuesta de su etapa original. Las instrucciones de esperar, recibir,
> comparar o migrar un starter e investigar su defecto quedaron superadas: el
> repositorio actual es la base definitiva. No aplica por cambio de alcance;
> no se acredita implementación de esas tareas. WebLogic sigue independiente.
> Reglas y estado vigentes: AGENTS.md, tasks.md y docs/BITACORA.md.

Fecha de preparación: 14 de septiembre de 2026. Estado: diseño propuesto antes de inspeccionar el starter y antes de implementar.

La recomendación es construir un monolito pequeño con Angular y Spring Boot, persistencia Oracle y un flujo completo para crear, consultar y cancelar reservas propias. La calidad debe concentrarse en la corrección de horarios, la autorización por recurso, las recurrencias parciales y la evidencia de verificación. La aplicación no necesita IA en tiempo de ejecución: lo obligatorio es utilizar IA durante el desarrollo y demostrar criterio al revisar su trabajo.

Este plan distingue requisitos de la prueba, decisiones propuestas y puntos pendientes de validación. No acredita código implementado, pruebas ejecutadas ni un defecto del starter corregido. El paquete incluye una guía de comprensión y defensa, instrucciones por fases para Codex y una propuesta de extensión de AGENTS.md.

## 1 Qué exige realmente la prueba

Se revisaron completos los tres adjuntos: el correo de invitación, AGENTS.md y la historia de usuario. El correo fija una dedicación estimada de 4 a 6 horas y una entrega hasta el 14 de septiembre de 2026 a medianoche, hora Colombia. Conviene entregar antes de finalizar ese día, sin depender de interpretaciones de medianoche.

El correo contiene literalmente un marcador para el enlace al repositorio starter. No se proporcionó una URL utilizable. Ese repositorio es necesario para inspeccionar el entorno reproducible, conservar su historial y encontrar el defecto sembrado. No debe inventarse un defecto para completar la bitácora ni atribuir a ese defecto las inconsistencias de los documentos.

### Matriz de evaluación y evidencia

| Dimensión | Peso | Evidencia que debe producir la implementación |
|---|---:|---|
| Resolución del problema | 15 % | Crear reservas válidas, rechazar colisiones y conservar ocurrencias válidas de una serie |
| Calidad y arquitectura | 15 % | Responsabilidades claras, contrato pequeño, transacciones correctas y stack justificado |
| Orquestación y verificación de IA | 25 % | Spec refinada antes del código, propuestas contrastadas, defecto investigado y decisiones comprobables |
| Seguridad del código generado | 10 % | Identidad del servidor, autorización por propietario, secretos externos y pruebas negativas |
| Pruebas y calidad | 10 % | Casos borde, recurrencia, integración, autorización y concurrencia |
| Documentación y bitácora | 10 % | Ejecución reproducible, decisiones reales y límites explícitos |
| Integración end to end | 15 % | Flujo funcional desde Angular hasta Oracle y regreso |

Orquestación de IA y seguridad tienen piso obligatorio de 3,0 sobre 5. Un promedio alto no compensa quedar por debajo. La ponderación final del proceso es Hard Skills 50 %, Soft Skills 25 % y Sustentación 25 %; se exige promedio de al menos 3,5 y el piso de las dimensiones críticas. Estas cifras proceden del correo, no son una predicción de la calificación.

### Alcance que se implementará

1. Identificación con HTTP Basic y dos usuarios de desarrollo, configurados fuera del repositorio.
2. Catálogo de espacios semilla.
3. Reserva individual y repetición semanal por un número acotado de ocurrencias.
4. Consulta de reservas propias activas y cancelación de una ocurrencia propia.
5. Detección de colisiones exactas, parciales y por contención, también ante solicitudes simultáneas.
6. API documentada, pantalla Angular funcional y pruebas de los comportamientos críticos.
7. Dependencias de MSAL Angular y OAuth2 Resource Server con puntos de integración preparados; el MVP inicia sin tenant ni conexión a Azure.
8. WAR, entorno reproducible, historial incremental y documentación de decisiones.

No construir notificaciones, administración de espacios, autenticación real con Azure, calendario con arrastrar y soltar, microservicios, colas, caché distribuida, motor RRULE completo, internacionalización ni asistente conversacional. Tampoco es necesario agregar una API de disponibilidad previa: la creación debe volver a comprobar siempre la disponibilidad.

AGENTS.md menciona autorización para modificar, pero la historia y la definición de terminado solo exigen crear, consultar y cancelar. Decisión propuesta: no agregar edición en el MVP; si el starter ya la trae, asegurar su autorización o retirarla del contrato documentando la decisión. Una futura edición debe respetar propiedad, colisiones y bloqueo.

## 2 Inconsistencias técnicas y cómo tratarlas

### WebLogic es un bloqueo de compatibilidad real

Spring Boot 3.3 requiere un contenedor Servlet 5.0 o superior. WebLogic 12.2.1.4 es Java EE 7 y pertenece a la generación anterior de APIs. Por ello, el WAR del stack solicitado no es directamente desplegable allí; cambiar solamente el empaquetado no resuelve el problema. Esta conclusión se obtiene al contrastar los [requisitos de Spring Boot 3.3](https://docs.spring.io/spring-boot/3.3/system-requirements.html) con la [compatibilidad documentada de WebLogic 12.2.1.4](https://docs.oracle.com/en/middleware/fusion-middleware/weblogic-server/12.2.1.4/intro/compatibility.html).

Acción: solicitar al evaluador que precise el runtime válido para demostrar el WAR o que corrija el servidor objetivo. Mientras responde, conservar Java 21, Boot 3 y WAR; avanzar en dominio, API, pruebas y frontend. Se puede proponer Tomcat 10.1 para la demostración local, pero debe registrarse como alternativa pendiente de aceptación, no como cumplimiento de WebLogic. Si el despliegue exacto en WebLogic es obligatorio, el cumplimiento completo queda bloqueado hasta resolver esta contradicción. No degradar a Boot 2 ni cambiar `jakarta.*` por `javax.*` a escondidas.

### Las dos ramas de Boot no administran las mismas versiones

Como base de mayor ajuste literal se propone Boot 3.3.13, salvo que el starter y una aclaración indiquen otra combinación. Su BOM administra Framework 6.1.21, Security 6.3.10 e Hibernate 6.5.3.Final. Boot 3.5 administra Security 6.5.x, que no coincide con el rango 6.3/6.4 escrito en la spec. No fijar versiones transitivas antiguas para aparentar compatibilidad. Consultas: [BOM de Boot 3.3](https://docs.spring.io/spring-boot/3.3/appendix/dependency-versions/coordinates.html) y [BOM de Boot 3.5](https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/coordinates.html).

Boot 3.3 con SpringDoc 2.6.x es una combinación recogida en la [matriz oficial de SpringDoc](https://springdoc.org/v2/#what-is-the-compatibility-matrix-of-springdoc-openapi-with-spring-boot). Usar inicialmente 2.6.0 y comprobar su resolución. Seleccionar una versión por ajuste a la prueba no demuestra ausencia de vulnerabilidades ni soporte vigente: revisar avisos y dependencias resueltas, y documentar cualquier conflicto con los rangos exigidos.

### MSAL Angular 3 requiere una comprobación específica

Microsoft documenta MSAL Angular 3 para Angular 15 a 18; esa página no acredita soporte para Angular 20. La metadata consultada de `@azure/msal-angular` 3.1.0 declara peers de RxJS 7 y MSAL Browser 3, sin una restricción Angular explícita. Esto permite distinguir ausencia de bloqueo de instalación de soporte oficialmente documentado. Se debe compilar y comprobar el adaptador en Angular 20; no afirmar incompatibilidad absoluta ni compatibilidad certificada solo por instalarlo. Fuentes: [guía de Microsoft](https://learn.microsoft.com/en-us/entra/msal/javascript/angular/v2-v3-upgrade-guide) y [metadata del paquete 3.1.0](https://registry.npmjs.org/@azure/msal-angular/3.1.0).

Mantener MSAL 3 en el plan, encapsulado y desactivado en modo Basic. Si la comprobación falla, registrar el error reproducible y pedir ajuste de versión. No usar `--force`, `--legacy-peer-deps`, `any` ni relajar el modo estricto para ocultarlo.

### Datos necesarios del starter

Solicitar enlace y acceso, confirmar Oracle disponible y versión, mecanismo de arranque del devcontainer y criterio de ejecución del WAR. No hace falta aclarar detalles cosméticos para comenzar. No enviar consultas en nombre de la candidata sin su indicación.

## 3 Base tecnológica propuesta

Primero se inspecciona el starter. Si su combinación cumple y funciona, conservarla reduce trabajo y deriva. Las siguientes son versiones candidatas verificadas en documentación o metadata pública, no un conjunto instalado y probado en esta tarea.

| Elemento | Base propuesta | Validación durante la implementación |
|---|---|---|
| Java | 21 | `java -version` dentro del devcontainer y compilación con release 21 |
| Backend | Spring Boot 3.3.13 | Resolver BOM, revisar árbol y ejecutar build |
| Seguridad y persistencia | Versiones administradas por Boot | Sin versiones manuales de Framework, Security, Data JPA o Hibernate |
| OpenAPI | SpringDoc webmvc-ui 2.6.0 | Resolución Maven y apertura real de documentación |
| JDBC | `com.oracle.database.jdbc:ojdbc11` | Resolver versión, comprobar soporte JDK y conexión con el Oracle suministrado |
| Angular core y CLI | 20.3.0 como base | Alinear paquetes framework y compiler; conservar parches compatibles del starter |
| Angular CDK | 20.2.0 como base | Comprobar peer de PrimeNG y build |
| PrimeNG | 20.0.0 como base | Configurar tema y peer dependencies verificados |
| Tailwind CSS | 3.4.17 | Configuración de v3, no instrucciones de v4 |
| RxJS | 7.8.2 | Flujos HTTP y estado simple |
| TypeScript | 5.9.2 para Angular 20.3 | `strict` y `strictTemplates` activos |
| MSAL | Angular 3.1.0 y Browser 3.28.1 | Adaptador compilable, Basic no activa llamadas Azure |
| Node | Línea 22 con parche compatible, al menos 22.12 | Fijar el parche o imagen exacta después de inspeccionar el entorno |
| Base de datos | Oracle 19c o superior | Usar servicio del starter; no sustituir la entrega por H2 |
| Empaquetado | WAR | Build y despliegue en runtime compatible acordado |

Angular 20.3 admite TypeScript desde 5.8 y menor a 6, y las líneas Node indicadas por su [tabla de compatibilidad](https://angular.dev/reference/versions). Se verificó la existencia de las versiones npm listadas mediante el registro; todavía falta probar la instalación conjunta y revisar parches de seguridad. PrimeNG 20.0.0 declara CDK `^20.0.3` y Angular `^20.0.4`, compatibles por rango con las candidatas. Fuentes de metadata: [Angular](https://registry.npmjs.org/@angular/core/20.3.0), [PrimeNG](https://registry.npmjs.org/primeng/20.0.0), [CDK](https://registry.npmjs.org/@angular/cdk/20.2.0).

Dependencias backend esperadas: starters web, validation, data-jpa, security, oauth2-resource-server; SpringDoc; driver Oracle; starter-test y spring-security-test. Reutilizar el mecanismo de migración del starter; en ausencia de uno, bastan scripts Oracle versionados de esquema y semillas con ejecución documentada. Evitar añadir Flyway, Liquibase, MapStruct y Lombok simultáneamente por comodidad del generador.

Registrar las versiones realmente resueltas en AGENTS.md, Maven Wrapper, `pom.xml`, lockfile npm y devcontainer. No dejar rangos `x` o `latest` en la configuración reproducible final.

## 4 Arquitectura mínima y responsabilidades

Un repositorio con `backend/`, `frontend/`, `.devcontainer/` y `docs/`. Mantener la organización del starter si es razonable. Backend organizado por funcionalidad: `booking`, `space`, `security` y un paquete pequeño para errores HTTP. Dentro de cada funcionalidad, separar los componentes cuando tengan una responsabilidad real.

| Componente propuesto | Responsabilidad | Qué no debe hacer |
|---|---|---|
| BookingController | Validar DTO, recibir principal y traducir respuesta HTTP | Consultar disponibilidad o decidir propietario desde el body |
| BookingService | Orquestar transacción, propietario, bloqueo, recurrencias y guardado | Manejar controles de Angular |
| TimeRange y RecurrenceExpander | Validación y generación de intervalos deterministas | Acceder a base de datos o HTTP |
| BookingRepository | Consultas parametrizadas de reservas y propiedad | Exponer consultas sin filtro a cualquier usuario |
| SpaceRepository | Leer espacios y bloquear una fila al escribir reservas | Administrar salas mediante CRUD no pedido |
| SecurityConfig | Basic, reglas de acceso, CSRF y modo de identidad | Duplicar reglas de negocio |
| DTO y mapper pequeño | Contrato explícito y conversiones sencillas | Serializar entidades JPA directamente |
| AuthService de Angular | Usuario actual y credencial temporal en memoria | Determinar autorización real |
| BookingApiService de Angular | Tipos y llamadas HTTP | Implementar otro motor autoritativo de colisiones |
| BookingPage | Formulario, lista y resultados por ocurrencia | Gestionar transacciones |

No añadir interfaces con una única implementación salvo una frontera concreta que lo justifique, como el proveedor de identidad. No usar repositorios genéricos encima de Spring Data, CQRS, event sourcing, buses de eventos ni una arquitectura hexagonal de muchos módulos. Inyección por constructor y funciones pequeñas son suficientes.

## 5 Modelo de datos y tiempo

| Entidad | Campos esenciales | Restricciones |
|---|---|---|
| Espacio | id, nombre, tipo, capacidad, sede | PK, campos obligatorios, capacidad positiva |
| Reserva | id, espacio_id, usuario_id, fecha_inicio, fecha_fin, estado | FK espacio, usuario obligatorio, inicio menor que fin, estado permitido |
| Usuario de desarrollo | id estable, nombre, dependencia, contraseña externa codificada al cargar | Configuración de seguridad, sin CRUD ni tabla de contraseñas |

Una fila de Reserva representa una ocurrencia concreta. No hace falta persistir la regla de recurrencia ni crear una entidad Serie para el alcance inicial: el servidor expande la solicitud y guarda las ocurrencias válidas. La cancelación afecta únicamente la fila indicada. El cliente muestra el resultado de la expansión recibida; no vuelve a calcularla para decidir qué se creó.

Identificadores Oracle mediante secuencia o la estrategia existente del starter. Estados de negocio `ACTIVE` y `CANCELLED`, persistidos como texto. Evitar nombres SQL reservados como `USER`. Índices iniciales: `(espacio_id, estado, fecha_inicio)` para detectar conflictos y `(usuario_id, estado, fecha_inicio)` para listar. Un índice ayuda al acceso pero no evita por sí mismo solapamientos.

API con fechas ISO 8601 y offset obligatorio, por ejemplo `2026-09-21T10:00:00-05:00`. Java recibe `OffsetDateTime`; compara instantes y genera recurrencias en `America/Bogota`. Persistir mediante un mapeo explícito a Oracle `TIMESTAMP WITH TIME ZONE`, verificando su round trip con Hibernate. Si el starter usa UTC normalizado, conservar esa alternativa solo con contrato y prueba explícitos; no mezclar convenciones.

En pantalla, rotular «Hora de Bogotá» y convertir la entrada de fecha y hora conforme a esa zona. No depender de la zona del navegador ni concatenar una `Z` a una hora local. La misma reserva debe verse con la misma hora de Bogotá al abrir la aplicación desde otro huso.

Decisiones de producto propuestas: «mis reservas activas» significa estado ACTIVE y fin posterior al instante actual, e incluye las que ya comenzaron. No agregar una prohibición de reservas pasadas o una duración máxima arbitraria porque no está en la prueba; documentar que solo se exige un rango cronológicamente válido. Si se decide prohibir el pasado tras aclaración, incorporar `Clock` inyectable y sus pruebas. El motor de colisiones considera todas las filas ACTIVE, sin depender del filtro temporal de la pantalla.

## 6 Motor de colisiones y concurrencia

### Regla exacta

Usar intervalos semiabiertos `[inicio, fin)`. Dos intervalos A y B se solapan cuando:

```text
A.inicio < B.fin  AND  B.inicio < A.fin
```

Solo hay conflicto si también coinciden el espacio y el estado activo. La comparación es estricta: terminar a las 11:00 permite que otra reserva empiece a las 11:00.

| Existente | Solicitada | Resultado |
|---|---|---|
| 10:00–11:00 | 10:30–11:30 | Conflicto al final |
| 10:00–11:00 | 09:30–10:30 | Conflicto al inicio |
| 10:00–11:00 | 10:00–11:00 | Conflicto exacto |
| 10:00–11:00 | 10:15–10:45 | Conflicto por contención |
| 10:00–11:00 | 09:00–12:00 | Conflicto envolvente |
| 10:00–11:00 | 11:00–12:00 | Permitida |
| 10:00–11:00 | 09:00–10:00 | Permitida |
| 10:00–11:00 | Mismo horario en otro espacio | Permitida |

Consulta conceptual, siempre parametrizada:

```sql
SELECT COUNT(*)
FROM reserva
WHERE espacio_id = :espacioId
  AND estado = 'ACTIVE'
  AND fecha_inicio < :finSolicitado
  AND fecha_fin > :inicioSolicitado
```

`BETWEEN` no expresa bien esta regla porque incluye extremos y puede omitir algunos casos de contención según cómo se combine. Una unicidad sobre espacio e inicio tampoco protege solapamientos parciales.

### Evitar que dos peticiones reserven a la vez

Consultar y después insertar sin exclusión mutua permite que dos transacciones vean el espacio libre. `@Transactional` por sí solo no resuelve esta carrera bajo el aislamiento habitual.

Decisión: bloquear con `PESSIMISTIC_WRITE` la fila del Espacio antes de consultar y mantener el bloqueo hasta finalizar la transacción. Esta fila siempre existe, aunque todavía no haya reservas. Bloquear únicamente resultados de la consulta de reservas deja desprotegido el caso vacío. Spring Data permite declarar bloqueo en métodos del repositorio; véase [Locking en Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html).

Secuencia propuesta:

1. Validar la estructura de la solicitud y generar una lista acotada de intervalos.
2. Entrar por un método público transaccional del servicio, invocado a través del bean Spring.
3. Cargar y bloquear Espacio; si no existe, devolver 404.
4. Recorrer ocurrencias en orden. Consultar conflictos en Oracle y contra las ocurrencias aceptadas del mismo pedido.
5. Acumular válidas y rechazos de negocio; persistir las válidas y confirmar.
6. Devolver resultado solo después de que la transacción haya concluido con éxito.

Todas las vías que escriban reservas de un espacio deben seguir el mismo protocolo, incluida cancelación y futura edición. La garantía cubre escrituras de esta aplicación; un script externo que lo omita puede violarla. El bloqueo por espacio reduce paralelismo incluso para horas distintas de la misma sala; es una compensación simple y adecuada para este volumen.

Usar el aislamiento READ_COMMITTED de Oracle y ejecutar una consulta nueva de colisiones después de adquirir el bloqueo; no reutilizar una consulta de disponibilidad obtenida antes. Comprobar que la configuración del starter no cambie inadvertidamente ese supuesto. Usar un tiempo de espera acotado si el driver lo soporta y comprobar su comportamiento. Un agotamiento de espera produce error temporal, por ejemplo 503 con código `BOOKING_BUSY`, no una falsa colisión de negocio. No reintentar automáticamente un POST en el cliente. El comportamiento concreto de bloqueo y timeout debe probarse con Oracle, no inferirse de mocks.

## 7 Recurrencias con aceptación parcial

Propuesta de alcance: sin repetición o frecuencia semanal sobre el día de la primera fecha. `count` cuenta la primera ocurrencia y acepta de 1 a 12. El límite 12 es una protección técnica propuesta para acotar trabajo y bloqueo, no un requisito del evaluador. Un pedido sin recurrencia genera una ocurrencia. No soportar múltiples días por semana, excepciones ni RRULE libre.

Para el ejemplo «todos los lunes por cuatro semanas», la primera fecha debe ser lunes; las siguientes se obtienen sumando semanas en la zona de Bogotá. Se preserva la duración del intervalo. Si una reserva larga provoca solapamiento entre ocurrencias de la propia solicitud, conservar la primera válida y marcar posteriores conflictos mediante la misma regla.

La historia exige no bloquear las ocurrencias válidas por un conflicto de otra. Elegir aceptación parcial: crear las libres e informar cada rechazada. Si cuatro ocurrencias tienen una colisión, se crean tres. No responder un error genérico que esconda que hubo inserciones.

Es posible usar una sola transacción y aceptar parcialmente: las colisiones se clasifican como resultados de negocio antes de guardar; no se provocan excepciones de persistencia para controlarlas. Si falla Oracle, hay un error inesperado o no se confirma el commit, se revierte todo lo aceptado en esa solicitud y se devuelve error técnico. No capturar excepciones de base de datos dentro de la transacción para continuar con un contexto marcado para rollback.

Propuesta de respuesta uniforme:

```json
{
  "created": [
    {"occurrence": 1, "id": 101,
     "start": "2026-09-21T10:00:00-05:00",
     "end": "2026-09-21T11:00:00-05:00"}
  ],
  "rejected": [
    {"occurrence": 2,
     "start": "2026-09-28T10:00:00-05:00",
     "end": "2026-09-28T11:00:00-05:00",
     "code": "TIME_CONFLICT"}
  ]
}
```

Los números son ilustrativos. No incluir el nombre, identidad ni ID de la reserva ajena que ocupa el espacio. Informar únicamente la ocurrencia solicitada que resultó rechazada.

## 8 Contrato de API propuesto

Usar los nombres del starter si ya existe un contrato razonable; en caso contrario, adoptar el siguiente. No mantener simultáneamente versiones españolas e inglesas de cada campo.

| Método y ruta | Entrada o filtro | Respuesta |
|---|---|---|
| GET `/api/csrf` | Inicialización del cliente | 200 y token CSRF materializado |
| GET `/api/me` | HTTP Basic | 200 con id, nombre y dependencia del autenticado |
| GET `/api/spaces` | Usuario autenticado | 200 con espacios semilla |
| POST `/api/bookings` | espacio, inicio, fin y recurrencia opcional | 201 si todas se crean; 200 si parcial; 409 si ninguna por colisión |
| GET `/api/bookings` | Sin parámetro de usuario | 200 con lista propia activa, ordenada por inicio e id |
| DELETE `/api/bookings/{id}` | Reserva propia | 204; 404 si no existe o pertenece a otro |

El endpoint CSRF puede permitirse sin autenticación porque no expone datos de negocio. Las demás rutas de negocio requieren identidad. API auxiliar `/me` evita un sistema de login con tokens propio: verifica las credenciales Basic y devuelve la identidad. La lista de reservas puede ser un array sin paginación en este MVP; documentar su límite de escala sin incorporar paginación prematuramente.

DTO de creación:

```json
{
  "spaceId": 1,
  "start": "2026-09-21T10:00:00-05:00",
  "end": "2026-09-21T11:00:00-05:00",
  "recurrence": {"frequency": "WEEKLY", "count": 4}
}
```

No aceptar `userId`, `state`, `id` ni campos de propietario en el DTO de escritura. El servidor deriva usuario y estado. Rechazar campos desconocidos en este contrato de entrada para detectar intentos y equivocaciones; probar esa configuración sin ampliar innecesariamente el comportamiento de otras integraciones.

Errores: 400 para JSON, tipos, offset, campos o rango inválidos; 401 para falta de credenciales o credenciales inválidas en recursos protegidos; 403 para CSRF ausente o inválido en operaciones de escritura; 404 para espacio inexistente o reserva inexistente/ajena; 409 para cero ocurrencias aceptadas por colisión; 503 para indisponibilidad temporal identificada. Según el orden de filtros, una escritura anónima sin CSRF puede recibir 403 antes de llegar a autenticación: documentar y probar el caso con token válido para comprobar el 401.

Formato `ProblemDetail` para errores, con código estable y errores de campos cuando aplique. El 409 puede añadir `created: []` y `rejected` como extensiones; el frontend debe conocer esa variante. Para 200/201 usar el resultado normal. Swagger debe mostrar ejemplos de éxito parcial y autenticación, además del esquema de datos.

Cancelación lógica e idempotente: buscar por id y propietario sin filtrar por estado; si es propia y ya CANCELLED, devolver 204. Si es ajena, devolver 404. Cambiar estado bajo el protocolo de bloqueo del espacio. No implementar cancelación de toda la serie sin petición.

## 9 Seguridad suficiente y verificable

### Identidad y autorización

Basic debe validarse en Spring Security con dos usuarios distinguibles. Las contraseñas de desarrollo se obtienen de variables o archivos locales ignorados y se codifican con un PasswordEncoder. No incluir contraseñas predeterminadas en Java, README, `.env.example`, tests persistidos o historial. Para pruebas, usar autenticación simulada de Spring Security cuando no se pruebe Basic y generar una contraseña efímera al probar Basic real.

El frontend guarda la credencial solo en memoria y la elimina al cerrar sesión o recargar. No usar localStorage, sessionStorage ni cookies para guardarla. Base64 no cifra: fuera de un circuito local de desarrollo, usar HTTPS. Un interceptor adjunta Authorization exclusivamente a rutas relativas de esta API; probar que nunca lo adjunta a una URL externa.

El propietario procede del principal autenticado, nunca de un selector de usuario ni de datos confiados al cliente. La consulta de lista incluye propietario en SQL; la cancelación busca por id y propietario. Ocultar botones o proteger una ruta Angular solo mejora la experiencia, no autoriza el recurso.

### CSRF y origen

Mantener CSRF activo. HTTP Basic puede ser vulnerable a CSRF en navegadores, incluso si el servidor no guarda sesión; así lo explica [Spring Security](https://docs.spring.io/spring-security/reference/features/exploits/csrf.html). Proponer CookieCsrfTokenRepository, `XSRF-TOKEN` y `X-XSRF-TOKEN` con el tratamiento para SPA correspondiente a la versión 6.3. El token CSRF puede ser legible por JavaScript; no es la contraseña. Materializarlo mediante GET `/api/csrf` y renovarlo cuando la configuración de autenticación lo requiera.

Angular llama a `/api` con un proxy de desarrollo al backend, conservando mismo origen y su mecanismo XSRF. Verificar expresamente generación del token diferido y validación de la cabecera; no copiar una API disponible solo en una versión posterior. Consultar la [configuración CSRF de Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html) y contrastarla con la versión usada. Probar POST y DELETE con token válido y sin él. Documentar también cómo usar CSRF desde Swagger o el cliente HTTP de la demo.

No habilitar CORS `*` con credenciales. Si el entorno exige orígenes separados, permitir únicamente el origen exacto y adaptar el envío de CSRF, con prueba real. Evitar convertir un problema del proxy en una desactivación general de seguridad.

### Punto de extensión para Azure

En Angular, separar Basic de un adaptador MSAL y su factoría de configuración. El adaptador debe referenciar tipos y proveedores reales y compilar; instalar el paquete y dejar un comentario TODO no demuestra integración. El modo Basic no registra interceptores que lancen autenticación Azure ni requiere tenant. Una prueba comprueba selección del modo y ausencia de llamadas externas. Un tenant real y el flujo de tokens quedan fuera del MVP.

En backend, incluir el starter OAuth2 Resource Server y separar configuración Basic de la futura configuración JWT. Esta última debe quedar condicionada a un modo explícito y validar configuración requerida al activarse. Mapear una identidad estable; para Entra, un identificador basado en tenant y objeto evita depender del correo mutable. No aceptar JWT sin verificar firma, issuer, expiración y audiencia cuando se habilite en el futuro. No declarar validado ese flujo sin proveedor real o pruebas específicas.

### Revisión final de seguridad

Revisar autorización, consultas parametrizadas, validación de campos, salida de errores sin stack traces, datos ajenos en conflictos y secretos en archivos e historial. Usar el análisis de dependencias disponible en el starter y revisar los hallazgos por aplicabilidad. `npm audit` cubre el árbol npm, no el backend; un árbol Maven resuelto acredita existencia, no seguridad. Registrar comandos, resultados y limitaciones. No usar `npm audit fix --force` como corrección automática de un stack fijado.

## 10 Frontend funcional mínimo

Una pantalla de acceso local y una pantalla de reservas. Esta última incluye identidad actual, selector de espacio con sede y capacidad, inicio y fin en hora Bogotá, repetición semanal opcional, botón Reservar, resultado por ocurrencia y tabla de reservas propias con Cancelar.

Usar componentes standalone, formularios reactivos tipados, PrimeNG para controles y tabla, Tailwind 3.4 para espaciado y adaptación, y RxJS para HTTP y estados de carga. CDK queda instalado como parte del stack y puede utilizarse donde haga falta accesibilidad; no inventar drag and drop para justificarlo. Mantener `strict`, `strictTemplates` y las verificaciones existentes.

Deshabilitar envío mientras hay una solicitud pendiente. Mostrar errores junto a los campos. Diferenciar claramente «se crearon todas», «se crearon algunas» y «no se creó ninguna». Después de crear parcialmente o cancelar, recargar la lista desde la API. Mantener el resultado parcial visible para que el usuario sepa qué ocurrió.

En una respuesta técnica fallida no asumir que se crearon reservas; si se pierde la respuesta por red, consultar la lista antes de reintentar. No incorporar un sistema completo de idempotency keys en este alcance. Cubrir al menos estados de carga, vacío, error de autenticación, colisión, resultado parcial y confirmación de cancelación; etiquetas accesibles, navegación por teclado y botones inequívocos.

## 11 Plan de pruebas centrado en riesgos

No buscar un porcentaje arbitrario ni probar getters. Los siguientes escenarios aportan evidencia observable. Los tests de integración deben ejecutarse en el Oracle del entorno; H2 no demuestra su bloqueo, dialecto ni tipos temporales.

| ID | Nivel | Escenario y resultado esperado |
|---|---|---|
| T01 | Unidad parametrizada | Parcial al inicio y al final producen conflicto |
| T02 | Unidad parametrizada | Igualdad y ambas contenciones producen conflicto |
| T03 | Unidad parametrizada | Adyacencia a ambos lados y separación permiten reservar |
| T04 | Unidad y validación API | Inicio igual o posterior a fin, null y offset ausente producen error |
| T05 | Integración Oracle | Mismo horario en otro espacio permitido; CANCELLED no bloquea |
| T06 | Unidad | Semanal con count 4 produce cuatro fechas, incluye la primera y respeta hora Bogotá |
| T07 | Integración Oracle | Solo la segunda de cuatro colisiona: tres filas nuevas y rechazo identificado |
| T08 | Integración Oracle | Todas colisionan: ninguna fila nueva y 409 |
| T09 | Validación y unidad | count 0, superior al límite o frecuencia no soportada: 400; conflicto interno detectado |
| T10 | API y seguridad | GET protegido sin Basic o con Basic inválido: 401; Basic válido representa identidad real |
| T11 | API y persistencia | A no ve reservas de B; intento de aportar userId o estado no altera propietario |
| T12 | API y persistencia | A cancela suya; B intenta cancelar la de A: 404 y estado intacto |
| T13 | API y persistencia | Repetir cancelación propia: 204; vuelve a poder reservarse ese horario |
| T14 | API | Espacio inexistente: 404, sin escrituras |
| T15 | Integración Oracle concurrente | Dos transacciones independientes intentan mismo rango en espacio inicialmente vacío: una respuesta 201, otra 409 y una reserva activa |
| T16 | Integración Oracle | Error técnico al guardar lote: rollback de las filas de ese pedido |
| T17 | API y cliente | POST y DELETE sin CSRF: 403; con Basic y token válidos funcionan |
| T18 | Frontend | Interceptor no envía Basic a origen ajeno; cerrar sesión limpia estado |
| T19 | Frontend y API | Respuesta parcial muestra creadas y rechazadas y actualiza lista |
| T20 | Recorrido completo | Entrar, seleccionar espacio, reservar, ver, provocar conflicto y cancelar desde Angular |
| T21 | Tiempo y Oracle | Offset equivalente representa mismo instante; persistencia y visualización preservan hora Bogotá |

T15 necesita sincronización controlada de dos hilos/conexiones, transacciones separadas y verificación final en base de datos. No envolver toda la prueba en la misma transacción del test ni sincronizar ambas después de adquirir el bloqueo, porque la segunda nunca llegaría a esa barrera. Usar inicio coordinado y, si hace falta, una señal que confirme que la primera mantiene el bloqueo antes de lanzar la segunda. Evitar `sleep` como garantía de carrera.

Elegir el runner que ya traiga el starter. Unitarias Java con JUnit; integración API con MockMvc o cliente HTTP según base existente; prueba de navegador con herramienta existente. Si no hay automatización de navegador y el tiempo no permite incorporarla, registrar la demo manual completa como manual y conservar pruebas automáticas de API y componentes. No etiquetar un test de mocks como end to end.

## 12 Secuencia de trabajo de seis horas

Estimación condicionada a un starter utilizable y Oracle accesible. La preparación de este plan no equivale al tiempo real de implementación. Si el entorno falla, registrar tiempo y bloqueo; no reducir silenciosamente los criterios esenciales.

| Fase | Minutos | Trabajo | Evidencia de salida |
|---|---:|---|---|
| 0 | 0–30 | Inspección del starter, ejecución base, dependencias y defecto sembrado | Estado inicial, reproducción o hipótesis claramente rotulada, bloqueos |
| 1 | 30–50 | Refinar spec, contrato, decisiones y casos borde | AGENTS extendido y primer commit real de diseño |
| 2 | 50–105 | Datos, Basic, CSRF y flujo individual | API individual autenticada con validación y propiedad |
| 3 | 105–155 | Motor, bloqueo, recurrencias y cancelación | Tests esenciales y resultados por ocurrencia |
| 4 | 155–215 | Angular completo y adaptación de identidad | Crear, listar, parcial y cancelar por la pantalla |
| 5 | 215–285 | Pruebas integradas, concurrencia, seguridad y regresión del defecto | Evidencia de Oracle y autorización real |
| 6 | 285–325 | Revisar simplicidad, build y WAR; documentación | Comandos reproducibles y limitaciones explícitas |
| 7 | 325–360 | Demo en limpio, ensayo de explicación y entrega | Git coherente, artefacto completo y checklist |

Los tiempos son orientación, no autorización para posponer pruebas hasta el final: cada fase prueba su cambio; la fase 5 integra y cierra riesgos pendientes. Para acercarse a cuatro horas, reutilizar UI, entorno y herramientas del starter; eliminar ornamentación, no recurrencias, autorización, Oracle ni comprobaciones esenciales.

Commits posibles: decisiones de alcance; reproducción y corrección del defecto; dominio y colisiones; API y seguridad; recurrencias y concurrencia; interfaz; integración y documentación. El orden exacto lo impone el trabajo real. No crear retrospectivamente un historial falso ni cambiar fechas. Un arreglo del defecto debe tener una prueba que falle antes y pase después cuando el defecto sea comprobable mediante test.

## 13 Cómo documentar trabajo con IA

La bitácora registra decisiones observadas. Formato por entrada: problema, propuesta de IA, evaluación humana, aceptación o rechazo y motivo, verificación ejecutada, resultado, limitación y commit relacionado. Las sugerencias de este paquete son decisiones propuestas, no experiencias vividas por la candidata.

Ejemplos de temas que vale la pena registrar si efectivamente se presentan: rechazo a desactivar CSRF; rechazo a recibir propietario desde Angular; selección de aceptación parcial; detección de incompatibilidad de despliegue; rechazo a `--force`; cambio de consulta sin bloqueo a bloqueo de Espacio. No copiar estos ejemplos como si ya hubieran sucedido.

README final: propósito y alcance, versiones resueltas, precondiciones, variables sin secretos, pasos devcontainer, inicialización Oracle, arranque backend/frontend, prueba y build, autenticación de desarrollo, ejemplos API, decisiones, defecto corregido y límites. Añadir el estado real de WebLogic y MSAL. AGENTS contiene reglas ejecutables y concisas; la explicación extensa va en documentación, no en el contexto permanente del agente.

## 14 Definición de terminado y entrega

- [ ] Starter inspeccionado y su historial preservado.
- [ ] Defecto sembrado identificado con evidencia, explicado y corregido, o bloqueo declarado honestamente.
- [ ] Matriz de versiones resueltas, instalación sin forzar peers y compilación strict.
- [ ] Oracle real conectado, scripts reproducibles y datos semilla de espacios.
- [ ] Crear, listar propias y cancelar funcionan desde Angular.
- [ ] Colisiones y recurrencias parciales cumplen la historia.
- [ ] Propiedad, CSRF, credenciales y concurrencia comprobados.
- [ ] MSAL/OAuth2 preparados en código, Basic independiente de Azure y límites documentados.
- [ ] Pruebas realmente ejecutadas, fallos corregidos y no ejecutadas identificadas.
- [ ] WAR generado; runtime de demostración documentado; WebLogic no se declara validado sin evidencia.
- [ ] README, AGENTS extendido y bitácora coherentes con el código final.
- [ ] Commits incrementales reales y revisión de secretos en archivos e historial.
- [ ] Demo de arranque en limpio y explicación ensayada.

Se puede entregar repositorio privado con acceso al evaluador o ZIP con historial completo según el correo. Para ZIP, incluir `.git` y las fuentes, excluyendo `.env`, credenciales, node_modules, caches y artefactos temporales. No usar `git archive` como único mecanismo porque no incorpora el historial. Si `.git` es un archivo que apunta a un worktree, no basta con copiarlo: preparar un clon autocontenido y comprobarlo. Revisar `git log` y `git fsck` en una extracción de prueba. Agregar colaboradores o enviar el correo solo cuando la candidata lo indique.

## 15 Qué queda pendiente y qué ya está listo

Listo: análisis de los adjuntos, trazabilidad de criterios, arquitectura propuesta, contrato, algoritmo, seguridad, concurrencia, recurrencias, pruebas, cronograma, instrucciones para Codex y material de comprensión.

Pendiente: obtener el starter, inspeccionar su defecto y entorno, fijar las versiones finales, resolver el criterio de despliegue, compilar el adaptador MSAL en Angular 20, implementar y ejecutar las pruebas. Las respuestas de sustentación deben actualizarse contra el código real; no debe afirmarse que la solución usa una decisión de este plan si luego se implementó otra.
