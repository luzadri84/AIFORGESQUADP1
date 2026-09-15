# Evidencia de entrega y límites

Este informe consolida pruebas reales; no es una nueva ejecución de los resultados
históricos. Fuente de estados: [tasks](../specs/001-booking-espacios/tasks.md).
Decisiones/fechas/fallos: [BITACORA](../BITACORA.md). Revisión auditada 47a6af4; informe
e170418; correcciones productivas e859c80; evidencia eee557b; operación portable d6e45ad.

## Matriz de cumplimiento actual

| Condición/fuente | Evidencia | Límite |
|---|---|---|
| Original: reservas sin colisión/propiedad |BookingApiTest, OraclePersistenceTest, OracleConcurrencyTest, UI |Sin exclusión de intervalos en DDL; escritores externos deben seguir protocolo. |
| Concreción: recurrencia 1–12 parcial |WeeklyRecurrenceTest, API y UI 3+1 |Sin edición/cancelación de serie ni RRULE general. |
| Original: stack y WAR |POM/lock/Dockerfile, WAR e859c80 y árbol resuelto |T018 parcial; no se autorizó excepción de familias. |
| Original: Basic/identidad preparada |SecurityContractTest/HttpSecurityTest; JWT claims y MSAL compilables |No Azure real; Swagger interactivo T021 pendiente. |
| Original: ejecución Dev Container, confirmada por instrucción humana DEC-016 |CLI up/exec, herramientas/build/API dentro de dev |Correo ausente en auditoría local; no se atribuye su lectura ni apertura visual VS Code. |
| Original: WebLogic 12.2.1.4 |Incompatibilidad potencial documentada con Boot 3/Jakarta |T015 sin aclaración externa ni despliegue probado. |
| Humana DEC-004 |Repositorio definitivo; T014 No aplica |Sin recepción/migración/defecto equivalente. |
| Original: historia/decisiones IA |Git, BITACORA, AGENTS y specs/handoffs |Consolidación retrospectiva señalada, no cronometraje ni aprobaciones inventadas. |

## Ejecución funcional final,2026-09-14

Una suite completa **88 Java,0 fallos,0 errores,0 omitidas** desde Dev Container; frontend
**13/13 y build strict**. Incluye seis regresiones de auditoría conservadas y espera
Oracle. Antes, la auditoría tuvo 75casos con 6 fallos y otra prueba posterior 1/1; no sumar
como ejecución completa. Persistencia Oracle real, no H2. Carrera 201/409/una fila y
rollback tras primer flush, inyección técnica controlada; no caída física de Oracle.

WAR de e859c802d2c66967b4f3d5aa571575929cefcad1, fuente limpia, generado
2026-09-14T23:52:53Z. SHA256:
`a2ab5fe1a6d301d45a94fc7f26415326b7e1fdfbc2567e37f527e269a7d13aa5`.
Maven clean verify; Tomcat 10.1.59 core/websocket en lib-provided y el en lib: provided
no implica ausente al ejecutar java-jar.76 JAR, sin dependencias de test XMLUnit/AssertJ.

HTTP 12 comprobaciones al WAR nuevo: dos decimales 400, lista sin nuevas filas,401/403,
404real/400ID/405Allow, headers y Basic AND CSRF en OpenAPI. Cabeceras 503 probadas en
OracleConcurrencyTest. Navegador: ana#256 creada/listada/duplicado rechazado; bruno
#257–259 para 3+1; cuatro canceladas. Hubo dos avisos de acceso inválido entre pestañas,
resueltos reingresando, sin diagnóstico causal atribuido.17 filas anteriores idénticas;
5 archivos de secretos/clave/marcador con hashes conservados; localhost y Oracle saludable.

La prueba eager observa filtro efectivo antes del downstream: falla antes y pasa con
mitigación; no PoC del camino no divulgado de CVE-2026-22732. T018 conserva 43 coincidencias
(42 CVE/43 GHSA distintos):1 mitigada,13 no aplicables con evidencia,29 pendientes. No 44 ni 43
explotaciones. [Tratamiento individual](TRATAMIENTO_DEPENDENCIAS_T018.md).

## Consolidación y operación portable,2026-09-14 (DEC-022/023)

- Windows 10,Node 18.19.1:4/4 tests del operador; Linux Node 22.22 del contenedor:4/4.
  Docker simulado en esos tests; archivos/credenciales temporales sí se generan y
  conservan realmente. No son instalación nueva de Oracle.
- Windows real: environment.mjs check/start/status/db-status; guard prepare-new rechazó
  entorno existente sin alterar secretos. SQL por stdin conectó BOOKING/FREEPDB1.
- Dev Container CLI 0.89.0 up con initialize Node: success, mismo contenedor 58b20a069dfa,
  node/workspace, sin reconstruir imágenes ni reinicializar Oracle. No VS Code visual.
- Instrucciones Windows/Linux usan la misma CLI Node y argv Docker; sintaxis y guardas
  verificadas en ambos runtimes. **No se probó Docker desde host Linux independiente,
  descarga/construcción desde cero ni instalación limpia del evaluador.** UID1000,
  Linux/amd64, memoria y acceso a imágenes públicas son precondiciones explícitas.
- No se repitió la suite funcional 88/13 al no cambiar backend/frontend. Cambios solo
  documentación, operador y initialize de Dev Container. Cierre documental T025 verificado a continuación.

## Evidencia compacta y reproducción

[Suite/WAR](correction-evidence/verification.json), [HTTP](correction-evidence/http-results.json),
[OpenAPI](correction-evidence/openapi.json), [inventario Maven](correction-evidence/maven-tree.txt),
[WAR](correction-evidence/war-libraries.txt), [catálogos](correction-evidence/catalogs.json),
[avisos](correction-evidence/dependency-findings.json), [preservación histórica](correction-evidence/preservation.json).
Son fotografías de las ejecuciones originales; no se actualizan fingiendo repetirlas.
Logs privados/XML con entorno no se versionan. Escaneo de secretos conocidos no demuestra
ausencia de todo secreto desconocido ni inspección exhaustiva de JAR externos.

Pruebas completas: `node scripts/environment.mjs verify` en el host, solo con Oracle
preparado. Pruebas operativas: `node --test scripts/test/environment.test.mjs`.
Auditoría de dependencias offline: [procedimiento](../scripts/audit/README.md); descarga
catálogos públicos completos, nunca envía el inventario privado a OSV.

T021: el usuario abrió Swagger; el control Windows se detuvo al no poder determinar
la URL. No hubo escritura UI acreditada. No reabrir esa prueba por reorganizar documentos.
T015 y T018 siguen independientes; T023/T024 no implementadas. La tarea documental no
es certificación absoluta de seguridad ni despliegue.

## Cierre T025 — comprobaciones de esta consolidación

- 60 archivos originales respaldados y comprobados por SHA-256 antes de retirar:
  41 Markdown propios y 19 anexos. La versión posterior de la bitácora de d6e45ad
  se preservó adicionalmente. Se archivaron 20 Markdown redundantes y 11 anexos de
  auditoría anterior; quedaron 23 Markdown propios en repo, más herramientas de terceros.
- 25 Markdown revisados (23 del repo y dos externos), 137 enlaces locales válidos,
  sin destinos/anchors ausentes. Referencias a documentos retirados solo quedan como
  comandos explícitos de consulta histórica en Git, no como dependencias de ejecución.
- Requisitos originales de AGENTS preservados literalmente; constitución 1.1.2 solo
  editorial. Sin diff en backend/frontend, Compose, infra, Wrapper, skills ni plantillas
  respecto a eee557b. No nueva implementación de negocio ni migración de datos.
- Stop/start real con environment.mjs en Windows pasó; Oracle healthy y misma imagen/
  volumen. Las **22 filas presentes al iniciar este ciclo** quedaron idénticas, además
  de las 17 de la línea base anterior. Cinco archivos sensibles conservaron sus hashes.
  JDBC BOOKING/FREEPDB1 leyó de nuevo el marcador original tras reiniciar Oracle.
- HTTP host 4200 y /api/csrf en 8080 respondieron 200; hash de fuentes/WAR actual válido.
  Las publicaciones siguen solo en 127.0.0.1. La aplicación queda iniciada.
- Cero coincidencias de secretos conocidos en checkout/documentos externos/respaldo,
  historia incremental y entradas propias del WAR. Los cinco archivos sensibles están
  ignorados. Este control tiene los límites de secretos conocidos/JAR externos ya indicados.
- Sintaxis de los dos mensajes PowerShell editados y de verify-local.sh correcta;
  git diff --check sin errores. Precondiciones Spec Kit con feature 001-booking-espacios
  y -RequireTasks/-IncludeTasks correctas. Esos cambios solo redirigen al README/operador común.
- Las pruebas funcionales 88/13 son evidencia anterior; las guardas operativas 4/4 por
  runtime y el ciclo real son las verificaciones pertinentes de este ajuste. No se hizo
  instalación nueva completa, Docker de host Linux, Swagger write, Azure ni WebLogic.

Documentación extensa fuera del repositorio: técnica con 16 secciones y diagramas;
guía con 25 respuestas y ocho ejercicios progresivos. Ningún ejercicio fue implementado.
Estados T018/T021/T015 conservados; no T023/T024. Commits locales, sin push.

## DEC-024 — Verificación del clon nuevo, 2026-09-15

Entorno: clon Windows con Docker Linux amd64, base inicializada por el usuario con
prepare-new/images/up/schema/seed. Revisión base effd3ab y cambios locales descritos
en BITACORA.md. No se recrearon volúmenes ni se modificaron las credenciales.

- Diagnóstico: SQL*Plus conectó como BOOKING/FREEPDB1; secretos montados idénticos,
  52 bytes sin espacios. La sonda JdbcCheck en modo check devolvió ORA-01017 con
  ojdbc11 21.9.0.0 y conectó con 23.26.3.0.0, usando la misma base y contraseña.
- Recorrido final: `node scripts/environment.mjs verify`, salida 0. Maven clean
  verify: 88 pruebas, 0 fallos/errores/omitidas, WAR generado. Angular build y
  comprobación ngc correctos; 13 pruebas, 13 pasan. Sonda de infraestructura ngc
  correcta y JDBC 23.26.3.0.0 confirma BOOKING/FREEPDB1.
- Git: se retiró únicamente la excepción /workspace del contenedor antes del
  recorrido final. backend-artifact.sh la creó automáticamente antes del build;
  el registro de revisión/estado del WAR y verify-local.sh terminaron correctamente.
- Aplicación iniciada por verify; HTTP host 4200/ y 8080/api/csrf devuelven 200.
  `bash scripts/backend-artifact.sh current` confirma correspondencia fuentes/WAR.
- Registro local completo, ignorado por Git: .local/runtime/verify-jdbc23-final.log.
  El agente no repitió prepare-new/images/schema/seed; la evidencia de esos pasos
  es la ejecución reportada por el usuario. No acredita un nuevo host Linux,
  Swagger interactivo, Azure, WebLogic ni una nueva auditoría de dependencias.
