# Transferencia cifrada del entorno original

Este procedimiento se conserva porque los scripts de transferencia siguen disponibles.
La Release `local-env-20260914` corresponde a **31782c5**: infraestructura, imágenes,
credenciales y datos Oracle de aquella etapa; aún no contenía Booking ni WAR. **No es
un respaldo de las reservas actuales ni de la aplicación terminada.** Clonar main o un
bundle actual no actualiza por sí mismo ese respaldo. Para una instalación nueva sin
secretos del autor, seguir [README A](../README.md); para la actual, [README B](../README.md).
La restauración anterior y el marcador conservado están registrados en [BITACORA](../BITACORA.md).

## Restaurar ese paquete en Windows, sin sobrescribir otro entorno

Requisitos: Git autenticado con acceso al [repositorio privado](https://github.com/luzadri84/AIFORGESQUADP1),
Docker Linux amd64/Compose y **PowerShell 7 (`pwsh`)**, no Windows PowerShell 5.1.
Memoria/disco/puertos: [README](../README.md). Clave privada entregada por separado.
No descargar ni mostrar tokens de GitHub. Inspeccionar carpeta, contenedores y volúmenes:
si booking-local contiene datos, preparar otro destino aislado, no reemplazarlos.

1. Clonar en carpeta nueva y leer los scripts actuales. Descargar los **ocho** adjuntos de
   [la Release](https://github.com/luzadri84/AIFORGESQUADP1/releases/tag/local-env-20260914)
   en `.local/transfer`: `dev-linux-amd64.tar.gz`, `oracle-linux-amd64.tar.gz`,
   `oracle-state.cms`, `oracle-state.cms.sha256`, `compose.images.yaml`, `manifest.json`,
   `booking-source.bundle`, `SHA256SUMS.txt`.
2. Comprobar los SHA-256 contra SHA256SUMS.txt y el checksum del CMS antes de usar el
   paquete. Los importadores validan sus manifiestos/checksums; el bundle es historia
   de aquella etapa, no la aplicación posterior. No tratar un hash como firma de autor.
3. Copiar la clave recibida a `.local/restore-key.pem`. No subirla, imprimirla ni añadirla
   a Git. Conservar respaldo privado de la clave; sin ella no se descifra.
4. Desde raíz, restaurar **datos y credenciales antes de prepare y antes de iniciar Oracle**:

```powershell
pwsh -NoProfile -File scripts/transfer-images.ps1 import
pwsh -NoProfile -File scripts/transfer-oracle-data.ps1 decrypt -KeyFile .local/restore-key.pem
pwsh -NoProfile -File scripts/transfer-oracle-data.ps1 import
pwsh -NoProfile -File scripts/local.ps1 prepare
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml up -d --no-build --wait --wait-timeout 600
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash scripts/verify-local.sh
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash scripts/jdbc-check.sh read
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml restart oracle
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml up -d --no-build --wait --wait-timeout 600
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash scripts/jdbc-check.sh read
```

Import falla si volumen/credenciales de destino entran en conflicto. No borrar para
forzar éxito. **Nunca `jdbc-check.sh write` antes de comprobar el marcador original.**
Las cachés Maven/npm no viajaron: puede requerirse Internet. Usar imágenes importadas,
ambos Compose y --no-build. Puertos publicados solo en localhost.

La Release de infraestructura no contiene el esquema BKG posterior: no afirmar que
arrancar este paquete recupera las reservas actuales. Restaurar una exportación posterior
requiere su conjunto completo, revisión y clave correspondientes. No aplicar schema/seed
sobre datos restaurados sin inspección y autorización del alcance de esa operación.
El operador nuevo protege `schema` contra restauraciones deliberadamente.

## Operación y futuras copias

Para detener sin perder datos: `docker compose -f compose.yaml -f .local/transfer/compose.images.yaml stop`.
Arrancar con el mismo up anterior. Dev Container actual usa los overlays restaurados;
[README](../README.md) reemplaza las instrucciones antiguas de reconstruir automáticamente.

Exportadores conservados: `scripts/transfer-images.ps1 export`,
`scripts/transfer-oracle-data.ps1 export` y `scripts/transfer-oracle-data.ps1 encrypt`,
ejecutados con pwsh -NoProfile -File. Revisar sus parámetros y usar `-Directory` con una
carpeta nueva para cada exportación; no mezclar paquetes ni sobrescribir claves.
Oracle se respalda detenido, volumen montado solo lectura, y vuelve a arrancar en finally.
No usar docker commit. El cifrado CMS usa AES-256-GCM/RSA-OAEP. Conservar imágenes,
bundle, manifiestos, checksums y estado cifrado juntos; clave por canal separado.
**No se hizo una nueva exportación ni publicación durante la consolidación documental.**

Fuentes del mecanismo: [Docker save](https://docs.docker.com/reference/cli/docker/image/save/)
y [OpenSSL CMS](https://docs.openssl.org/3.0/man1/openssl-cms/).
