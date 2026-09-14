# Bitácora de infraestructura

## 1. Diagnóstico y alcance — 2026-09-14

Pedido humano: ejecutar la sección 6 de 04_Preparar_Entorno_Local_con_Codex.md,
diagnosticando primero, sin negocio ni publicación.

Observado: seis documentos en la carpeta inicial, sin .git, POM, package.json,
devcontainer ni código. Se leyó el correo MIME, su AGENTS.md original y la historia
DOCX (texto del XML interno). El correo trae un marcador sin URL para el starter.
Se restauró AGENTS.md sin alterar sus requisitos.

Decisión del agente dentro del alcance autorizado: crear una base provisional
aislada y un historial nuevo, sin atribuirle el historial o el defecto del starter.
Primer commit real: preservación de documentos y especificación.

## 2. Reutilización del equipo

Windows 11 Home Single Language build 26200, Ryzen 7 7730U, 8 núcleos/16 hilos,
virtualización activa, 13,84 GiB de RAM visible, unos 159 GiB libres en C.
Docker Desktop 4.91.0 / Engine 29.8.0 / Compose 5.5.1, contexto desktop-linux.
WSL2 docker-desktop activo; no se instaló otra distribución ni daemon.
VS Code y ms-vscode-remote.remote-containers ya estaban instalados.
Git Windows 2.47.1, Node Windows 22.14.0 y npm 10.9.2 reutilizados para diagnóstico.
Java y Maven no estaban en PATH del host.

La primera lectura Windows dio 520 MiB libres; antes del arranque dio 5,19 GiB.
WSL reportó 6.469 MiB disponibles de 6.850 MiB. No se cerraron procesos ajenos.
Se acotó Oracle a 3 GiB y dev a 2 GiB. No se cambiaron límites globales de WSL.
Puertos 1521, 4200 y 8080 libres al diagnóstico; no había contenedores ejecutándose.
Imágenes de otros proyectos conservadas.

El sandbox falló repetidamente al crear procesos y leer archivos con
helper_sandbox_lock_failed / SetNamedSecurityInfoW error 5.
Se pidieron y usaron permisos de ejecución externos al sandbox; no se modificaron
sus protecciones. El acceso a Docker y al workspace es real.

## 3. Versiones y secretos

Se contrastaron npm view (existencia, engines, peers), BOM Maven Central y
docker buildx imagetools inspect. Imágenes exactas y digest en Compose/Dockerfile.
Se eligió Oracle Free 23.26.3-slim, publicada para amd64, con healthcheck oficial
de la imagen y creación APP_USER. Contraseñas en archivos ignorados mediante
Compose secrets; Oracle no publica puerto al host.

Wrapper oficial 3.3.4 only-script y Maven 3.9.9 descargados de Maven Central.
La consulta .sha512 del wrapper respondió 404. Se verificaron los checksums .sha1
publicados de ambas descargas y se calculó/fijó SHA-256 de la distribución Maven.
No se instaló Maven global ni se escribió un wrapper propio.

## 4. Límite de las comprobaciones

La sonda Java usa JDBC real, usuario BOOKING y verifica versión/schema/PDB.
La sonda Angular compila un componente standalone y tipos de MSAL; no implementa
autenticación ni una interfaz de reservas. El POM de infraestructura no genera
un WAR vacío. El WAR deberá configurarse y comprobarse con el starter.

Fuentes oficiales confirman la incompatibilidad de runtime Boot 3/WebLogic 12.2.1.4.
MSAL 3 con Angular 20 requiere validación funcional posterior aunque resuelva peers.
Resultados y fallos reales del arranque se registran en VERIFICACION_LOCAL.md.
No se atribuyen al usuario decisiones o rechazos que no haya expresado.
## 5. Verificación y corrección de dependencias

El primer ciclo completó Maven Wrapper, resolución del BOM, JDBC a Oracle real,
npm ci, ngc strict y persistencia tras restart. Oracle informó:
Oracle AI Database 26ai Free Release 23.26.3.0.0; driver 21.9.0.0.0;
schema BOOKING y PDB FREEPDB1.

npm audit detectó 23 paquetes vulnerables (1 crítico, 13 altos, 8 moderados,
1 bajo) con las versiones candidatas. Se verificaron los parches disponibles:
core/compiler Angular 20.3.31 y CLI 20.3.37, dentro de la rama obligatoria.
Decisión del agente: aplicarlos conservando los demás paquetes compatibles.
No se ejecutó npm audit fix --force.

El primer intento de actualizar el lockfile produjo ERESOLVE por referencias
cruzadas a Angular 20.3.0. Se resolvió el manifiesto actualizado en una carpeta
temporal limpia (.local/npm-resolution), sin forzar peers, y se copió el lockfile
resultante. Esa resolución reportó cero vulnerabilidades. El árbol anterior
permanece en el commit de infraestructura, no se inventó un defecto del starter.

La CLI Dev Containers 0.89.0 de VS Code leyó devcontainer.json correctamente.
Un primer intento con --log-level error falló porque admite info/debug/trace;
se corrigió el argumento. No fue un fallo de la configuración del proyecto.

Se ajustó el Dockerfile para inicializar el volumen node_modules con propietario
node. El volumen creado en el primer build se corrigió con chown sobre ese único
directorio. No se borraron volúmenes ni se modificaron instalaciones ajenas.

## 6. Cierre de infraestructura

Dev Containers up terminó con outcome=success para booking-local, usuario node y
/workspace, y ejecutó postCreateCommand completo. La compilación con los parches
y npm ci terminaron correctamente, con cero vulnerabilidades reportadas por npm.
La prueba JDBC confirmó persistencia tanto después de restart como de stop/up.

Se detectó dubious ownership en Git sobre el bind mount Windows; la corrección
se limita a safe.directory=/workspace dentro del contenedor y queda en el script
de verificación. Nunca se añadió safe.directory=*.
El árbol Maven real confirmó Framework 6.1.21, Security 6.3.10, Data JPA 3.3.13,
Hibernate 6.5.3.Final y ojdbc11 21.9.0.0. La consulta se corrigió al pasar a bash
un argumento -DoutputFile que PowerShell había dividido; no fue un fallo del BOM.

Resultado y límites completos en docs/VERIFICACION_LOCAL.md. El entorno queda
local y activo. Starter, aplicación, WAR y verificación WebLogic siguen pendientes.
No se implementaron funcionalidades de negocio ni se publicó nada.

## 7. Transferencia a otro Windows 10 — 2026-09-14

El usuario pidió subir el trabajo y las imágenes a luzadri84/AIFORGESQUADP1,
conservar también los datos y cambiar el repositorio a privado antes de subir.
El conector inicialmente autenticado como visualito no tenía permiso de escritura.
Git Credential Manager terminó disponiendo de una sesión de luzadri84; se verificó
esa identidad mediante API y se cambió la visibilidad a private, con push=true.

Se exportaron las imágenes de desarrollo y Oracle, con SHA-256, y se comprobó
su importación con docker load. El primer exportador tenía una función llamada
Docker que ocultaba docker.exe; se corrigió el nombre y se repitió con éxito.
Los bundles se limitan a ramas, tags y HEAD: no incluyen refs internas de herramientas.

Se agregó respaldo consistente y restauración de Oracle con comprobación de
volumen vacío y credenciales coherentes. El respaldo se generó con Oracle detenido,
se verificó gzip y el listado tar, se reinició el servicio y JDBC confirmó que
el marcador original seguía presente. No se borró ningún volumen.

Para subir también datos/credenciales, se usa un sobre CMS cifrado con
AES-256-GCM, RSA de 3072 bits y OAEP SHA-256 mediante OpenSSL 3.0.18 ya instalado.
La clave privada queda fuera de Git y de los archivos que se envían a GitHub.
La prueba de cifrado compara el tar descifrado con el original byte a byte.
La restauración sobre otro equipo solo se acredita después de ejecutarla allí.

## 8. Subida y restauración verificadas

La rama main se subió al repositorio privado. La Release local-env-20260914
contiene ocho archivos; cada digest SHA-256 de GitHub coincidió con el archivo local.
El código y el bundle de la Release corresponden al commit 31782c5.

El respaldo Oracle se extrajo a un volumen independiente nuevo y se arrancó
oracle-restore-check sobre él. El healthcheck pasó. JDBC se dirigió explícitamente
a ese servicio y leyó el marcador original como BOOKING/FREEPDB1. Después se detuvo
el contenedor de comprobación, conservando ese volumen y la base original.

La clave de restauración permanece únicamente en .local/transfer-private.
El resultado completo y el enlace de descarga están en docs/TRANSFERENCIA_VERIFICADA.md.
La comprobación real en el otro Windows todavía corresponde al paso de importación.
