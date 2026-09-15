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

## Guía rápida

- [Requisitos y preparación Windows/Linux](#requisitos-del-equipo).
- [Instalación nueva](#a-instalación-nueva-del-evaluador).
- [Reutilización, puertos y datos](#b-reutilización-del-entorno-existente).
- [Dev Container](#dev-container-exigido-por-la-prueba).
- [Acceso y operación](#acceso-operación-y-pruebas).
- [Diagnóstico de instalación](#diagnóstico-de-instalación).
- [Evidencia y pendientes](#evidencia-y-pendientes).

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
| Tomcat / Oracle / JDBC |10.1.59 / Free 23.26.3-slim / ojdbc11 23.26.3.0.0 |
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

### Preparación según el sistema operativo

| Host | Preparación | Comprobación específica |
|---|---|---|
| Windows x64, PowerShell | Instalar y abrir [Docker Desktop con WSL2](https://docs.docker.com/desktop/setup/install/windows-install/); comprobar los requisitos vigentes de Windows y virtualización. Instalar Git y Node en Windows y abrir una terminal nueva. | `wsl --status`; el servidor Docker debe informar Linux, aunque PowerShell se ejecute en Windows. |
| Linux x86_64, Bash | Instalar [Docker Engine](https://docs.docker.com/engine/install/) y el plugin Compose para la distribución. Configurar el [acceso al daemon](https://docs.docker.com/engine/install/linux-postinstall/) para la cuenta que usará el proyecto. | `id -u` debe devolver `1000` para este bootstrap; el checkout debe pertenecer a esa cuenta. |

Node 22 en el host permite usar la misma versión mayor que la imagen; el operador
acepta Node >=18. Las versiones exactas del contenedor las fija el Dockerfile.
En Linux, no ejecutar `prepare-new` con `sudo`: el UID 0 también es rechazado.
Si la cuenta tiene otro UID, resolver el mapeo del usuario antes de instalar;
el operador actual no lo ajusta automáticamente. No corregirlo con `chmod 777`.

Elegir una terminal y un checkout para todo el recorrido. Si se ejecuta desde una
terminal WSL, se aplican las condiciones de Linux, incluido el UID; las rutas son
las de esa terminal. Los ejemplos PowerShell de esta guía se ejecutan en Windows.

### Comprobaciones previas comunes

Ejecutar en el host, antes de generar el entorno:

```text
git --version
node --version
docker version
docker compose version
docker context show
docker info --format '{{.OSType}}/{{.Architecture}}'
```

`docker version` debe mostrar cliente y servidor; el último comando debe devolver
`linux/amd64` o `linux/x86_64`. Usar el contexto del Docker local que montará el
checkout. El operador espera acceso al daemon sin cambiar de usuario entre pasos.
Un Compose antiguo puede rechazar `!reset`: usar una versión que soporte la
[fusión de archivos Compose](https://docs.docker.com/compose/how-tos/multiple-compose-files/merge/)
y `up --wait`. La comprobación definitiva del proyecto se ejecuta con `check`
después de `images`, cuando ya existen `.local` y las imágenes.

## A. Instalación nueva del evaluador

Obtener el repositorio privado con acceso GitHub propio (no introducir tokens en URLs),
o clonar un bundle Git entregado con su revisión. Estos comandos sirven en PowerShell
Windows y en shell Linux; elegir una carpeta vacía propia, sin ruta fija obligatoria:

```text
git clone https://github.com/luzadri84/AIFORGESQUADP1.git Booking
cd Booking
```

### Acceso al repositorio privado

Si GitHub devuelve `Repository not found`, abrir el repositorio en el navegador
con la cuenta autorizada y comprobar que se ven los archivos. Un 404 puede indicar
URL incorrecta o falta de permisos; aceptar la invitación de colaboración si está
pendiente. Tener sesión en el navegador no garantiza que Git use esa misma cuenta.
[Diagnóstico oficial de GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/troubleshooting-cloning-errors).

Si Git Credential Manager está instalado, se puede autenticar con código:

```text
git credential-manager github login --device --force
```

Seguir el enlace y el código que muestra la terminal, autorizar la cuenta con acceso
y repetir `git clone`. Este modo evita la redirección al servidor local `127.0.0.1`
del inicio de sesión por navegador. Si el comando no existe, configurar un gestor
de credenciales o SSH según la [guía de GitHub](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/about-authentication-to-github).
No incluir tokens en la URL ni compartir códigos de autorización.

### Inicialización y arranque

Desde la raíz `Booking`, ejecutar **un comando por vez** y continuar solo si termina
correctamente. Todos estos comandos se ejecutan en el host:

```text
node scripts/environment.mjs prepare-new
node scripts/environment.mjs images
node scripts/environment.mjs up
node scripts/environment.mjs schema
node scripts/environment.mjs seed
node scripts/environment.mjs verify
```

La revisión entregada debe incluir `scripts/environment.mjs`, `ojdbc11` 23.26.3.0.0
en [backend/pom.xml](backend/pom.xml) e [infra/checks/pom.xml](infra/checks/pom.xml),
y la excepción Git de `/workspace` en [backend-artifact.sh](scripts/backend-artifact.sh)
(DEC-024). Actualizar el README por sí solo no incorpora esas correcciones a un
clon de una revisión anterior. Identificar la revisión recibida con `git rev-parse HEAD`
y comprobar cambios locales con `git status --short`.
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

### Resultado esperado

La primera ejecución descarga imágenes y dependencias; su duración depende de la
red, disco y memoria. `up` espera hasta 600 segundos por la salud de los servicios;
al iniciar la aplicación, el operador espera hasta 120 segundos por cada URL.
Un timeout requiere revisar el servicio correspondiente, no repetir la inicialización.

- `verify` termina con código 0 y el mensaje `Booking running`.
- Maven muestra `BUILD SUCCESS`; en la revisión DEC-024 pasan 88 pruebas Java.
- Angular compila; en esa misma revisión pasan 13 pruebas del frontend.
- La sonda confirma `Schema=BOOKING, PDB=FREEPDB1, JDBC connection OK`.
- La interfaz abre en [localhost:4200](http://localhost:4200/); las credenciales de
  ana/bruno se consultan en `.local/runtime/booking.properties` con un editor local.

`up` inicia los contenedores; `start` y `verify` arrancan los procesos de la aplicación.
Un contenedor `dev` en estado Up por sí solo no confirma que la interfaz esté lista.
Los contadores de pruebas son evidencia de esta revisión, no valores fijos para futuras versiones.

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

### Puertos y conservación de datos

Si 8080 o 4200 están ocupados, editar `.env` después de `prepare-new` y antes de `up`.
Por ejemplo:

```dotenv
LOCAL_HTTP_PORT=8081
LOCAL_ANGULAR_PORT=4300
```

En un entorno ya iniciado, guardar el archivo y ejecutar `stop` seguido de `start`.
Se accederá a `http://localhost:4300/` y `http://localhost:8081/swagger-ui/index.html`.
Los puertos internos siguen siendo 4200/8080; el proxy de Angular ya apunta al backend
interno. El mensaje final del operador muestra las URLs predeterminadas: usar los
valores de `.env` si se cambiaron. Estas publicaciones están limitadas a localhost;
la configuración entregada está destinada a desarrollo/evaluación local.

| Elemento | Qué conserva | Tratamiento |
|---|---|---|
| Volumen `booking-local_oracle-data` | Base Oracle, esquema y reservas | Conservar al detener/reiniciar. Un clon Git no contiene estos datos. |
| `.local/secrets/` | Credenciales de la base existente | Guardar de forma privada junto con el respaldo de datos; no regenerarlas para ese volumen. |
| `.local/runtime/booking.properties` | Usuarios y claves Basic locales | Conservar para mantener el acceso de ana/bruno. |
| `.env` y `.local/transfer/compose.images.yaml` | Puertos y selección de imágenes | Conservar para reproducir el entorno. |
| Cachés Maven/npm, `node_modules`, `target`, `dist` | Dependencias y compilados | Se pueden reconstruir; no sustituyen un respaldo de Oracle. |

`stop` conserva los datos. Evitar `docker compose down -v` y limpiezas de volúmenes
como solución a errores de instalación. El [procedimiento de transferencia](docs/TRANSFERIR_EQUIPO.md)
documentado corresponde a Windows, requiere PowerShell 7 y una clave de cifrado
separada. La Release antigua citada allí contiene infraestructura de otra etapa;
para conservar reservas actuales se necesita una exportación actual. Falta validar
un procedimiento completo desde un host Linux. Copiar solo el repositorio o `.local`
no respalda los datos del volumen Oracle.

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

## Diagnóstico de instalación

### ORA-01017 al verificar una instalación nueva

Si `schema` y `seed` conectan pero `verify` falla con `ORA-01017`, comprueba la
versión JDBC: `prepare-new` genera claves de 52 caracteres y el controlador 21.9
heredado del BOM no admite estas claves largas. La aplicación y la sonda fijan
`ojdbc11` 23.26.3.0.0 en sus POM. Con ambos POM actualizados, ejecutar de nuevo
`node scripts/environment.mjs verify`; se conservan la base y las credenciales.
Este diagnóstico específico se confirmó comparando ambos controladores con el
mismo secreto; otros casos de ORA-01017 requieren revisar el acceso a Oracle.
`ApplicationContext failure threshold` y los errores de dialecto Hibernate pueden
ser consecuencias: buscar antes el primer `Caused by` o código `ORA-`.

### Git rechaza la propiedad de /workspace

En un montaje Windows puede aparecer `detected dubious ownership`. La revisión
DEC-024 configura automáticamente `safe.directory=/workspace` dentro del contenedor
antes de compilar y registrar el WAR. La excepción se limita a ese montaje;
no configurar `safe.directory=*`. Si aparece en una revisión anterior, incorporar
la corrección de `scripts/backend-artifact.sh` y repetir `verify`.

### Otros errores y recuperación

| Síntoma | Qué comprobar / siguiente paso |
|---|---|
| Docker no responde o informa Windows/ARM | Iniciar el motor Linux amd64 y revisar el contexto con las comprobaciones previas. |
| `unknown flag: --wait` o error de etiqueta `!reset` | Actualizar Compose para soportar las opciones usadas; volver a `check`. |
| `Existing .local` o proyecto/volúmenes existentes | Identificar si es una instalación ya preparada: seguir B. No borrar secretos ni volúmenes para forzar `prepare-new`. |
| `Missing .local/...` | Determinar si faltó terminar el bootstrap o restaurar los archivos privados. Un volumen existente requiere sus credenciales originales. |
| Imagen no disponible | En una instalación nueva ya preparada, repetir `images` tras corregir la red. En una restauración, importar las imágenes indicadas en su overlay. |
| `Permission denied` o `/workspace` no escribible | Revisar propietario y UID1000 en Linux, permisos del checkout y montaje del contenedor. |
| `schema` falla con objetos existentes | Revisar si V001 se aplicó completa o parcialmente; el DDL de Oracle no se revierte entero. No repetirlo a ciegas. |
| Oracle no alcanza estado healthy | Revisar logs del servicio, memoria y disco; corregir antes de repetir `up`. |
| Puerto ocupado | Ajustar `.env` como se describe arriba y reiniciar con `stop`/`start`. |
| Fallo de descarga de Docker/Maven/npm | Revisar conectividad, DNS y proxy/certificados de la red; conservar cachés y repetir el paso que falló. |
| Código 137 / proceso terminado | Comprobar si hubo falta de memoria y revisar Docker; el código por sí solo no confirma la causa. |
| `verify` falla y la UI deja de responder | Es esperado que la app quede detenida: corregir la primera causa y volver a ejecutar `verify`. |
| `invalid option` con `\r` o `bash\r` | Revisar finales de línea: `.gitattributes` exige LF en scripts shell y `mvnw`. |
| HTTP 401 / 403 | Revisar credenciales Basic / sesión y token CSRF; volver a ingresar obtiene un token nuevo. |

Ante un timeout al crear una reserva, consultar la lista antes de repetir: la
operación puede haber quedado confirmada. Para verificar usar un solo proceso a la
vez: las pruebas de integración utilizan el esquema Oracle local y ejecutan escrituras
de prueba. No ejecutar `schema`, `seed` o `verify` simultáneamente desde otra terminal.

### Guardar un error completo para soporte

Después de preparar el entorno, desde la raíz del proyecto:

**PowerShell (Windows):**

```powershell
node scripts/environment.mjs verify *> .local/runtime/verify-install.log
$LASTEXITCODE
Get-Content .local/runtime/verify-install.log -Tail 80
```

**Bash (Linux):**

```bash
node scripts/environment.mjs verify > .local/runtime/verify-install.log 2>&1
echo $?
tail -n 80 .local/runtime/verify-install.log
```

La salida queda en el archivo durante la ejecución. El código 0 indica éxito;
el resumen final puede omitir la causa inicial, así que conservar el log completo.
Para reportar un problema, indicar sistema/terminal, revisión (`git rev-parse HEAD`),
comando que falló, versiones de Node/Docker/Compose y el primer error con su contexto.
Añadir `node scripts/environment.mjs status`; si es conexión Oracle, `db-status`.
Revisar y redactar los logs antes de compartirlos; no adjuntar `.local/secrets`,
`booking.properties`, tokens ni un volcado completo de variables de entorno.

## Evidencia y pendientes

El 2026-09-15 se completó `verify` sobre el clon Windows con las correcciones DEC-024:
88/88 Java, 13/13 frontend, build, sonda Oracle y arranque HTTP correctos. Los pasos
prepare-new/images/up/schema/seed fueron ejecutados y reportados por el usuario;
el diagnóstico y verify posterior fueron comprobados por el agente. No equivale
a acreditar una instalación limpia en un host Linux. La guía Linux se basa en los
scripts portables y sus requisitos, y conserva ese límite explícito.

La ejecución anterior e859c80 y su flujo visual conservan su evidencia histórica.
Esta ampliación del README es documental: no vuelve a ejecutar las suites ni cambia
la aplicación. Resultados y límites en [VERIFICACION](docs/VERIFICACION.md).
[Tareas](specs/001-booking-espacios/tasks.md) es la
única fuente de estado; [handoffs](.handoffs/README.md) y [Spec Kit](docs/SPECKIT.md)
conservan continuidad. [AGENTS](AGENTS.md) preserva requisitos y extensiones; [BITACORA](BITACORA.md)
explica decisiones humanas/técnicas y fallos reales. Documentos de estudio están fuera
del repositorio y no son necesarios para instalar ni evaluar este checkout.

T018: no se acepta riesgo en nombre del usuario; mantiene stack obligatorio. T015:
Boot 3/Jakarta/Java 21 no se ha probado compatible con WebLogic 12.2.1.4. T023/T024 son
opcionales no implementadas. Ni Azure real, rendimiento a escala ni despliegue público
forman parte de las verificaciones acreditadas.
