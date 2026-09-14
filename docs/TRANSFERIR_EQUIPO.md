# Continuar en otro equipo Windows

Destino solicitado: https://github.com/luzadri84/AIFORGESQUADP1.
La subida requiere autenticar Git con una cuenta que pueda escribir allí.
El estado de publicación se registra al terminar; este archivo por sí solo no
acredita que ya se haya subido el repositorio.

## Qué se traslada

- GitHub: código de infraestructura, documentación y commits.
- Paquete de imágenes: desarrollo (Java/Node/Git) y Oracle ya descargados.
- No se copian contraseñas, correo, datos Oracle ni cachés Maven/npm al repositorio.

Las imágenes se exportan con docker image save y se importan con docker image load.
No se usa docker commit: el contenido mutable de un contenedor o sus volúmenes no
forma parte de esta exportación. Maven y dependencias npm se recuperan con los
wrappers/lockfile al verificar, por lo que esa primera verificación requiere Internet.

El paquete actual es Linux amd64, para Windows Intel/AMD con Docker en modo Linux.
El script detecta otras arquitecturas o un daemon Windows y falla claramente.
No supone que la mera instalación de Docker garantice que su motor funciona.

## Exportar de nuevo en el equipo origen

Desde la raíz del repositorio, con PowerShell 7 y las imágenes locales disponibles:

```powershell
pwsh -NoProfile -File scripts/transfer-images.ps1 export
```

Se genera .local/transfer, excluida de Git, con:

- dev-linux-amd64.tar.gz y oracle-linux-amd64.tar.gz.
- compose.images.yaml y manifest.json.
- booking-source.bundle: copia del historial Git.
- SHA256SUMS.txt: hashes de integridad.

Los .tar sin comprimir son intermediarios y no es necesario transferirlos.
No sobrescribe un paquete terminado; para una exportación posterior use
-Directory .local/transfer-otra-fecha.

## En el otro equipo

Además de Docker con Compose y motor Linux funcionando, instale Git y PowerShell 7
si no están disponibles. VS Code y Dev Containers son opcionales para editar.

1. Clone el repositorio indicado una vez confirmada la subida. Alternativamente,
   clone el bundle sin GitHub: git clone RUTA/booking-source.bundle AIFORGESQUADP1.
2. Entre a la raíz del clon y copie los archivos del paquete a .local/transfer.
3. Ejecute:

```powershell
pwsh -NoProfile -File scripts/local.ps1 prepare
pwsh -NoProfile -File scripts/transfer-images.ps1 import
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml up -d --no-build --wait --wait-timeout 600
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash scripts/verify-local.sh
```

prepare genera contraseñas nuevas para una base Oracle nueva. Si se decide migrar
el volumen existente, se necesita un respaldo consistente separado y conservar sus
credenciales mediante un canal privado. No restaure un volumen inicializado con
contraseñas nuevas esperando que estas cambien la base.

Import valida los SHA-256 antes de cargar ambas imágenes. El override selecciona
las etiquetas exportadas, pull_policy=never y --no-build evita reconstruirlas.
Los puertos siguen limitados a 127.0.0.1. Para detener conservando datos:

```powershell
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml stop
```

Si utiliza el devcontainer normal sin el override, puede reconstruir el Dockerfile.
Eso es válido y reproducible, pero puede volver a descargar sus imágenes base.
No hay todavía aplicación Booking ni WAR en este repositorio provisional.

## Dónde guardar las imágenes

Los archivos grandes no van en Git. Se pueden copiar por USB, carpeta compartida
o un almacenamiento privado. GitHub Container Registry también admite imágenes,
pero requiere autenticación y permisos propios para publicar paquetes.

Fuentes: [Docker save](https://docs.docker.com/reference/cli/docker/image/save/),
[GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
y [límites de archivos GitHub](https://docs.github.com/en/repositories/working-with-files/managing-large-files/about-large-files-on-github).