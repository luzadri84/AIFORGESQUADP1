# Verificación posterior de correcciones T018–T022

Fecha:2026-09-14 America/Bogota (registros UTC pueden corresponder al día siguiente).
La auditoría47a6af4/e170418 se conserva con sus seis fallos históricos. Este anexo
registra la ejecución posterior; no constituye certificación absoluta de seguridad.
Estado canónico: specs/001-booking-espacios/tasks.md.

## Resultado por tarea

| Tarea | Resultado y límite |
|---|---|
| T018 | Parcial. Tomcat10.1.59 alineado,22 coincidencias previas corregidas por versión. Mitigación oficial eager headers probada antes/después. Quedan43 coincidencias:1 mitigada,13 no aplicables a configuración actual con evidencia,29 pendientes. Usuario mantiene familias obligatorias; alternativa excepcional NO aplicada. |
| T019 | Completada. Dos decimales rechazados400 antes del servicio; cero escrituras en regresiones, entero/null/límites probados. HTTP real confirma rechazo sobre WAR nuevo. |
| T020 | Completada. Desconocida404, ID inválido400, método no admitido405 con Allow;500 inesperado/503 técnico conservados. Se justificó primero en DEC-017 GET sobre ruta DELETE=405. |
| T021 | Implementada, verificación parcial. OpenAPI/Basic+CSRF y respuestas pasan pruebas/HTTP. Falta ejecutar escritura desde Swagger UI tras autenticación nativa del usuario; no declarar cierre. |
| T022 | Completada para entorno restaurado: CLI up/exec, herramientas, compilación, aplicación y marcador original. Instrucciones/bootstrap de base nueva revisados; no instalación limpia ni apertura visual de VS Code acreditadas. |
| T015 | Pendiente externo independiente; Tomcat local no acredita WebLogic12.2.1.4. |

T023/T024 no implementadas. Arquitectura por funcionalidades, datos, credenciales e
historial conservados. No starter, migración ni defecto sembrado sustituto.

## Revisión y artefacto probado

Fuentes: `e859c802d2c66967b4f3d5aa571575929cefcad1`, sin cambios productivos al construir.
WAR: `backend/target/booking.war`, construido2026-09-14T23:52:53Z.
SHA256: `a2ab5fe1a6d301d45a94fc7f26415326b7e1fdfbc2567e37f527e269a7d13aa5`.
Los commits posteriores de documentación no cambian sus fuentes. Metadata local:
.local/runtime/backend-build.properties; start coteja fuente y WAR antes de reutilizar.
Se usó clean verify, no -DskipTests ni el WAR anterior. Tomcat core/websocket están
en WEB-INF/lib-provided, el en WEB-INF/lib; los tres10.1.59.76 JAR, sin XMLUnit/AssertJ
ni Artemis. [Inventario WAR](correction-evidence/war-libraries.txt).

## Ejecuciones reales

- Una suite Java completa mediante Dev Container CLI exec: **88 tests,0 fallos,0
  errores,0 omitidas**, clean verify correcto. Incluye seis regresiones originales,
  coerción/formato, Basic/CSRF/propiedad, rollback/carrera/espera Oracle y cabeceras503.
- Frontend desde CLI exec: **13/13**, build Angular strict correcto; flags no relajados.
- Comparador de rangos:3/3. Inventario resuelto final actualizado, catálogo completo
  filtrado localmente:43 Maven/0 npm. No se enviaron coordenadas privadas a servicios.
- HTTP real al WAR8080:12 comprobaciones, incluidos dos cuerpos fraccionarios400,
  lista antes/después igual,401/403/404/400/405 y Allow, cabeceras nosniff/DENY/no-store,
  OpenAPI con Basic AND csrfToken. Las pruebas de Oracle confirman503 con cabeceras;
  no se detuvo Oracle deliberadamente para simularlo en el servicio del usuario.
- Navegador contra ese WAR vía Angular4200: ana creó#256 (13/06/2044), vio su listado
  y recibió rechazo al duplicar. Bruno no vio esa reserva; solicitó cuatro semanas
  desde06/06/2044:3 creadas (#257–259) y1 rechazada identificada (segunda semana).
  Cancelaciones visuales confirmadas y SQL: las cuatro quedan CANCELLED.
- Al alternar dos pestañas durante cancelación se observó «Acceso no válido» en dos
  intentos. Reingresar como su dueño permitió completar cancelación; no se atribuye
  sin traza una causa interna ni se presenta el recorrido como libre de incidencias.
- Dev Container CLI0.89.0 reutilizó contenedor58b20a069dfa, usuario node/workspace.
  Primer postCreate falló por CRLF introducido durante edición Bash; corregido LF.
  Segundo up exitoso; CLI omitió lifecycle ya intentado, así que postCreate corregido
  se ejecutó explícitamente por exec y pasó. Java21.0.10/Node22.22/Maven3.9.9 y sonda
  Angular correctos. build/test y HTTP API200/frontend200 comprobados por CLI exec.
- Bootstrap nuevo rechaza .local existente ANTES de generar secretos. Procedimiento
  nuevo usa imágenes públicas fijadas; no se ejecutó instalación desde cero aquí.

Resultados reducidos: [verification.json](correction-evidence/verification.json),
[HTTP](correction-evidence/http-results.json), [OpenAPI efectivo](correction-evidence/openapi.json).
Logs detallados quedan en .local/corrections; no se versionan XML con entorno ni secretos.

## Datos, secretos y exposición

Se compararon las **17 filas de reservas preexistentes**, ID/espacio/propietario/fechas/
estado, contra snapshot previo: todas idénticas. Cuatro filas nuevas de verificación
conservadas CANCELLED, sin borrar historial. Cinco archivos locales (claves Oracle,
Basic, restore-key y token de persistencia) mantienen hashes iniciales y Git los ignora.
JDBC BOOKING/FREEPDB1 y lectura del marcador original correctos. El mensaje histórico
"Persistence after restart" del helper es el nombre de la comprobación; esta fase
leyó el marcador, no ejecutó un nuevo reinicio ni escribió otro token.
Oracle sigue saludable en booking-local_oracle-data; dev/Oracle usan imágenes
importadas originales. Solo127.0.0.1:4200/8080 publicados; Oracle no publica puerto.
Revisión de secretos conocidos en cambios/historia incremental y entradas propias del
WAR: cero coincidencias. Límites y recuentos en preservation.json; no análisis exhaustivo
de secretos desconocidos o contenido de JAR externos.

## Pendientes y continuidad

[Tratamiento individual T018](TRATAMIENTO_DEPENDENCIAS_T018.md) y
[alternativa exacta no aplicada](ALTERNATIVA_STACK_T018.md). DEC-019 es decisión de
mantener stack, no aceptación de riesgos. Siguiente verificación local: abrir
http://localhost:8080/swagger-ui/index.html en Chrome, introducir credenciales locales
en diálogo Basic y avisar al agente. El navegador integrado no admitió esa navegación
(ERR_BLOCKED_BY_CLIENT); la habilidad Windows prohíbe automatizar diálogos de
autenticación. OpenAPI y recursos con Basic ya pasan HTTP/pruebas, pero eso no sustituye
la escritura interactiva. Pasos precisos de token/cookie en CONTRATO_API.md.

Correo original no localizado; nueva condición Dev Container procede de instrucción
humana DEC-016, no de una lectura atribuida del correo. Matriz de fuentes histórica
permanece intacta. Instalación del evaluador/reapertura y recuperación de WAR anterior:
[DEV_CONTAINER.md](DEV_CONTAINER.md). Historial corregido se entrega además como
bundle local `.local/delivery/booking-corrections.bundle`; su verificación y hash se
reportan al finalizar. No push ni publicación en esta fase.

Desde PowerShell7 en la raíz del checkout:

```powershell
pwsh -NoProfile -File scripts/app.ps1 stop
pwsh -NoProfile -File scripts/app.ps1 start
pwsh -NoProfile -File scripts/app.ps1 verify
```

start/verify dejan la aplicación en http://localhost:4200/ cuando terminan bien.
Detener conserva volúmenes. Si verify falla, corregir y repetir; recuperación temporal
manual del WAR anterior debe identificar esa revisión y no declararse corrección.

## Commits de implementación

- 1a4efd0: autorización DEC-016.
- 3447ad3: T019/T020, DEC-017.
- 5a7d688: mitigación/patch Tomcat y OpenAPI protegido, DEC-018/019/020.
- e859c80: Dev Container/WAR verificable, DEC-021.

El commit de este anexo se consulta en Git, sin inventar un hash antes de crearlo.
