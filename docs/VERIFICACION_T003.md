# T003: base de persistencia Oracle y WAR

> Registro de su etapa histórica. La aplicación posterior y su operación vigente
> están en [README](../README.md) y [VERIFICACION_FINAL](VERIFICACION_FINAL.md).

Verificación ejecutada por Codex el 2026-09-14, America/Bogota, sobre
`C:\PruebaAIFORGESQUAD`. Continúa desde el commit documental `75af194` (DEC-004/005).
Estado canónico: [tasks.md](../specs/001-booking-espacios/tasks.md).

## Alcance y archivos

[backend/pom.xml](../backend/pom.xml) usa Java 21, Boot 3.3.13 y las versiones
administradas ya presentes; Maven 3.9.9 y las imágenes importadas se reutilizan.
[BookingApplication](../backend/src/main/java/local/booking/BookingApplication.java)
es la entrada Spring/Servlet. `booking` y `space` contienen entidades, enums y
repositorios Spring Data JPA. No hay controladores, servicio de reservas ni UI.
No se crearon paquetes vacíos `security`/`errors`.

[Configuración](../backend/src/main/resources/application.properties): conexión
existente por JDBC_URL/JDBC_USER y configtree de secretos montados; Hibernate
`validate`, SQL automático desactivado. El WAR no contiene contraseñas.
[V001](../backend/src/main/resources/db/oracle/V001__booking_schema.sql) añade dos
tablas, dos secuencias y dos índices, con FK y restricciones de obligatoriedad,
estado, tipo, capacidad y rango. No altera ENVIRONMENT_PROBE.
[Semillas](../backend/src/main/resources/db/oracle/R__development_spaces.sql): tres
espacios ficticios de desarrollo identificados como tales; no son datos oficiales
SED, ni reservas. MERGE solo inserta IDs ausentes y conserva ediciones existentes.

## Ejecuciones reales

Desde la raíz, con ambos contenedores existentes activos:

```powershell
# Ejecutado una sola vez con éxito tras inspeccionar USER_OBJECTS:
pwsh -NoProfile -File scripts/booking-db.ps1 schema
# Ejecutado dos veces: 3 filas insertadas, después 0:
pwsh -NoProfile -File scripts/booking-db.ps1 seed
# Compila, prueba y empaqueta; requiere el esquema/semillas anteriores:
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash mvnw -B -ntp -f backend/pom.xml verify
# Consultas finales:
pwsh -NoProfile -File scripts/booking-db.ps1 status
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash scripts/jdbc-check.sh read
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml ps
```

No repetir `schema` en este equipo: ya se aplicó. Su prueba negativa posterior
rechazó la reaplicación con ORA-20001 antes de cualquier DDL. Si V001 falla a mitad,
Oracle ya puede haber confirmado DDL: inspeccionar objetos y preparar una corrección
aditiva específica; no borrar ni reintentar a ciegas. No hay motor de migraciones
instalado ni reversión automática de DDL.

| Comprobación | Resultado observado |
|---|---|
| Inspección previa | BOOKING/FREEPDB1, solo ENVIRONMENT_PROBE; ningún objeto BKG_ |
| Primer schema | 2 tablas, 2 secuencias y 2 índices creados |
| Seed repetido | 3 filas, luego 0; prueba adicional conserva un nombre editado |
| Maven verify final | BUILD SUCCESS, 12 pruebas, 0 fallos, 0 errores, 0 omitidas |
| Artefacto | backend/target/booking.war, generado a las 20:33:02 UTC (15:33:02 Bogotá) |
| Consulta final | BKG_SPACE=3, BKG_BOOKING=0; ENVIRONMENT_PROBE conservada |
| Preservación | read coincide con marcador original; archivos de credenciales y token idénticos al respaldo privado |
| Docker | oracle healthy, dev activo, imágenes importadas; Oracle sin puerto publicado |
| Puertos | dev publica exclusivamente 127.0.0.1:4200 y 127.0.0.1:8080 |

[OraclePersistenceTest](../backend/src/test/java/local/booking/booking/OraclePersistenceTest.java)
ejecuta JPA y SQL contra Oracle real. Cubre identidad/PDB y mapeo validate; tres
round trips con offsets -05:00, +02:00 y Z, incluyendo nueve decimales; relación con
espacio; secuencia sin colisión con IDs semilla; idempotencia que conserva ediciones;
FK inválida; intervalo cero/negativo; estado/tipo/capacidad inválidos y propietario
obligatorio. Cada prueba revierte su transacción: no quedaron reservas de prueba.
Las secuencias avanzan aunque haya rollback; no se promete numeración consecutiva.

Primera ejecución: 12 pruebas, 6 fallos, 0 errores. Las restricciones Oracle sí
rechazaban los datos: el helper de prueba esperaba SQLException en la causa más
profunda, que el driver representa mediante OracleDatabaseException. Se corrigió
para localizar SQLException en la cadena y comprobar su código Oracle; la segunda
verificación pasó completa. Es un fallo real de la prueba nueva, sin relación con
un defecto sembrado; T014 no aplica.

## Límites

No se repitió la restauración, instalación de herramientas ni H001. La lectura del
marcador es actual; el reinicio y segunda lectura pertenecen a la restauración
anterior. La etiqueta del script «Persistence after restart» no implica un nuevo
reinicio durante T003. No se ejecutó modo write.

El WAR es una base compilada, sin API de negocio ni prueba de despliegue. No se ha
iniciado un servidor HTTP de Booking ni probado Basic/CSRF, propiedad, colisiones,
concurrencia, recurrencias o navegador. No acredita T013 ni resuelve T015/WebLogic.
El soporte de HTTP y Security en el POM no equivale a implementar esos requisitos.

DEC-006 explica la elección técnica: SQL explícito y JPA validate en lugar de un
nuevo gestor para este único esquema inicial. Referencias consultadas:
[Spring Boot 3.3, inicialización](https://docs.spring.io/spring-boot/3.3/how-to/data-initialization.html)
y [Hibernate ORM 6.5, tipos temporales](https://docs.hibernate.org/orm/6.5/userguide/html_single/).

## Revisión previa al commit

136 enlaces locales válidos; 15 IDs T conservados, T001–T003 marcadas completas,
T014 sin marcar y con disposición No aplica, bloque T015 idéntico al commit 8ba745a.
XML Surefire contrastado: 12/0/0/0. Contenido ZIP del WAR inspeccionado: clases base
presentes, Tomcat provided excluido y sin PEM/directorio de secretos. Git ignora
clave, credenciales, token, .env y target; fuentes versionadas/nuevas sin contenido
de clave ni valores de contraseñas. Compose, devcontainer, infra/checks, scripts de
verificación/restauración e integración Spec Kit permanecen sin diff respecto a
75af194. `git diff --check` correcto. Sin reinstalación ni publicación.
