# Explicación de la implementación real

Actualizado después de T003, 2026-09-14. Fuente de estado:
[tasks.md](../specs/001-booking-espacios/tasks.md). Este documento vivo explica la
base existente; no acredita la entrega T013 ni transforma ejercicios en trabajo.

## 1. Estado y alcance

Existe infraestructura restaurada, Spec Kit/.handoffs y backend Maven WAR con
persistencia Oracle para espacios/reservas. Hay tres espacios semilla de desarrollo
y cero reservas al terminar las pruebas. No existe API de reservas, UI Booking,
autenticación local configurada, colisiones ni recurrencias. El starter no llegará:
base actual definitiva por DEC-004; no migración ni defecto sembrado equivalente.
WebLogic conserva su evaluación independiente T015.

## 2. Ejecución reproducible

[README](../README.md) contiene inicio/parada con ambas configuraciones Compose y
`--no-build`, usando las imágenes y credenciales importadas. Java 21.0.10, Maven
3.9.9, Boot 3.3.13, Data JPA 3.3.13, Hibernate 6.5.3.Final, ojdbc11 21.9.0.0 y
Oracle 23.26.3: ver [POM backend](../backend/pom.xml) y versiones completas en
[plan](../specs/001-booking-espacios/plan.md). Las herramientas Angular siguen en
la sonda de infraestructura, no constituyen un frontend funcional.

```powershell
pwsh -NoProfile -File scripts/booking-db.ps1 status
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash mvnw -B -ntp -f backend/pom.xml verify
```

Produce `backend/target/booking.war`. Esquema/semillas ya aplicados en este equipo;
no repetir schema. [Informe T003](VERIFICACION_T003.md) documenta la preparación de
una base nueva y su protección de conflictos. Los puertos del contenedor dev están
reservados en localhost 4200/8080; aún no hay URL funcional de Booking comprobada.
El WAR no se desplegó ni se ejecuta con `java -jar`.

## 3. Arquitectura concreta

El usuario decidió monolito por funcionalidades (DEC-005). Lo ya construido:

| Archivo/paquete | Responsabilidad real |
|---|---|
| [BookingApplication](../backend/src/main/java/local/booking/BookingApplication.java) | Entrada Spring y SpringBootServletInitializer para WAR |
| [space/Space](../backend/src/main/java/local/booking/space/Space.java) | Mapeo de espacio, tipo/capacidad/sede |
| [SpaceRepository](../backend/src/main/java/local/booking/space/SpaceRepository.java) | Acceso JPA a espacios |
| [booking/Booking](../backend/src/main/java/local/booking/booking/Booking.java) | Mapeo de reserva, referencia a Space, propietario, instantes/estado |
| [BookingRepository](../backend/src/main/java/local/booking/booking/BookingRepository.java) | Acceso JPA a reservas; sin operaciones de negocio |
| [application.properties](../backend/src/main/resources/application.properties) | Conexión mediante entorno/secretos y validación del esquema |
| [V001](../backend/src/main/resources/db/oracle/V001__booking_schema.sql) | Esquema inicial aditivo, secuencias, índices y restricciones |
| [semillas](../backend/src/main/resources/db/oracle/R__development_spaces.sql) | Catálogo mínimo de desarrollo, MERGE solo de ausentes |

`booking` depende de `space` por la relación de persistencia. No se introdujeron
interfaces genéricas propias ni capas de mapeo para un segundo almacén inexistente.
Controlador–servicio–repositorio dentro de booking es la organización acordada para
el siguiente código; hoy solo existe su persistencia. `security`, `errors` compartido
y Angular `acceso`/`reservas` se crearán cuando sus tareas los necesiten.
Alternativa discutida: puertos/adaptadores; ver DEC-003/005/006 sin atribuir al usuario
un rechazo que no expresó. SQL explícito evita añadir un motor de migraciones para
una sola V001; obliga a revisar manualmente futuras evoluciones y fallos parciales.

## 4. Recorrido existente

El recorrido ejecutado es el de [OraclePersistenceTest](../backend/src/test/java/local/booking/booking/OraclePersistenceTest.java):
`persistsExactInstantAndRelationship` obtiene la referencia del espacio 1, construye
Booking, llama `saveAndFlush`, limpia el contexto JPA y vuelve a cargar con `findById`.
Así fuerza escritura/lectura Oracle y compara instantes, relación, propietario y
estado. La transacción de prueba termina en rollback. No simula un request HTTP ni
una reserva aceptada por reglas de negocio.

Creación desde Angular, listado propio, cancelación y recurrencias todavía no tienen
recorrido implementado; sus contratos propuestos están en spec/plan, no en esta base.

## 5. Reglas comprobadas y pendientes

V001 exige espacio existente, propietario no nulo, estado ACTIVE/CANCELLED y
`STARTS_AT < ENDS_AT`; espacios con tipo válido y capacidad positiva. Los enums
[BookingStatus](../backend/src/main/java/local/booking/booking/BookingStatus.java) y
[SpaceType](../backend/src/main/java/local/booking/space/SpaceType.java) se guardan
como texto. IDs semilla 1–3; secuencia de espacios inicia en 100.

OffsetDateTime y `@TimeZoneStorage(NATIVE)` se mapean a TIMESTAMP(9) WITH TIME ZONE.
Los tests comparan `toInstant()` con tres offsets y precisión de nanosegundos.
No prueban reglas de horario local ni identificadores regionales de zona.
El CHECK de rango solo valida cada fila: no evita que dos filas se solapen.
Adyacencias, cancelación, propiedad y expansión semanal siguen pendientes.

## 6. Seguridad y concurrencia

El backend obtiene contraseña de `/run/secrets/` por configtree, sin literal en
código. [booking-db.ps1](../scripts/booking-db.ps1) lee el secreto dentro de Oracle
y lo pasa por stdin a SQL*Plus. `.local`, `.env` y PEM están ignorados por Git.
Compose publica solo localhost y Oracle no publica puerto.

Spring Security es dependencia del WAR, pero no hay configuración de dos usuarios,
endpoint de identidad ni prueba de Basic/CSRF. No hay adaptación Azure verificada.
La columna OWNER_ID no autoriza por sí misma: falta derivarla del principal y
comprobar acceso. Los repositorios no implementan bloqueo de espacio ni carrera
concurrente. Las pruebas transaccionales aíslan sus datos, no acreditan T011.

## 7. Evidencia de pruebas

| Requisito/parcial | Evidencia real | Límite |
|---|---|---|
| FR002 catálogo | seed 3 filas, luego 0; repeatedSeedsPreserveRowsAndExistingEdits | Sin consulta HTTP |
| FR009 Oracle/WAR | connectsAsBookingInFreepdb1WithValidatedMapping, Maven verify | Sin despliegue ni compatibilidad WebLogic |
| Base de FR003 | rejectsMissingSpace, rejectsZeroOrNegativeRange, requiresOwner y checks de enums/capacidad | Sin validación DTO, propiedad ni errores HTTP |
| Tiempo persistido | persistsExactInstantAndRelationship, 3 casos | No prueba recurrencias/concurrencia |
| FR011 evidencia | 12 pruebas Oracle, rollback y [informe T003](VERIFICACION_T003.md) | Sin pruebas completas del MVP |
| Respaldo original | [jdbc-check.sh](../scripts/jdbc-check.sh) read | No comprueba reservas |

Primera ejecución: seis aserciones fallidas por interpretar mal la cadena de
excepciones del driver. Tras corregir el helper: 12 pruebas correctas, 0 fallos,
0 errores, 0 omitidas. Se registró el fallo real, no se ocultó ni se atribuyó al
starter. `verify-local.sh`/sonda Angular permanecen verificados en su contexto
histórico; no fueron reinstalados ni repetidos para este cambio.

## 8. Proceso con IA y decisiones

La [bitácora](BITACORA.md) conserva registros originales y nuevas decisiones:
DEC-001/002 integración, DEC-003 propuesta, DEC-004 base definitiva por usuario,
DEC-005 organización por funcionalidades por usuario, DEC-006 mapeo/SQL/pruebas
resueltos por Codex dentro de T003. No se atribuye al usuario la elección de cada
columna o aserción. Las verificaciones aquí indicadas fueron ejecutadas por Codex.

Commits reales anteriores: b977ca3 (integración técnica), 8ba745a (planificación),
75af194 (cambio documental de alcance y arquitectura, antes del código T003).
El commit de T003 se identifica por T003/DEC-006 en `git log`; no se introduce un
hash futuro ni se reescriben commits anteriores para aparentar otra secuencia.

## 9. Base y procedencia

Repositorio actual, credenciales, imágenes y marcador proceden de la restauración
verificada. Esquema BKG_, entidades y pruebas son trabajo nuevo de T003. Solo existía
ENVIRONMENT_PROBE antes de esa tarea. El material de apoyo se fusionó selectivamente
en H001; no se copió overlay encima. Starter/recepción/migración/defecto: **No aplica
por cambio de alcance**, conservado históricamente en T014, sin marcar implementado.
Los problemas observados aquí no se presentan como aquel defecto.

## 10. Preguntas de sustentación respondidas

1. **¿Qué implementa hoy booking?** Entidad, enum y repositorio; no servicio HTTP.
   Mostrar Booking/BookingRepository. Error frecuente: confundir persistencia con
   un caso de uso autenticado y transaccional.
2. **¿Qué cambia al organizar por funcionalidades?** Las piezas de reservas quedan
   próximas y separadas de space; mantienen JPA y el mismo runtime. Mostrar árbol
   backend y DEC-005. No significa microservicios ni bases separadas.
3. **¿Por qué WAR y no un JAR ejecutable?** Es el formato solicitado; pom packaging=war
   y BookingApplication sirven para inicialización Servlet. Empaquetarlo no acredita
   despliegue ni resuelve incompatibilidad WebLogic.
4. **¿Qué evita perder el instante?** Booking usa OffsetDateTime/NATIVE y V001 precisión
   9; el test limpia el contexto y compara toInstant tras leer Oracle. No sustituir
   por LocalDateTime suponiendo que siempre se conoce el offset.
5. **¿Por qué limpiar EntityManager?** Evita que la aserción lea la misma entidad
   gestionada sin volver a cargarla. Mostrar persistsExactInstantAndRelationship.
   Un getter inmediatamente después de save no demuestra round trip.
6. **¿Por qué ddl-auto=validate?** Contrasta el mapeo con tablas existentes sin crearlas
   ni borrarlas; SQL es explícito. Mostrar properties y V001. Validate no migra ni
   demuestra por sí solo todos los CHECK, por eso se prueban inserciones inválidas.
7. **¿El rango CHECK impide colisiones?** No; solo compara inicio/fin de una fila.
   Mostrar BKG_BOOKING_RANGE_CK y test de rango. Solapamiento entre reservas requiere
   la regla y protocolo concurrente pendientes, no un CHECK adicional ingenuo.
8. **¿Cómo evita la semilla sobrescribir cambios?** MERGE solo WHEN NOT MATCHED;
   repeatedSeedsPreserveRowsAndExistingEdits modifica un nombre y aplica seed dos
   veces dentro de la prueba. Idempotencia no significa actualizar catálogo oficial.
9. **¿Cómo se preserva el esquema anterior?** V001 agrega BKG_ tras inspección y
   rechaza objetos destino existentes; ENVIRONMENT_PROBE permanece. Mostrar guard y
   booking-db.ps1. No confundir rollback DML con reversión del DDL de Oracle.
10. **¿Por qué cero reservas tras las pruebas?** DataJpaTest revierte la transacción;
    las secuencias sí avanzan. Mostrar anotaciones y status final. IDs con huecos no
    implican reservas perdidas ni justifican reiniciar secuencias.
11. **¿Cómo se conserva el respaldo?** Se lee JdbcCheck en modo read, compara token
    y se contrastan archivos de credenciales sin mostrarlos. Mostrar rama read en
    [JdbcCheck](../infra/checks/JdbcCheck.java). Write reemplazaría la evidencia.
12. **¿Cómo se comprobó la restricción Oracle?** El helper assertOracleError exige
    DataIntegrityViolationException y encuentra SQLException para validar código
    2290/2291/1400. Mostrar helper. La causa más profunda del driver no tiene por
    qué ser SQLException; ignorar la excepción tampoco sería una prueba válida.
13. **¿OWNER_ID garantiza autorización?** No: es dato requerido, aún no identidad
    verificada. Mostrar Booking y T004/T007. El futuro controlador debe obtener
    identidad del principal, no aceptar un propietario arbitrario del body.

## 11. Ejercicios de análisis, sin implementar

Son cambios hipotéticos para explicar consecuencias sobre código existente. No
se aplicaron. Deben adaptarse a la API/UI reales cuando existan y al cerrar T013.
No se infiere que el evaluador vaya a pedirlos ni se estima tiempo medido.

1. **Ampliar nombre de espacio de 120 a 180 caracteres.** Cambiaría el máximo
   persistible. Afecta Space y nueva migración aditiva SQL, no reescribir V001 aplicada;
   aún no hay DTO/endpoint/UI. Probar 120/180/181 con Unicode y mapeo real. No cambia
   propietario; prever bloqueo DDL al ampliar con conexiones activas. Secuencia:
   inspeccionar columna/datos, diseñar migración, ajustar mapeo, probar en copia
   aislada y revisar impacto; no tocar este esquema como ejercicio.
2. **Añadir otro tipo de espacio.** Afectaría SpaceType y CHECK de V001 mediante
   migración nueva; ajustar seed solo si se requiere un ejemplo. No hay API/UI que
   editar hoy; futuras validaciones deben reconocerlo. Probar valor nuevo válido,
   desconocido rechazado y lectura de los tipos actuales. No añade permiso de CRUD;
   coordinar migración y versión Java para no leer enums desconocidos. Secuencia:
   acordar significado, inspeccionar consumidores, ampliar constraint/enum y probar.
3. **Reducir precisión temporal a microsegundos.** Afectaría columnas STARTS_AT/ENDS_AT
   y columnDefinition en Booking; OraclePersistenceTest debe revelar redondeo de
   nanosegundos. No hay formato DTO/UI implementado. Riesgo: alterar bordes de futuras
   colisiones; no cambiar propietario ni asumir que todos los valores son exactos.
   Secuencia: medir pérdida con datos aislados, decidir precisión aceptable,
   preparar migración/mapeo, verificar instantes y documentar pérdida si se aprueba.
4. **Cambiar la política de seed para actualizar nombres.** Modificaría R__development_spaces.sql
   y el test de preservación: solo campos explícitamente aprobados cambiarían.
   No crea endpoint administrativo. Riesgo de sobrescribir ediciones manuales o
   concurrentes; idempotencia no autoriza esa sobrescritura. Probar repetición,
   edición previa y conflicto concurrente en copia. Secuencia: definir propiedad
   de catálogo, comparar valores, elegir guard/versión, ajustar MERGE y tests.
5. **Comprobar un error si falta el marcador restaurado.** El comportamiento esperado
   ya es fallar; el ejercicio amplía su evidencia sin ejecutar write. Afecta pruebas
   de JdbcCheck y scripts/jdbc-check.sh, no Booking/DTO/UI. Riesgo: destruir evidencia
   si se ensaya sobre el original; no cambia autorización de reservas. Secuencia:
   preparar recurso aislado, simular fila o archivo ausente, exigir fallo y comprobar
   que no se crea otro token; conservar el respaldo intacto.
6. **Evolucionar V001 sin borrar tablas.** Ejemplo: añadir un atributo opcional de
   catálogo solo si se autorizase. Afecta una futura V002, Space y tests; no hay
   endpoint/DTO/UI actuales. Riesgos: commit DDL implícito, bloqueo y despliegue de
   versiones distintas, no nuevos permisos implícitos. Probar datos anteriores,
   valor nulo, nueva columna y rechazo de esquema incompleto. Secuencia: inspección,
   plan de migración aditiva, prueba en copia, mapeo y validate; nunca ejecutar drop
   ni editar V001 para fingir que la base ya incorporó el cambio.

## 12. Límites y continuación

Primera tarea pendiente: T004, dos identidades locales, Basic/CSRF y credenciales
externas, con pruebas reales de seguridad. T005 fijará el contrato; quedan decisiones
de producto explícitas en spec/plan. Aquí no se adelantaron esas funcionalidades.
T013 sigue pendiente aunque exista WAR y este texto tenga preguntas/retos: faltan
recorrido completo, pruebas de entrega y ejecución de aplicación. T015 continúa
bloqueada por aclaración del runtime; no hay despliegue público ni certificación de
producción. T014 no aplica y no es un bloqueo. Consultar tareas antes de continuar.

## Evolución H002 (supera el estado T003 anterior)

IdentityController/SecurityConfig implementan /api/me y /api/csrf; SpaceController
consulta catálogo. BookingRequest y ApiErrors validan contrato sin confiar campos
extra. SecurityContractTest verifica dos identidades, CSRF y contrato MVC; 15 tests
totales correctos. WAR ahora ejecutable con plugin Boot para uso local. DEC-008
registra el fallo de renovación CSRF y corrección. Reservas/UI aún pendientes H003/H004.
