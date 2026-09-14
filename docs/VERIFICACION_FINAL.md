# Verificación de la solución local

Ejecutada por Codex el 2026-09-14, America/Bogota, en `C:\PruebaAIFORGESQUAD`.
Cierre T013/DEC-013 sobre H006/0344b9a. Evidencia de etapas previas en bitácora y
handoffs; estados únicamente en [tasks.md](../specs/001-booking-espacios/tasks.md).

## Resultado reproducible

```powershell
Set-Location C:\PruebaAIFORGESQUAD
pwsh -NoProfile -File scripts/app.ps1 verify
pwsh -NoProfile -File scripts/app.ps1 stop
pwsh -NoProfile -File scripts/app.ps1 start
pwsh -NoProfile -File scripts/app.ps1 status
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash scripts/jdbc-check.sh read
pwsh -NoProfile -File scripts/booking-db.ps1 status
```

Todos ejecutados correctamente. verify ejecuta Maven verify, ngc strict, ng build,
node test, lectura de marcador y arranque. El ciclo stop/start detuvo realmente ambos
contenedores, esperó Oracle saludable y volvió a iniciar WAR/Angular. No se eliminaron
volúmenes ni se reconstruyeron imágenes. Después se leyó el mismo marcador original.
Login Basic en navegador posterior al reinicio: ana, catálogo de tres espacios y lista
propia vacía; MSAL opcional no interfiere. UI http://localhost:4200/, API :8080.

## Pruebas Java: 50 correctas, 0 fallos, 0 errores, 0 omitidas

| Clase (backend/src/test/java/local/booking) | Casos | Evidencia |
|---|---:|---|
| booking/OraclePersistenceTest | 23 | BOOKING/FREEPDB1, mapeo, precisión, constraints/seed, 10 fronteras del query, cancelada/otro espacio |
| booking/TimeRangeTest | 10 | Intervalos puros: parciales, igualdad, contención, adyacencias y separados |
| booking/WeeklyRecurrenceTest | 5 | Primera incluida, hora Bogotá, límites e internos |
| booking/BookingApiTest | 4 | MockMvc + Oracle: contrato, propiedad, cancelación, parcial y límites |
| booking/OracleConcurrencyTest | 2 | HTTP real concurrente 201/409/1 fila; excepción tras flush revierte a 0 |
| security/SecurityContractTest | 3 | Basic/CSRF y validación MVC de DTO; ruta de probe solo de test |
| security/HttpSecurityTest | 1 | HTTP/cookies reales, GET paralelos, escrituras y cookie sin Basic |
| security/JwtValidationTest | 2 | Claims y principal; no firma real ni Azure |

Los 11 casos adicionales de persistencia en H007 se añadieron porque la tabla de
intervalos debía probar también el query Oracle, además del helper puro. Último verify
completo pasó los 50. XML de Surefire en `backend/target/surefire-reports/` (ignorado).
Mocks/spy no sustituyen Oracle: para concurrencia observan/delegan; para rollback
inyectan el fallo después de escritura real dentro de una transacción.

## Frontend y navegador

`npm run check` (ngc), `npm run build` strict: correctos. `npm test`: 1/1, con nueve
URLs en el escenario que restringe credenciales a API propia. `npm ls --depth=0`:
dependencias válidas, sin peers forzados. `npm audit --json`: 0 vulnerabilidades para
el lockfile frontend consultado; no equivale a auditoría integral ni cubre Java.

Recorridos manuales reales con la herramienta de navegador:

- H004: ana crea/lista #55, duplicado rechazado y cancelación; lista vacía.
- H005: ana crea #72 en segunda semana; bruno no la ve. Cuatro semanas solicitadas
  por bruno: #73–75 creadas y segunda rechazada, con fechas y sin identidad ajena.
  Cancelación posterior de cada reserva por su propietario.
- H007: después de reiniciar Oracle y app, acceso Basic correcto, catálogo/listado
  cargados; revisión visual de formulario, estados y textos en español.

Persisten cinco filas de prueba visual **CANCELLED**, sin reservas activas residuales
ni borrado de esa historia. Las pruebas automáticas revierten transacciones o limpian
solo los IDs de sus espacios temporales. Las secuencias pueden avanzar por los tests.

## Persistencia, secretos y artefactos

JDBC confirmó BOOKING y FREEPDB1 y coincidencia con el marcador original después del
reinicio real. Comparación byte a byte de app-password/oracle-password y token contra
copias privadas de transferencia: iguales, sin imprimir valores. No se invocó write.
SQL status: tres tablas (incluida ENVIRONMENT_PROBE), tres espacios y cinco reservas.

`git check-ignore` confirmó clave restore-key.pem, credenciales Basic y Oracle, token,
.env y artefactos. Revisión de valores privados en archivos versionados/candidatos:
sin coincidencias. WAR contiene BookingApplication y Tomcat en WEB-INF/lib-provided,
sin .local, PEM ni booking.properties. Es WAR ejecutable local, no despliegue WebLogic.

Compose/devcontainer/infra/scripts originales sin diff frente a 992bc41. Imágenes
booking-transfer/dev:3d485c6533be y oracle:6d61d267a3b9 reutilizadas. Publicación exclusiva
127.0.0.1:4200 y 127.0.0.1:8080; Oracle no expone puertos. Procesos de app activos y
Oracle healthy. T015 conserva literalmente su aceptación/bloqueo frente a 8ba745a.

## Fallos encontrados y corregidos; límites

La bitácora conserva el detalle y orden real: rotación de token/sesión CSRF descubierta
por tests y navegador, spy sobre proxy abstracto corregido para delegar implementación
real, error inesperado redirigido como 401 corregido a 500 genérico, codificación Windows
normalizada a UTF-8 y polling para archivos montados. No son un defecto sembrado ni un
sustituto. Se ajustó entrecomillado PowerShell para obtener árbol Maven.
Avisos no bloqueantes: deprecación de animations/themes fijados y aviso Node sobre
detección ESM del test TypeScript; builds y tests pasan sin relajar strict.

No probado: Azure real, firmas/JWKS empresariales, carga/rendimiento, auditoría Java
integral ni despliegue WebLogic. T015 sigue externo; T014 No aplica por DEC-004. Sin
publicación, push ni implementación de ejercicios. Logs locales ignorados:
`.local/h007-final-verify.log`, `.local/h007-stop-start.log`, `.local/h006-build.log`,
`.local/frontend-audit.json`, `.local/backend-dependencies.txt`; no se versionan logs masivos.
Explicación, decisiones y pruebas rastreables en [EXPLICACION_IMPLEMENTACION](EXPLICACION_IMPLEMENTACION.md)
y [BITACORA](BITACORA.md).
