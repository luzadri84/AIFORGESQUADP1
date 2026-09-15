# Reproducir auditoría y correcciones

Estado vigente: docs/CIERRE_CORRECCIONES_AUDITORIA.md. La ejecución corregida incluye
88 casos Java y pasa; T018 sigue parcial. El procedimiento T017 inferior es histórico
y sus afirmaciones de fallo/76 casos se refieren exclusivamente a47a6af4/e170418.
Para verificación actual: pwsh -NoProfile -File scripts/app.ps1 verify.

## Procedimiento histórico T017 / DEC-015

Desde C:\PruebaAIFORGESQUAD, PowerShell7, con entorno/restauración y secretos ya preparados. No ejecutar prepare/import ni escribir el marcador. Se requiere Oracle real y los dos Compose. No reemplazar credenciales. Los fixtures automáticos son transaccionales o se limpian por su propio ID. Los tests usan contraseñas sintéticas aleatorias, no imprimen las operativas.

## Pruebas

```powershell
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash scripts/app-process.sh stop
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash -c 'bash mvnw -B -ntp -f backend/pom.xml verify'
# Resultado esperado actual: FALLA por seis regresiones de auditoría. No omitirlas.
# En T017, 75 casos en la primera ejecución + una prueba nueva de espera después:
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash -c 'bash mvnw -B -ntp -f backend/pom.xml -Dtest=OracleConcurrencyTest#auditLockTimeoutReturns503AndDoesNotCommit test'
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash -c 'cd frontend && npm test && npm run build'
# Ejecutar incluso cuando Maven haya fallado: restaura la app desde el WAR conservado.
pwsh -NoProfile -File scripts/app.ps1 start
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash scripts/jdbc-check.sh read
```

No ejecutar `app.ps1 verify` esperando un resultado verde: incluye las nuevas regresiones y puede detener procesos antes del fallo. No borrar WAR/volumen ni saltar tests. Su próximo verify completo descubrirá 76 casos Java. Los logs Surefire nuevos pueden reemplazar los anteriores; extractos de las ejecuciones auditadas se conservan en docs/audit-evidence/executions.txt.

## Dependencias sin enviar inventario a servicios externos

```powershell
New-Item -ItemType Directory -Force .local/audit | Out-Null
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash -c 'bash mvnw -B -ntp -f backend/pom.xml dependency:tree -DoutputType=text -DoutputFile=/workspace/.local/audit/maven-tree.txt'
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash -c 'bash mvnw -B -ntp -f backend/pom.xml help:active-profiles'
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash -c 'cd frontend && npm ls --all --json > /workspace/.local/audit/npm-tree.json'
# Descarga catálogos públicos COMPLETOS de Maven/npm, no consultas por paquetes.
python scripts/audit/prepare_advisories.py --download
# Si los dos ZIP ya existen, omitir --download para reutilizarlos.
python -m unittest discover -s scripts/audit -p test_range_match.py
python scripts/audit/match_advisories.py
```

Prepare extrae coordenadas del árbol Maven real y del lock npm (incluye opcionales/dev), descarta avisos withdrawn y filtra los catálogos en local. Match usa ComparableVersion de Maven3.9.9 y semver instalado del frontend, evalúa unión de intervalos inclusive/exclusive y versiones explícitas. No usa una comparación lexicográfica de versiones. Los tres tests del comparador comprueban fronteras/intervalos, no certifican exhaustividad del catálogo. Se inspeccionaron tipos ECOSYSTEM/SEMVER; no se afirma soporte para rangos Git. No son pruebas de explotación ni análisis de alcanzabilidad. Scripts sin llamadas de actualización/install/fix; solo --download tiene red hacia catálogos públicos completos.

Hashes de catálogos, inventario y anexo fijan la foto auditada; ejecuciones futuras pueden cambiar. Reporte de trabajo .local/audit/dependency-findings.json. El plugin Maven instalado ignoró outputType=json en la ejecución original y escribió árbol de texto; por eso el procedimiento reproducible pide text. No interpretar ese archivo como JSON.

## Otros controles

Se ejecutaron `git status --short`, `git rev-parse HEAD`, `git log`, `git rev-list --objects --all` y lectura en lote `git cat-file --batch`. La comparación de secretos usa en memoria valores de archivos locales más patrones de credenciales; no imprimir esos valores ni pasar contraseñas como argumentos. No enviar fuentes/inventarios a scanners externos. La cobertura histórica y sus límites están en docs/audit-evidence/secrets-history.json.

Privilegios: consultar desde SQL*Plus BOOKING `USER_ROLE_PRIVS`, `SESSION_ROLES`, `SESSION_PRIVS`, `USER_TS_QUOTAS`; el patrón seguro de conexión por stdin está en scripts/booking-db.ps1. Inventario runtime: Compose ps, docker inspect y docker image inspect/history, siempre redactando env/valores. No publicar salidas crudas de inspección.

E2E manual reproducible: usar dos identidades locales configuradas sin imprimir claves; escoger fechas/espacios libres para fixtures propios, reservar la segunda de cuatro semanas con A, solicitar cuatro con B, comprobar 3+1 y luego 0+4, listados separados, cancelar cada fixture con su dueño y verificar SQL. No cancelar filas preexistentes. Para texto inocuo crear un espacio propio con nombre HTML literal y retirarlo solo cuando no tenga reservas. Teclado: Tab entre controles de acceso. No se exige navegador automatizado instalado fuera de la herramienta ya disponible.
