# Booking de espacios físicos

Aplicación local para consultar espacios, crear reservas sin colisiones, listar las
propias activas y cancelarlas. Permite 1–12 ocurrencias semanales con aceptación parcial.
Dos usuarios Basic de desarrollo simulan identidad corporativa. No incluye edición,
notificaciones, CRUD de espacios/usuarios ni integración Azure real. El starter y su
supuesto defecto quedaron fuera del alcance por decisión humana DEC-004.

**Estado de entrega:** aplicación local verificada, con pendientes explícitos:
T018 seguridad parcial, T021 escritura interactiva Swagger no acreditada y T015
WebLogic independiente. Funcionar en Tomcat no demuestra cumplir WebLogic 12.2.1.4.
[Verificación y límites](docs/VERIFICACION.md) · [Tratamiento de avisos](docs/TRATAMIENTO_DEPENDENCIAS_T018.md).

## Arquitectura y versiones efectivas

Monolito por funcionalidades elegido por el usuario (DEC-005). Dentro de `booking`:
controlador → servicio transaccional → repositorio; `space` contiene catálogo y fila
bloqueada, `security` identidad y CSRF, `errors` tratamiento HTTP compartido. Angular
standalone separa `acceso` y `reservas`. No microservicios, bus ni persistencia alternativa.

| Componente | Versión del proyecto |
|---|---|
| JDK / Maven / Wrapper | Temurin 21.0.10+7 / 3.9.9 / 3.3.4 |
| Boot / Framework / Security |3.3.13 / 6.1.21 / 6.3.10 |
| Data JPA / Hibernate / SpringDoc |3.3.13 / 6.5.3.Final / 2.6.0 |
| Tomcat / Oracle / JDBC |10.1.59 / Free 23.26.3-slim / ojdbc11 21.9.0.0 |
| Node / npm dentro de dev |22.22.0 / 10.9.4 |
| Angular core / CLI-build / CDK |20.3.31 / 20.3.37 / 20.2.0 |
| PrimeNG / Tailwind / RxJS / TypeScript |20.0.0 / 3.4.17 / 7.8.2 / 5.9.2 strict |
| MSAL Angular / Browser |3.1.0 / 3.28.1; extensión preparada, inactiva |

[Contrato API](docs/CONTRATO_API.md). Intervalos semiabiertos; se bloquea la fila Espacio
antes de consultar colisiones. Una transacción por solicitud recurrente conserva
ocurrencias válidas ante conflicto y revierte todo ante fallo técnico. Propietario del
principal; Basic por petición y cookie de sesión exclusivamente para CSRF. No se guarda
Basic en localStorage. Decisiones y alternativas: [BITACORA](BITACORA.md).

## Requisitos del equipo

- Windows 10/11 x64 con Docker Desktop y motor Linux/WSL2 activo; o Linux x86_64 con
  Docker Engine y Compose. Imágenes fijadas **linux/amd64**; no se acredita ARM/emulación.
- Docker Compose compatible con `--wait` y `!reset`; comprobado 5.5.1/Engine 29.8.0.
  `check` valida el overlay con el Compose instalado antes del arranque.
- Git y **Node 18 o superior en el host** para el operador común (comprobado 18.19.1 en
  Windows). Node 22 es la versión dentro del contenedor. No hace falta Java/Maven/npm
  global ni PowerShell en Linux. Transferencia cifrada antigua usa PowerShell 7 aparte.
- Planificar 6 GB para Docker (Oracle limitado 3 GB, dev 2 GB), al menos 8 GB de RAM del equipo
  y 25 GB de disco libre para imágenes/cachés/datos; mayor holgura evita presión de memoria.
  No se garantiza capacidad con cargas grandes. Internet para registros públicos y Maven/npm.
- Puertos 127.0.0.1:4200 y 8080 disponibles. Acceso al daemon Docker con la cuenta actual.
- Linux: checkout local escribible y cuenta **UID1000**, correspondiente a `node` en
  la imagen. Secretos nuevos usan 0600; no ampliar permisos a todo el mundo. El bootstrap
  rechaza otro UID. No se verificó otro mapeo de usuarios, SELinux ni Docker rootless.
- Para Dev Container: VS Code con extensión Dev Containers, o CLI ya instalada.

## A. Instalación nueva del evaluador

Obtener el repositorio privado con acceso GitHub propio (no introducir tokens en URLs),
o clonar un bundle Git entregado con su revisión. Estos comandos sirven en PowerShell
Windows y en shell Linux; elegir una carpeta vacía propia, sin ruta fija obligatoria:

```text
git clone https://github.com/luzadri84/AIFORGESQUADP1.git Booking
cd Booking
node scripts/environment.mjs prepare-new
node scripts/environment.mjs images
node scripts/environment.mjs up
node scripts/environment.mjs schema
node scripts/environment.mjs seed
node scripts/environment.mjs verify
```

Usar la revisión de esta entrega que incluye `scripts/environment.mjs`; los commits
más recientes de consolidación aún son locales hasta una publicación autorizada.
Con un bundle: `git bundle verify RUTA_AL_BUNDLE` y `git clone RUTA_AL_BUNDLE Booking`.
No clonar encima de trabajo existente. No hace falta `specify init` para ejecutar.

`prepare-new` rechaza `.local` o cualquier proyecto/volumen `booking-local` existente
**antes** de crear claves. Genera contraseñas Oracle nuevas en `.local/secrets`, copia
`.env.example` solo si falta y crea el overlay ignorado con imágenes públicas. No restaura
los datos originales del autor. `images` construye dev con el Dockerfile fijado y descarga
Oracle por digest público; no depende de imágenes `booking-transfer` privadas. Si falla
la red, repetir `images`, sin borrar `.local` ni volver a generar claves.

`up` usa ambos Compose y `--no-build`; espera salud Oracle. **Solo en esta base nueva**,
`schema` aplica V001 una vez y `seed` inserta tres espacios sin sustituir existentes.
V001 rechaza objetos existentes; Oracle DDL confirma implícitamente: tras un fallo parcial,
inspeccionar antes de reintentar. No usar `schema` en restauraciones. `verify` genera
Basic si falta, descarga dependencias necesarias, construye WAR limpio con pruebas,
comprueba Angular/Oracle y deja la aplicación iniciada. No carga un WAR entregado antiguo.

El proyecto Compose se llama `booking-local`. Para tener otra instalación aislada en
el mismo daemon hace falta diseñar nombres/puertos/volúmenes distintos; este operador
rechaza el conflicto, no lo resuelve borrando datos. No cambiar solo el nombre del directorio.

## B. Reutilización del entorno existente

Desde su checkout ya restaurado, con `.local` y los volúmenes originales:

```text
node scripts/environment.mjs check
node scripts/environment.mjs start
node scripts/environment.mjs status
node scripts/environment.mjs db-status
```

No ejecutar `prepare-new`, `images` ni `schema`. `check` valida secretos/imágenes sin
regenerarlos. `start` conserva credenciales, utiliza las imágenes del overlay existente
y compara fuentes/metadata/SHA256 del WAR. Si está obsoleto lo reconstruye con pruebas
antes de iniciar; no certifica un artefacto con `-DskipTests`. `.local` está ignorada:
conservarla de forma privada junto con los volúmenes. Para restaurar el respaldo cifrado
original en otra máquina: [procedimiento de transferencia](docs/TRANSFERIR_EQUIPO.md),
con datos Y credenciales antes de preparar o iniciar Oracle. Se requiere la clave aparte.

Los scripts PowerShell previos siguen disponibles para el entorno Windows existente;
la entrada común anterior evita exigir pwsh en Linux. No usar `local.ps1 up/verify`
como arranque rutinario: el primero construye imágenes y el segundo escribe otro marcador.

## Dev Container exigido por la prueba

Después del bootstrap (A) o check (B), abrir la raíz en VS Code y ejecutar
**Dev Containers: Reopen in Container**. La configuración une compose.yaml,
.local/transfer/compose.images.yaml y .devcontainer/compose.restored.yaml; elimina
build durante reapertura. initialize usa Node/Docker del host y solo valida.
`postCreateCommand` verifica herramientas, sondas y JDBC; lee el marcador si existe,
no lo crea ni reemplaza. `updateRemoteUserUID=false` conserva el usuario node1000.

Con CLI instalada, alternativa común desde la raíz:

```text
devcontainer up --workspace-folder . --id-label com.docker.compose.project=booking-local --id-label com.docker.compose.service=dev
devcontainer exec --workspace-folder . --id-label com.docker.compose.project=booking-local --id-label com.docker.compose.service=dev bash scripts/verify-local.sh
```

En la terminal del Dev Container: `bash scripts/app-process.sh status`. Para construir
y verificar, ejecutar el operador `node scripts/environment.mjs verify` **en el host**,
que dispone del daemon/CLI Docker; no ejecutar ese operador dentro de dev, que no
monta el socket ni instala Docker. La compilación dentro de dev se puede hacer con
`bash scripts/backend-artifact.sh verify` después de detener los procesos de la app;
no ejecutar dos verificaciones o pruebas concurrentemente contra el mismo esquema.

Evidencia: CLI up/exec y ejecución dentro del Dev Container comprobadas en Windows;
no se afirma apertura visual de VS Code ni instalación limpia de un host Linux.

## Acceso, operación y pruebas

- Interfaz: http://localhost:4200/; API: http://localhost:8080/api.
- OpenAPI: http://localhost:8080/v3/api-docs y http://localhost:8080/swagger-ui/index.html.
- Abrir `.local/runtime/booking.properties` en un editor local para consultar las
  claves de **ana** y **bruno**. No pegarlas en Git, capturas, tickets ni comandos.
  Son distintas de las contraseñas Oracle. Recargar la UI elimina su identidad en memoria.
- Swagger protegido: Basic en diálogo del navegador; Authorize/basicAuth en Swagger,
  GET / api/csrf con Try it out, copiar `token` a Authorize/csrfToken y conservar la cookie
  del mismo navegador. Escrituras necesitan ambos; ver [contrato](docs/CONTRATO_API.md).
  Su escritura interactiva continúa no acreditada; no confundir tests OpenAPI con esa prueba.

```text
node scripts/environment.mjs verify
node --test scripts/test/environment.test.mjs
node scripts/environment.mjs logs
node scripts/environment.mjs stop
node scripts/environment.mjs start
```

`verify` realiza Maven clean verify, Angular build/test y sonda/JDBC; detiene temporalmente
la app y solo la reinicia si todo pasa. No ejecuta jdbc write. Tests operativos usan
carpetas temporales y Docker simulado; no sustituyen el recorrido con Docker real.
`stop` conserva los volúmenes; no se usa down -v. Para reiniciar Oracle y todo el entorno,
`stop` seguido de `start`; no cambiar las claves con un volumen inicializado.

Logs privados: `.local/runtime/backend.log`, `frontend.log`; Oracle: comando
`docker compose -f compose.yaml -f .local/transfer/compose.images.yaml logs --tail 80 oracle`.
Revisar/redactar antes de compartir. `.env` permite LOCAL_HTTP_PORT/LOCAL_ANGULAR_PORT;
las publicaciones permanecen 127.0.0.1. Oracle no publica puerto de host.

Problemas habituales: activar motor Linux si check falla; importar imágenes restauradas
o repetir images en base nueva si falta una imagen; liberar puertos o ajustar .env;
revisar memoria/red para primera compilación. Un 404 de datos exige revisar recurso,
403 exige sesión/token; reingresar obtiene token nuevo. Ante timeout de reserva, consultar
la lista antes de repetir: el servidor puede haber confirmado. Si verify falla, revisar
logs y corregir; no omitir pruebas ni presentar el WAR anterior como corrección validada.

## Evidencia y pendientes

Última suite funcional sobre fuentes e859c80:88/88 Java,13/13 frontend y build strict;
HTTP contra WAR nuevo, carrera/rollback Oracle y flujo visual. Esta consolidación no
repite esas pruebas ni cambia código funcional. Pruebas operativas actuales y sus límites
en [VERIFICACION](docs/VERIFICACION.md). [Tareas](specs/001-booking-espacios/tasks.md) es la
única fuente de estado; [handoffs](.handoffs/README.md) y [Spec Kit](docs/SPECKIT.md)
conservan continuidad. [AGENTS](AGENTS.md) preserva requisitos y extensiones; [BITACORA](BITACORA.md)
explica decisiones humanas/técnicas y fallos reales. Documentos de estudio están fuera
del repositorio y no son necesarios para instalar ni evaluar este checkout.

T018: no se acepta riesgo en nombre del usuario; mantiene stack obligatorio. T015:
Boot 3/Jakarta/Java 21 no se ha probado compatible con WebLogic 12.2.1.4. T023/T024 son
opcionales no implementadas. Ni Azure real, rendimiento a escala ni despliegue público
forman parte de las verificaciones acreditadas.
