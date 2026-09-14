# Dev Container: reapertura y evaluador

La condición de entrega mediante Dev Container fue aportada por la instrucción
humana de correcciones (DEC-016); el correo original no está disponible en apoyo.
No se afirma haber leído ese correo ni se modifica la matriz histórica de T017.

## Entorno restaurado existente

Requisitos: Docker Desktop activo Linux/amd64 (aproximadamente 6 GB de memoria para
Docker), PowerShell7, Git y VS Code con Dev Containers ya instalados. Puertos locales
4200/8080 libres; Internet para dependencias no cacheadas. No reinstalar herramientas.
Abrir la raíz del checkout en VS Code y ejecutar **Dev Containers: Reopen in Container**.
La configuración combina compose.yaml, .local/transfer/compose.images.yaml y el
pequeño compose.restored.yaml que elimina build. Usa imágenes presentes, nunca
reconstruye durante reapertura. initialize valida archivos/imágenes y no genera claves.
postCreate verifica Java21/Node22/Maven/Oracle; lee el marcador cuando existe, nunca
lo reemplaza. La aplicación requiere esquema/credenciales ya restaurados.

Desde PowerShell7 en la raíz:

```powershell
pwsh -NoProfile -File scripts/app.ps1 verify
pwsh -NoProfile -File scripts/app.ps1 status
```

verify ejecuta pruebas completas y deja http://localhost:4200/ y la API8080 iniciadas.
Dentro de la terminal del Dev Container se puede ejecutar:

```bash
bash scripts/verify-local.sh
bash scripts/backend-artifact.sh verify
cd frontend && npm run build && npm test
```

No ejecute las pruebas completas simultáneamente con la aplicación: para ese flujo
manual detenga primero sus procesos con `bash scripts/app-process.sh stop` y al
terminar use app.ps1 start en el host. El script host gestiona esa secuencia.

También es válido `devcontainer up --workspace-folder .` seguido de
`devcontainer exec --workspace-folder . bash scripts/verify-local.sh` si la CLI ya
está instalada. La extensión VS Code incluye una CLI: su ruta depende de la versión,
no debe quedar fijada en devcontainer.json. La comprobación automatizada de esta
fase utiliza up/exec sobre el contenedor existente identificado por las etiquetas
com.docker.compose.project=booking-local y com.docker.compose.service=dev; no acredita
una apertura visual del editor. El informe de cierre registra resultados exactos.

## Checkout nuevo del evaluador, sin restaurar datos privados

Recibir el historial Git y seleccionar la revisión corregida entregada. Los commits
de esta fase son locales (no se autorizó push); un clone del remoto anterior no los
contiene hasta su publicación autorizada o entrega como bundle. Repositorio privado:
GitHub requiere acceso propio del evaluador; no compartir tokens. Alternativa de
transporte: bundle Git entregado con la revisión, sin .local ni secretos.

En una máquina sin el proyecto booking-local ni su volumen Oracle, clonar la revisión
o el bundle, situarse en su raíz y ejecutar:

```powershell
pwsh -NoProfile -File scripts/prepare-new-environment.ps1
# Abrir esa carpeta en VS Code: Dev Containers: Reopen in Container
# Solo para esta base NUEVA y vacía, una vez que Oracle esté saludable:
pwsh -NoProfile -File scripts/booking-db.ps1 schema
pwsh -NoProfile -File scripts/booking-db.ps1 seed
pwsh -NoProfile -File scripts/app.ps1 verify
```

Bootstrap construye dev desde las imágenes públicas fijadas en Dockerfile y descarga
Oracle público por digest; no necesita las imágenes privadas de transferencia.
Genera credenciales Oracle nuevas en .local/secrets, conserva .env.example sin secretos
y prepara el mismo overlay local. app.ps1 crea Basic solo cuando no existe su archivo.
Esto NO restaura datos originales, no crea ni certifica el marcador del respaldo.
El script rechaza .local/volumen/proyecto existentes antes de generar nada. Si falla
una descarga después de crear .local, NO borrar secretos para reintentarlo: corregir
la descarga, repetir docker build/pull del script y escribir únicamente el overlay
faltante con sus mismas referencias. La reapertura validará el resultado.

Este procedimiento se revisa y se comprueba su rechazo seguro sobre el entorno
existente; no equivale a una instalación desde cero ejecutada en otra máquina.
No aplicar schema aquí: la base restaurada ya contiene las tablas.

Para restaurar datos originales en otro equipo, seguir TRANSFERIR_EQUIPO.md:
importar imágenes, descifrar e importar datos Y credenciales ANTES de prepare/up;
requiere respaldo y clave entregada aparte. Esa ruta no usa bootstrap de base vacía.

## Selección de WAR y fallos

start compara hash de fuentes de producción/POM/wrapper con la metadata ignorada
.local/runtime/backend-build.properties y el SHA256 del WAR. Si cambia/falta,
detiene backend y ejecuta package con pruebas antes de iniciar. verify siempre
reconstruye con Maven verify y registra commit, estado de fuentes, fecha y hash.
No se certifica un WAR mediante -DskipTests. Un fallo de pruebas deja app detenida;
corregir y repetir verify. Para recuperar temporalmente un WAR anterior conservado,
detener backend, copiar explícitamente la copia local conocida a backend/target y
arrancarlo directamente con app-process.sh backend/frontend; identificar su revisión
anterior y no presentarlo como corrección verificada. start detectará su hash obsoleto.

Detener/reabrir sin borrar datos: app.ps1 stop / app.ps1 start. Nunca down -v.
