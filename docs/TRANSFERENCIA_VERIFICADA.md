# Transferencia verificada — 2026-09-14

Repositorio privado: [luzadri84/AIFORGESQUADP1](https://github.com/luzadri84/AIFORGESQUADP1).
Paquete completo: [Release local-env-20260914](https://github.com/luzadri84/AIFORGESQUADP1/releases/tag/local-env-20260914).

Se subieron ocho archivos y se contrastó el digest SHA-256 devuelto por GitHub
con el hash de cada archivo local:

| Archivo | Contenido |
|---|---|
| dev-linux-amd64.tar.gz | Imagen de desarrollo, aproximadamente 567 MiB |
| oracle-linux-amd64.tar.gz | Imagen Oracle, aproximadamente 849 MiB |
| oracle-state.cms | Datos Oracle y credenciales cifrados, aproximadamente 423 MiB |
| oracle-state.cms.sha256 | Hash del respaldo cifrado |
| compose.images.yaml | Configuración para usar las imágenes importadas sin build |
| manifest.json | Imágenes, arquitectura, tamaños, hashes y commit del paquete |
| booking-source.bundle | Historial Git de la versión empaquetada |
| SHA256SUMS.txt | Hashes de los archivos de transferencia |

El paquete corresponde al commit 31782c5f023f90b48c9e4452ce2bc9662af62bd5.
La rama main añade posteriormente esta constancia de verificación; no cambia
los scripts ni las imágenes del paquete.

## Comprobaciones realizadas

- Ambas imágenes se importaron con docker load y se verificó Linux amd64.
- El volumen Oracle se copió después de una parada completa y volvió a arrancar.
- Se comprobó integridad gzip y estructura tar del respaldo.
- El cifrado CMS AES-256-GCM se descifró y el tar coincidió byte a byte con el original.
- Se restauró el respaldo en el volumen independiente booking-transfer-restore-check.
- Un Oracle separado, oracle-restore-check, pasó su healthcheck.
- JDBC se conectó a ese Oracle restaurado, como BOOKING en FREEPDB1, y encontró
  el marcador original. No se creó otro marcador para pasar la prueba.
- El contenedor de comprobación quedó detenido; su volumen de prueba se conservó.
  El Oracle original sigue activo y sus datos no fueron sobrescritos.
- GitHub recibió la rama main y la Release quedó publicada dentro del repositorio privado.
- La clave privada, credenciales sin cifrar, cachés y respaldos sin cifrar no se subieron.

## Archivo que debe llevarse aparte

La clave está en el equipo origen, dentro de .local/transfer-private/restore-key.pem.
No está en GitHub. Guárdela por separado antes de dejar este equipo.
El [procedimiento de restauración](TRANSFERIR_EQUIPO.md) indica cómo usarla en Windows 10.

La recuperación en un volumen independiente fue probada aquí. El arranque en el
otro Windows todavía debe ejecutarse; exige Docker Linux amd64, Git y PowerShell 7.
La aplicación Booking y su WAR siguen pendientes del starter.