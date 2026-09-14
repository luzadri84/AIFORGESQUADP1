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