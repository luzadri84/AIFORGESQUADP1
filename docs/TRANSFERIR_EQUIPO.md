# Continuar en otro equipo Windows 10

Repositorio de destino: [luzadri84/AIFORGESQUADP1](https://github.com/luzadri84/AIFORGESQUADP1).
Se verificó la sesión de luzadri84 y se cambió el repositorio a **privado** antes de subir.
Paquete de transferencia: sección **Releases**, etiqueta local-env-20260914.

## Qué contiene

- Git: infraestructura, documentación y commits; no hay todavía aplicación Booking ni WAR.
- Dos imágenes Docker ya construidas: desarrollo (Java/Node/Git) y Oracle.
- Respaldo consistente de Oracle, con sus credenciales y marcador de persistencia,
  cifrado mediante OpenSSL CMS, AES-256-GCM y RSA-OAEP.
- Una copia del historial Git en booking-source.bundle, por si se traslada sin clonar GitHub.

La clave privada **restore-key.pem no se sube a GitHub**. En el equipo origen
está en .local/transfer-private/restore-key.pem. Guárdela aparte y llévela al otro
equipo por USB u otro canal privado. Sin esa clave no se puede recuperar el respaldo.
No copie la carpeta privada completa a un repositorio.

Las imágenes no contienen los volúmenes ni la capa mutable de los contenedores.
El volumen Oracle va en el respaldo cifrado separado. Las cachés Maven/npm no se
transfieren: la primera verificación recupera dependencias desde Internet usando
Maven Wrapper y el lockfile npm.

## Preparación del otro equipo

Se necesitan Docker con Compose y **motor Linux amd64 operativo**, Git y
**PowerShell 7 (pwsh)**. Windows 10 trae Windows PowerShell 5.1; no es suficiente
para estos scripts. El importador detecta plataformas diferentes y no fuerza
emulación. VS Code y Dev Containers son opcionales para editar.

Compruebe docker version, docker compose version, git --version y pwsh --version.
La arquitectura esperada es Intel/AMD x64. Si Docker muestra windows/amd64 en
lugar de Linux, cambie Docker Desktop a contenedores Linux antes de importar.

## Restaurar conservando Oracle

1. Clone el repositorio privado e ingrese a su carpeta. Use una carpeta nueva,
   sin otra base booking-local inicializada.
2. Descargue los archivos adjuntos de la Release y colóquelos en .local/transfer:
   dev-linux-amd64.tar.gz, oracle-linux-amd64.tar.gz, compose.images.yaml,
   manifest.json, oracle-state.cms, oracle-state.cms.sha256 y SHA256SUMS.txt.
   booking-source.bundle es una copia opcional del historial.
3. Copie la clave recibida por separado a .local/restore-key.pem.
4. Desde la raíz del clon, ejecute en este orden:

```powershell
pwsh -NoProfile -File scripts/transfer-images.ps1 import
pwsh -NoProfile -File scripts/transfer-oracle-data.ps1 decrypt -KeyFile .local/restore-key.pem
pwsh -NoProfile -File scripts/transfer-oracle-data.ps1 import
pwsh -NoProfile -File scripts/local.ps1 prepare
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml up -d --no-build --wait --wait-timeout 600
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash scripts/verify-local.sh
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash scripts/jdbc-check.sh read
```

Importar los datos **antes de prepare** permite conservar sus contraseñas.
El importador falla si el volumen destino contiene archivos o si ya existen
credenciales diferentes; no sobrescribe una base existente.

La última comprobación lee el marcador original del respaldo. No lo reemplaza
por un dato nuevo para aparentar que sobrevivió a la transferencia.
Los puertos siguen limitados a 127.0.0.1. No hay todavía una URL funcional de Booking.

Para parar conservando todos los datos:

```powershell
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml stop
```

Para volver a arrancar use el mismo comando up con ambos archivos Compose y
--no-build. El override selecciona las imágenes importadas y pull_policy=never.
El devcontainer normal puede reconstruir el Dockerfile y descargar sus bases.
Para reutilizar directamente el contenedor importado, VS Code permite
**Dev Containers: Attach to Running Container** y abrir /workspace.

## Usar una base nueva

Si decide no restaurar datos, omita decrypt/import de Oracle. Ejecute import de
imágenes, prepare y el arranque con ambos archivos Compose. En ese caso se generan
contraseñas nuevas y Oracle inicializa una base vacía.

## Exportar otra vez

En el equipo origen:

```powershell
pwsh -NoProfile -File scripts/transfer-images.ps1 export
pwsh -NoProfile -File scripts/transfer-oracle-data.ps1 export
pwsh -NoProfile -File scripts/transfer-oracle-data.ps1 encrypt
```

Los exportadores conservan paquetes terminados; use -Directory con otra carpeta
para una copia posterior. El cifrado escribe oracle-state.cms en .local/transfer;
conserve cada conjunto completo y su clave antes de generar otra copia cifrada.

El respaldo se hace con Oracle detenido y un montaje del volumen en solo lectura.
Se vuelve a arrancar en finally. No se usa docker commit ni se borra la base.
GitHub recibe las imágenes, el bundle y el archivo cifrado; nunca los archivos
oracle-password, app-password, restore-key.pem o el respaldo sin cifrar.

Fuentes: [Docker save](https://docs.docker.com/reference/cli/docker/image/save/),
[OpenSSL CMS](https://docs.openssl.org/3.0/man1/openssl-cms/),
[archivos grandes en GitHub](https://docs.github.com/en/repositories/working-with-files/managing-large-files/about-large-files-on-github).