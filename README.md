# Booking de espacios físicos

> Correcciones T018–T022: suite Java88/88 y frontend13/13/build verificadas sobre WAR nuevo. T018 sigue parcial por avisos pendientes y stack obligatorio (DEC-019); T015/WebLogic independiente. Véase [cierre y límites](docs/CIERRE_CORRECCIONES_AUDITORIA.md).

Aplicación local funcional sobre el repositorio definitivo (DEC-004): crear reservas,
consultar las propias activas, cancelarlas y solicitar de 1 a 12 ocurrencias semanales
con aceptación parcial. Dos identidades Basic de desarrollo; Oracle real conserva
los datos y credenciales restaurados. No hay despliegue público.

## Ejecutar en este equipo

Desde PowerShell 7, con Docker Desktop Linux activo:

```powershell
Set-Location C:\PruebaAIFORGESQUAD
pwsh -NoProfile -File scripts/app.ps1 start
pwsh -NoProfile -File scripts/app.ps1 status
```

Abrir **http://localhost:4200/**. Usuarios locales: **ana** y **bruno**; consultar sus
contraseñas en `.local/runtime/booking.properties` desde un editor local. Son credenciales
propias de Booking, distintas de las de Oracle. No copiarlas a Git ni a informes.
La autenticación solo permanece en memoria; recargar o cerrar sesión exige entrar de nuevo.

La API está en http://localhost:8080/api; OpenAPI protegido en
http://localhost:8080/v3/api-docs y http://localhost:8080/swagger-ui/index.html.
El frontend utiliza su proxy `/api/`; no requiere CORS abierto. Se publica únicamente
127.0.0.1:4200 y 127.0.0.1:8080; Oracle no publica puertos.

```powershell
# Recompilar, probar y dejar la aplicación iniciada:
pwsh -NoProfile -File scripts/app.ps1 verify
# Detener aplicación y contenedores, conservando los volúmenes:
pwsh -NoProfile -File scripts/app.ps1 stop
# Volver a iniciar:
pwsh -NoProfile -File scripts/app.ps1 start
```

`start` comprueba hashes de fuentes y WAR; si falta o está obsoleto, lo reconstruye
con pruebas antes de iniciar. `verify` siempre ejecuta una construcción limpia completa. Angular se sirve en modo desarrollo con vigilancia por polling para
el montaje Windows. `verify` detiene temporalmente los procesos de la aplicación,
ejecuta Maven/Angular y vuelve a iniciarlos si pasa; un fallo requiere corregirlo y
repetir el comando. Logs privados en `.local/runtime/backend.log` y `frontend.log`.
La primera resolución de dependencias puede necesitar Internet. No reinstala herramientas.

Los scripts usan siempre ambos Compose e imágenes importadas, sin construir imágenes:

```powershell
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml up -d --no-build --wait --wait-timeout 600
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash scripts/jdbc-check.sh read
```

El esquema V001 y las semillas **ya están aplicados aquí**. No volver a ejecutar
`schema`, borrar volúmenes ni regenerar secretos. `local.ps1 up` reconstruye imágenes
y `local.ps1 verify` reemplaza el marcador; para esta instalación usar `app.ps1` y
la lectura anterior. `pwsh -NoProfile -File scripts/booking-db.ps1 status` consulta datos.
Para otra máquina, importar primero datos y credenciales siguiendo
[TRANSFERIR_EQUIPO](docs/TRANSFERIR_EQUIPO.md) y
[TRANSFERENCIA_VERIFICADA](docs/TRANSFERENCIA_VERIFICADA.md).

## Dev Container y checkout nuevo

Abrir la raíz en VS Code → **Dev Containers: Reopen in Container**.
[Procedimiento completo](docs/DEV_CONTAINER.md): reapertura con imágenes importadas,
inicialización sin regenerar claves y bootstrap protegido para una instalación nueva
con imágenes públicas. Se comprobó CLI up/exec, no apertura visual de VS Code ni
instalación limpia en otra máquina. Los commits de corrección son locales, sin push.

Swagger: abrir la URL protegida, autenticarse en el diálogo Basic, usar Authorize
para Basic, ejecutar GET /api/csrf y copiar token a Authorize csrfToken; conservar la
cookie del mismo navegador. [Contrato y pasos](docs/CONTRATO_API.md).

## Arquitectura y evidencia

Decisión humana DEC-005: monolito organizado por funcionalidades. `booking` contiene
controlador, servicio transaccional, repositorio, DTO, entidad y reglas; `space`,
catálogo/persistencia; `security`, identidad; `errors`, errores comunes. Angular
standalone organiza acceso y reservas. Bloqueo Oracle de la fila Espacio antes de
consultar colisiones, intervalos semiabiertos, propietario tomado del principal y
CSRF exigido en escrituras. Una transacción por solicitud recurrente; los conflictos
son resultados de negocio y los errores técnicos revierten la solicitud.

Java 21 / Boot 3.3.13 / Oracle 23.26.3 / Angular 20.3.31; versiones y resultados en
[verificación final](docs/VERIFICACION_FINAL.md), contrato en
[CONTRATO_API](docs/CONTRATO_API.md) y explicación con preguntas y ejercicios en
[EXPLICACION_IMPLEMENTACION](docs/EXPLICACION_IMPLEMENTACION.md).
WAR ejecutable: `backend/target/booking.war`, con Tomcat administrado por Boot para
la ejecución local. Tener WAR no acredita compatibilidad con WebLogic 12.2.1.4:
T015 sigue bloqueada por aclaración externa. Azure real no forma parte de la entrega;
MSAL/Resource Server están preparados y desactivados en modo Basic.

## Continuidad

[Spec Kit](docs/SPECKIT.md), [constitución](.specify/memory/constitution.md),
[spec](specs/001-booking-espacios/spec.md), [plan](specs/001-booking-espacios/plan.md),
[tareas: única fuente de estado](specs/001-booking-espacios/tasks.md),
[handoffs](.handoffs/README.md), [bitácora real](docs/BITACORA.md),
[criterios de entrega](docs/ENTREGA_Y_STARTER.md).
El starter no llegará: T014 **No aplica por cambio de alcance**, no implementada;
no habrá migración ni defecto equivalente. Se preservan historial y evidencias
previas de [H001](docs/INTEGRACION_H001.md), [T003](docs/VERIFICACION_T003.md) y
[entorno original](docs/ENTORNO_LOCAL.md), que describen sus respectivas etapas.

## Auditoría posterior — T017 / DEC-015

Registro histórico: T017 conservó seis regresiones fallidas contra47a6af4. Esa situación queda superada por la ejecución corregida88/88; no se reescribe el informe original. El nuevo cierre acredita Dev Container CLI y distingue los pendientes de seguridad, Swagger y WebLogic.

[Diagnóstico y evidencia](docs/AUDITORIA_CUMPLIMIENTO_Y_SEGURIDAD.md) · [Plan de correcciones](docs/PLAN_CORRECCIONES_AUDITORIA.md).
