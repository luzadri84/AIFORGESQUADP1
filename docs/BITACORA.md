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


## DEC-001 — Reutilizar Specify e integrar solo sus recursos necesarios

- Registro/hecho: 2026-09-14, America/Bogota; contemporáneo, H001/T001.
- Problema: adoptar SDD sin reinicializar el entorno restaurado ni sobrescribir documentos.
- Origen/propuesta: solicitud humana explícita de Prompt A; selección de archivos por Codex.
- Intervención humana real: integrar Spec Kit y .handoffs, comparar/fusionar, ejecutar solo H001.
- Decisión/responsable: adopción aceptada por solicitud humana; reutilización de 0.8.1,
  generación offline temporal y selección de manifests decididas por el agente.
- Alternativa: inicializar sobre la raíz; descartada por el agente por riesgo de colisión.
- Motivo/compensación: evita reinstalación y deriva de versiones; requiere revisión
  selectiva de futuros cambios. Sin impacto de rendimiento de la aplicación medido.
- Implementación: .specify, nueve skills .agents/skills, docs/SPECKIT.md y exclusiones Git.
  Se excluyó el workflow generado; .handoffs no será un motor de ejecución.
- Verificación ejecutada por Codex en Windows 10: uv tool list y specify version =
  0.8.1; direct_url.json fija tag v0.8.1/commit a63f64b; ayuda real y specify check
  correctos. specify init temporal offline terminó correctamente. La llamada inicial
  falló por Unicode/cp1252; PYTHONIOENCODING=utf-8 y PYTHONUTF8=1 lo resolvieron.
- Verificación adicional: manifests originales coinciden byte a byte; specify integration
  list identifica codex instalado. Codex app-server skills/list, cwd del repositorio y
  forceReload=true, devuelve las nueve skills scope=repo/enabled=true y cero errores
  del proyecto. Consulta por stdio sin crear tareas ni ejecutar modelos.
- Límite: estas comprobaciones no ejecutan negocio ni demuestran la UI del selector
  de skills; no se atribuye al usuario la ejecución de estas comprobaciones.
- Vinculación: T001 y DEC-001 en el mensaje del commit, sin hash futuro inventado.


## DEC-002 — Reconciliar el estado real y conservar una sola fuente de tareas

- Registro: 2026-09-14, America/Bogota; H001/T001–T002. Origen mixto: integración
  contemporánea y reconstrucción retrospectiva de infraestructura explícitamente indicada.
- Problema: las semillas describen trabajo propuesto y tres commits; no deben borrar
  la bitácora anterior ni acreditar funciones inexistentes por usar Spec Kit.
- Origen/propuesta: Prompt A/reglas 05/semillas; agente propone fusión selectiva,
  feature 001-booking-espacios, mismos T001–T015 y bitácora docs/BITACORA.md.
- Intervención humana: conservar entorno/historial y ejecutar únicamente H001.
  No hubo aprobación específica de cada ajuste documental.
- Decisión/responsable: fusión y rutas elegidas por el agente dentro del alcance
  humano; aceptada e implementada en AGENTS, constitution/spec/plan/tasks/handoffs,
  README y criterios autocontenidos de entrega/starter. No se duplica backlog.
- Alternativa: overlay completo/segunda bitácora; no aplicada por duplicar y sobrescribir.
- Evidencia retrospectiva: Git inicial main/5dcdb7c contiene siete commits; los tres
  iniciales son 552db96 (12:12:26 -05), b219204 (12:20:21 -05), 4b66e6d (12:33:06 -05),
  todos del 2026-09-14. Respaldan documentos, infraestructura y corrección Angular.
  Los 37 minutos son reporte previo: no hay cronometraje auditable; el intervalo
  entre commits tampoco mide trabajo efectivo. No se cambian fechas ni se inventa historia.
- Alcance retrospectivo: docs/VERIFICACION_LOCAL.md y entradas anteriores describen
  otro equipo. La restauración Windows 10 se verificó en el turno previo de esta sesión
  (informe local .local/RESTAURACION_VERIFICADA.md); no se atribuye al usuario su ejecución.
  Clave actual en .local/restore-key.pem, credenciales en .local/secrets; las rutas
  de las entradas anteriores se conservan como hechos del equipo origen.
- Motivo/compensación: trazabilidad sin rehacer entorno. Se amplían T013/H007 y
  T014/H008 con criterios locales, sin fabricar explicación de código inexistente.
- Verificación aprobada por Codex el 2026-09-14 en Windows 10: 20 documentos y 100
  enlaces locales revisados; 15 IDs únicos, referencias T/FR/H válidas, cuatro scripts
  PowerShell parseados sin errores, precondiciones Spec Kit correctas con selector de
  feature. Compose config, JDBC read original y ngc correctos. Infraestructura sin diff
  frente a 5dcdb7c; prefijos originales AGENTS/bitácora intactos, secretos/marcador
  conservados e ignorados. Detalle en INTEGRACION_H001.md.
- Límites: las sondas no acreditan seguridad, reservas, WAR, E2E ni el defecto del starter.
  No se ejecutan write, prepare, npm ci, instalaciones, reinicios ni cambios de volumen en H001.
- Vinculación Git: T002/DEC-002 en commit de fusión posterior al commit b977ca3.

## DEC-003 — Arquitectura propuesta para la siguiente fase, sin aprobación atribuida

Actualización posterior: organización resuelta por decisión humana DEC-005; las
dependencias del starter de esta entrada quedaron superadas por DEC-004. Se conserva
la narración y su estado en el momento en que se escribió.

- Registro/hecho: 2026-09-14, America/Bogota, contemporáneo H001/T002.
- Problema: elegir estructura sencilla para negocio futuro sin reformar infraestructura válida.
- Origen: monolito propuesto en plan previo de ChatGPT; Codex lo contrasta ahora con
  el repositorio, que solo contiene infraestructura y sondas.
- Propuesta de Codex: controlador HTTP, servicio transaccional, Spring Data JPA,
  DTO/validadores y funciones pequeñas de intervalos/recurrencias; Angular por función.
- Alternativa razonable: dominio con puertos/adaptadores dentro del monolito. Aísla
  detalles de persistencia, pero añade interfaces, mapeos y pruebas; no existe aún
  una segunda persistencia o requisito que justifique ese coste.
- Intervención humana real: solicitud de recomendación y simplicidad; ninguna elección
  concreta de arquitectura registrada. No hubo un rechazo humano de capas ni revisión de clases.
- Decisión: recomendación del agente; aceptación humana PENDIENTE. Implementación
  solo documental en plan/README/spec; no hay clases ni carpetas backend/frontend nuevas.
- Garantías que ambas opciones deben mantener: propiedad, CSRF, validación, protocolo
  concurrente en Oracle, recurrencias y pruebas reales. No se promete rendimiento medido.
- Verificación: inspección de código y configuración, sin pruebas de arquitectura funcional
  porque todavía no existe. T003/H002 empieza después de la decisión y autorización.
- Pendientes: arquitectura, refinamientos producto/contrato explícitos de spec/plan;
  starter (T014) y aclaración WebLogic (T015), sin cambiar unilateralmente el stack.
- Vinculación: T002/DEC-003 en el commit real de planificación; sin hash futuro.


## DEC-004 — Adoptar el repositorio actual como base definitiva

- Registro/hecho: 2026-09-14, America/Bogota; decisión contemporánea del usuario.
- Origen e intervención humana real: «el starter no llegará y no se tendrá en cuenta»;
  continuar sobre el repositorio y entorno actuales, retirar esa dependencia y
  registrar el ajuste en un commit de documentación antes de la siguiente tarea.
- Decisión/responsable: **usuario — adoptar el repositorio actual como base definitiva**.
- Motivo: el starter no estará disponible y queda fuera del alcance.
- Consecuencia: no habrá recepción, comparación, migración ni investigación de un
  defecto sembrado de ese repositorio. No inventar uno equivalente ni presentar
  otro problema como el defecto sembrado. T014: No aplica por cambio de alcance,
  conservando descripción/aceptación histórica; no implementada ni bloqueante.
- Alternativa anterior: esperar/recibir/migrar/investigar el starter. Superada por
  esta instrucción del usuario; no se inventa un rechazo de otra propuesta técnica.
- Estado de decisión: aceptada por instrucción explícita. Implementación: actualización
  documental en AGENTS, constitution/spec/plan/tasks/trazabilidad, handoffs y docs.
  Los registros anteriores de ausencia/espera/provisionalidad quedan como historia
  superada por DEC-004; sus resultados de pruebas no se borran ni se reinterpretan.
- Límites: T015/WebLogic y su evidencia exigida permanecen sin cambios; esta decisión
  no certifica despliegue ni cambia el stack. La arquitectura se consultó por separado
  y el usuario respondió durante este ajuste: elección registrada en DEC-005.
- Entrega/estudio: EXPLICACION_IMPLEMENTACION.md debe describir lo realmente construido,
  archivos y pruebas, preguntas respondidas y retos solo de análisis, sin ampliar MVP.
- Verificación del ajuste ejecutada por Codex el 2026-09-14: enlaces canónicos
  válidos, T014 sin marcar implementada, bloque T015 idéntico al anterior y diff
  exclusivamente Markdown; código/configuración sin cambios. No se repitieron
  pruebas funcionales ya verificadas para este cambio documental.
- Git: continuación desde main/8ba745a con árbol limpio; commit asociado a DEC-004.
  No reset/rebase, reinstalación, nueva integración ni publicación.


## DEC-005 — Monolito organizado por funcionalidades

- Registro/hecho: 2026-09-14, America/Bogota; respuesta humana contemporánea.
- Origen: consulta sobre la propuesta DEC-003; el usuario decide monolito por
  funcionalidades para equilibrar simplicidad y claridad.
- Intervención real: booking con controlador/servicio/repositorio/DTO/entidad/reglas;
  space con consulta/entidad/persistencia; security con autenticación/adaptación de
  identidad; errors solo si se comparte. Angular: acceso y reservas, elementos próximos.
- Decisión/responsable: **usuario**, arquitectura aceptada con esta organización.
  Mantener controlador–servicio–repositorio dentro de booking, sin infraestructura nueva.
- Alternativas: organización global por capas y puertos/adaptadores estaban en la
  conversación; no se inventa rechazo de tecnologías ni desacuerdo previo. La estructura
  por funcionalidades concreta la propuesta anterior de paquetes booking/space/security.
- Implementación: plan/AGENTS/constitución/README actualizados; código pendiente de T003.
  No crear paquetes vacíos ni errors antes de un tratamiento realmente compartido.
- Garantías: propiedad, CSRF, validación, transacciones/concurrencia y pruebas Oracle
  se conservan; la organización no acredita por sí sola seguridad ni rendimiento.
- Verificación: decisión documental; no se atribuyen al usuario las pruebas del agente.
- Relación: DEC-003 conserva su origen histórico y queda resuelta en cuanto a organización
  por esta decisión; DEC-004 autoriza continuar a T003. Contratos/negocio fuera de T003
  no se implementan por esta confirmación. Git: DEC-004/DEC-005 en commit documental.


## DEC-006 — Base JPA/Oracle aditiva con SQL explícito y pruebas transaccionales

- Registro/hecho: 2026-09-14, America/Bogota; T003, decisión técnica del agente dentro
  de DEC-004/DEC-005. Usuario eligió funcionalidades; no revisó cada clase/mapeo.
- Problema: crear base de persistencia conservando Oracle restaurado y marcador.
- Evidencia previa: USER_TABLES solo contiene ENVIRONMENT_PROBE, no objetos BKG_;
  BOOKING/FREEPDB1 tiene CREATE TABLE/SEQUENCE. No se borran ni recrean datos actuales.
- Decisión: backend Maven WAR con Boot 3.3.13/Java 21 y repositorios por funcionalidades;
  BKG_SPACE/BKG_BOOKING, secuencias y constraints; semillas solo de espacios de desarrollo.
  No endpoints, servicios de reservas ni autenticación propia de T004/T005 aún.
- SQL versionado aplicado explícitamente con SQL*Plus existente; Hibernate validate,
  inicialización automática desactivada. Alternativa: nuevo motor Flyway/Liquibase;
  no requerido para una V001 y catálogo inicial. Futuras migraciones exigen revisión.
- Seguridad de operación: V001 falla si ya hay objetos destino; no DROP/ALTER, no
  sobrescritura. Oracle DDL hace commits implícitos: ante fallo parcial, inspeccionar
  antes de continuar. Semillas MERGE insert-only preservan filas y ediciones existentes.
- Tiempo: OffsetDateTime/NATIVE y TIMESTAMP(9) WITH TIME ZONE para probar conservación
  de instante y nanosegundos. Sin afirmar todavía corrección de intervalos/concurrencia.
- Pruebas: JPA/DataJpaTest contra Oracle real, validate y rollback de cada prueba;
  sin H2/create-drop ni reservas duraderas. Las secuencias pueden avanzar por pruebas.
- Dependencias: starter-test administrado por BOM existente; sin cambiar las versiones
  ya verificadas ni instalar otra herramienta. Se resuelve por Maven existente.
- Verificación ejecutada: schema aditivo aplicado; seed 3 filas y luego 0; guard de
  reaplicación rechaza con ORA-20001. Maven verify final BUILD SUCCESS, 12 pruebas
  Oracle, 0 fallos/errores/omitidas; WAR generado. Al finalizar: 3 espacios, 0 reservas,
  marcador original legible, credenciales/token idénticos al respaldo y Oracle healthy.
- Fallo real y corrección: primera ejecución 12 pruebas/6 fallos por buscar SQLException
  en la causa más profunda; driver usa OracleDatabaseException allí. Se localiza ahora
  SQLException en la cadena y se valida el código Oracle. Constraints correctas desde
  primera ejecución. No es defecto sembrado ni se presenta como sustituto de T014.
- Límites: sin API/UI, seguridad propia, colisiones o prueba de despliegue. No se repite
  reinicio de restauración ni pruebas H001. T004 es la siguiente tarea pendiente.
- Evidencia y fuentes técnicas: [VERIFICACION_T003.md](VERIFICACION_T003.md),
  [explicación](EXPLICACION_IMPLEMENTACION.md) y tests reales en backend/.
- Git: cambio de alcance/arquitectura confirmado en 75af194 antes de implementar;
  código/evidencia T003 se registra en commit incremental con DEC-006. Sin publicación.

## DEC-007 — Continuación hasta la solución local

2026-09-14, America/Bogota. Origen/decisor del alcance: instrucción adjunta del usuario,
completar T004–T013 con los criterios existentes, continuar entre handoffs sin consultar.
T014 no aplica, T015 externo independiente. Se conserva main/992bc41 limpio al comenzar.
Codex verifica precondiciones de speckit-implement, sin hooks ni checklists adicionales,
y reutiliza la integración. Las propuestas de detalle existentes (semanal 1–12,
aceptación parcial, activas futuras/en curso, Bogotá) se aplicarán para concretar
estos criterios; no se atribuye al usuario una aprobación separada de cada parámetro.
Arquitectura sin cambio. Tomcat incluido por Boot ejecutará localmente el WAR; no
certifica ni sustituye la obligación externa de WebLogic. No reinstalación ni push.

## DEC-008 — Basic por petición y CSRF de sesión

2026-09-14, America/Bogota. Decisión técnica de Codex para T004/T005: Basic con dos
usuarios externos, BCrypt y contexto de seguridad no persistido; sesión solo para
CSRF, token enmascarado obtenido de /api/csrf. Alternativa cookie legible automática:
se prefiere token explícito en memoria para controlar su ciclo y no desactivar CSRF.
UI enviará Basic solo a /api/ propio. Passwords generadas solo si no existen, archivo
.local/runtime/booking.properties ignorado, sin cambiar credenciales Oracle.
DTO estricto rechaza campos desconocidos y fechas sin offset; errores genéricos sin
SQL/credenciales. Documentación OpenAPI autenticada. Pruebas y resultado se registran
al completar H002; no se atribuye al usuario revisión de esta configuración.

H002, primer resultado: 15 tests, un fallo. Autenticar Basic en cada petición
invalidaba el token CSRF entre escrituras. Se conserva validación XOR/repositorio de
sesión y se evita exclusivamente la rotación por cada autenticación Basic (la sesión
no guarda identidad). La prueba de múltiples escrituras valida esta corrección;
no se desactiva CSRF ni se aceptan tokens ajenos.

H002 final: Maven verify BUILD SUCCESS, 15 tests correctos (12 persistencia + 3 seguridad/contrato), WAR ejecutable. Sin endpoints de reservas aún; siguiente H003.

## DEC-009 — Transacción y bloqueo por espacio

2026-09-14, America/Bogota. Codex concreta T006/T007 según plan autorizado: intervalos
semiabiertos, lectura de choques ACTIVE con desigualdades estrictas; PESSIMISTIC_WRITE
sobre Espacio antes de consultar/crear/cancelar, en READ_COMMITTED. Alternativa de
bloquear reservas deja sin protección una sala vacía; synchronized solo cubre una JVM.
Propietario del principal, nunca body; listado propias ACTIVE con fin futuro; cancelación
propia idempotente, ajena/ausente 404. Se conserva horario pasado permitido (sin requisito
que lo prohíba). Reloj inyectable UTC. Pruebas reales y resultados al cierre H003.

H003 verificado: Maven verify BUILD SUCCESS, 27 tests correctos; ciclo propio/ajeno, colisión/adyacencia y cancelación contra Oracle en transacciones revertidas. No prueba aún carrera entre peticiones, prevista T011.

## DEC-010 — Cliente por funcionalidades y ejecución dentro de dev

2026-09-14, America/Bogota. Codex implementa T008: Angular standalone strict por
acceso/reservas, PrimeNG, Tailwind y RxJS; autenticación/token solo en memoria,
interceptor limitado a rutas relativas /api/. Proxy Angular al backend del mismo
contenedor, sin CORS abierto. Formularios expresan horario Bogotá y envían offset.
Alternativa de almacenar Basic en localStorage descartada por persistencia innecesaria
de credenciales. Estado de carga/error/vacío explícito y POST sin reintento automático.
Dependencias @angular/build 20.3.37 y @primeng/themes 20.0.0 verificadas con npm view;
se reutilizan CLI/Node/cachés/versiones, sin reinstalar herramientas. Procesos locales
dentro de dev; no nuevos contenedores ni modificación del volumen Oracle.

H004 encontró un defecto real de sesión no cubierto por MockMvc: login/listado
funcionaban, POST devolvía 403 después de consultas paralelas. NEVER seguía permitiendo
rotación de sesión por autenticación; se cambia contexto Security a STATELESS,
conservando sesión explícita exclusivamente del repositorio CSRF. Se repiten pruebas
y flujo HTTP/navegador; no se desactiva la protección CSRF.

H004 final: regresión 27/27, HttpSecurityTest adicional 1/1, Angular strict y npm test 1/1. Navegador real creó/listó/rechazó duplicado/canceló #55. Estado Security STATELESS corrigió rotación de sesión; CSRF sigue exigido. npm avisó de deprecación de animations/themes de versiones fijadas; no se alteró unilateralmente el stack.

## DEC-011 — Recurrencia finita y resultado explícito por ocurrencia

2026-09-14, America/Bogota. Codex concreta criterios T009/T010 autorizados: occurrences
opcional (ausente=1), 1–12, semanas en America/Bogota, primera incluida. Una transacción
con un bloqueo de espacio para todo el pedido. Conflictos esperados generan resultados
rechazados sin datos ajenos; cualquier excepción técnica propaga y revierte todo.
Contrato uniforme {created,rejected}: 201 todas, 200 algunas, 409 ninguna. Alternativa
all-or-nothing para conflictos descartada porque contradice aceptación parcial del
criterio. No entidad Serie ni cron ni nuevas tablas. Se prueban límites, horarios,
conflicto no inicial/interno y UI. Rollback técnico/concurrencia se profundizan T011.

H005 final: 35/35 tests backend, frontend build/test correctos. Navegador: conflicto segunda semana de otro usuario, tres creadas y una rechazada (#72–75); luego canceladas. Se corrigió lectura de archivos Windows a UTF-8 explícito y vigilancia --poll 1000. No se ocultaron estos fallos de integración ni se asociaron al starter.

## DEC-012 — Evidencia concurrente y extensión empresarial desactivada

2026-09-14, America/Bogota. T011: peticiones HTTP separadas, sesión/token por cliente,
espacio de prueba propio; bloqueo JDBC externo retiene la fila hasta observar dos
entradas en lockById. Spy solo observa entrada/delega implementación real; no simula
Oracle. Verificar un 201/un 409/una fila. Rollback: spy inyecta fallo técnico tras
flush real de la primera ocurrencia y consulta dentro de esa transacción; después
se comprueba cero filas fuera. Limpieza limitada a IDs de espacios creados por el test.
T012: configuración JWT condicionada, firma Nimbus + issuer/tiempo/audience/subject;
identidad empresarial con prefijo/hash evita colisión con Basic y cabe en OWNER_ID.
MSAL Angular con providers/adaptador reales y memoria; configuración ausente en modo
Basic no crea instancia ni llama Azure. No se inventan tenant/clientId ni prueba Azure.
Alternativa activar Azure real queda fuera de alcance; no añade bloqueo local.

H006 primer verify: 39 tests, 2 fallos. El spy de repositorio intentaba callRealMethod
sobre método abstracto de proxy Spring Data; se corrige delegando al Answer real del
proxy, conservando SQL/locks. La excepción inesperada se redirigía a /error protegido
y parecía 401; ApiErrors ahora da 500 genérico para excepciones no previstas.
No se declaró concurrencia ni rollback verificados antes de corregir la prueba.

H006 final: 39 tests correctos; carrera HTTP Oracle 201/409 y una fila, rollback real tras fallo inyectado y cero filas; fixtures propias limpiadas. MSAL/JWT compilan; Basic independiente. npm audit 0 en lockfile frontend, npm ls sin peers inválidos; no SCA Java integral ni Azure real. Árbol Maven obtenido tras corregir comillas de argumento PowerShell.

## DEC-013 — Entrega local operable y documentación real

2026-09-14, America/Bogota. Codex implementa T013: app.ps1 start/verify/stop/status
reutiliza ambos Compose e imágenes --no-build. start usa WAR existente (lo empaqueta
solo si falta); verify recompila/prueba y reinicia la app al terminar. Procesos dentro
de dev, PID validado por comando/directorio antes de detener, sin matar procesos ajenos.
Credenciales Basic se generan solo si faltan; nunca se reemplazan Oracle ni las existentes.
Se documentan fechas de prueba y limitaciones, doce contenidos de explicación,
preguntas/retos solo analíticos. No se afirma compatibilidad WebLogic ni Azure real.
La comprobación de operación y la revisión final quedan registradas al cerrar H007.

H007 final: app.ps1 verify pasa 50 pruebas Java, ngc/build strict y npm test 1/1.
Se añadieron 11 casos al query Oracle para demostrar las fronteras también en
persistencia, no solo en el helper. Stop/start ejecutados realmente; ambos procesos
arrancan, Oracle healthy y lectura original coincide. Contraseñas Oracle y token
comparados byte a byte con copia privada sin revelar valores. Login navegador tras
reinicio correcto; cinco reservas visuales CANCELLED, ninguna activa de las pruebas.

Revisión final: infraestructura original sin diff frente a 992bc41, secretos/clave
ignorados y ausentes del WAR/candidatos, puertos exclusivos localhost. 140 enlaces
locales en 28 documentos correctos, 15 IDs únicos, precondiciones Spec Kit correctas.
README/explicación/contrato/spec/plan actualizados; explicación con doce secciones,
quince preguntas y seis ejercicios analíticos no implementados. Constitución 1.1.1:
solo corrección editorial de referencias a implementación, principios sin cambio.
T015 permanece literal frente a 8ba745a, sin aclaración externa; T014 no aplica.
No push ni despliegue. Informe: docs/VERIFICACION_FINAL.md. Próxima gestión T015.

## DEC-014 — Validación visible y espera limitada tras reporte del usuario

2026-09-14, America/Bogota. Origen: captura del usuario con inicio/fin iguales
17/09/2026 17:08 y botón cargando. Codex reproduce envío y observa espera prolongada;
HTTP directo/proxy devuelven 400. Al repetir con trazas temporales, el observable
recibe error y finaliza; no se identifica una causa definitiva de la espera inicial.
No atribuirla sin prueba a extensiones, Oracle o CSRF. Trazas sin secretos retiradas.

Decisión del agente para T016: validator de grupo Angular con aviso junto a Fin,
impedir rango igual/inverso antes de POST y timeout RxJS de 15 s para peticiones
Booking. No reintentar escrituras: si vence espera, consultar lista porque Oracle
podría haber confirmado. Servidor conserva sus validaciones y transacciones.
Alternativa de depender solo del 400 deja mala respuesta visual y petición evitable;
se mantiene como segunda barrera. No cambiar arquitectura ni reglas de producto.
Favicon vacío explícito evita el 404 accesorio; no era prueba de fallo de la API.
Resultados de pruebas y navegador se registrarán al cerrar T016.

T016 verificado: npm test ejecuta ngc y 13 pruebas correctas: siete de FormGroup
con fechas y corrección, cuatro de timeout por operación y una de HTTP 400/éxito
sobre BookingApi compilado, más la prueba previa de rutas de credenciales. Timeout
usa HTTP simulado/tiempo virtual; no se presenta como corte de red real en navegador.
Build Angular correcto. Navegador: igualdad muestra aviso inmediato y botón desactivado;
corregir fechas permite crear #157 (15/10/2038 10–11), duplicado informa conflicto y
libera controles, cancelación propia deja lista vacía. Se conserva #157 CANCELLED.

No se tocó backend, Oracle ni credenciales; sus 50 pruebas anteriores no se repitieron
para atribuir nueva evidencia. npm test ahora compila primero el servicio real para
la regresión; app.ps1 evita ejecutar ngc dos veces. T016 completada, no nueva funcionalidad.
Logs ignorados .local/t016-frontend-tests.log y .local/t016-build.log. Commit T016/DEC-014,
sin push. La causa exacta de la espera inicial sigue sin evidencia suficiente.

## DEC-015 — Auditoría independiente del estado implementado

2026-09-14, America/Bogota. Origen y decisión del usuario: auditar la solución
terminada, priorizar seguridad, comprobar código y originales, conservar datos/historia,
no corregir todavía producción/dependencias/configuración. El agente decide ejecutar
T017 dentro de H006/H007 y reutilizar tasks como único estado. No se infiere aprobación
de parches ni cambio de arquitectura. Starter/defecto excluidos, WebLogic independiente.

Alternativa de aceptar pruebas anteriores como certificación descartada por la instrucción
humana: se revisó main 47a6af4, árbol limpio y 18 commits, originales AGENTS/historia DOCX,
especificaciones y código. Correo no localizado; no se atribuye su lectura a esta auditoría.
Se añadieron AuditSecurityTest y una prueba real de espera a OracleConcurrencyTest.
75 casos Java:69 pasan/6 fallan; prueba posterior de espera 1/1; frontend 13/13 y build.
Los fallos se conservan: dos coerciones decimales, tres errores MVC500 y OpenAPI incompleto.
No se cambiaron tests para ocultarlos ni se ejecutó corrección productiva.

La espera Oracle devolvió503 en 8–14 s, cero filas; carrera y rollback previo pasaron con
conexiones reales. Navegador: Ana/Bruno, recurrencia 3+1 y 0+4, listas separadas, texto
inocuo escapado, teclado y fechas iguales. Se verificó cada cancelación tras observar
que clics durante busy podían ignorarse; seis fixtures #204–209 quedaron CANCELLED,
espacio temporal #170 retirado sin reservas. No se canceló trabajo preexistente.

Revisión de permisos automática rechazó consulta OSV por envío de nombres/versiones
derivados de repositorio privado. No se realizó ni se cambió de destino para eludirla.
Alternativa segura aceptada: catálogos públicos completos y comparación local;65 avisos
por versión Maven,0 npm, no 65 explotaciones. Se corrigió el comparador auxiliar para unir
intervalos y se probaron3 casos de fronteras; el conteo no cambió. No se actualizaron paquetes.
Avisos/scopes/condiciones y discrepancias de familias Spring están en el anexo.

Auditoría de secretos conocidos/patrones en 274 blobs: sin coincidencias, con límites
expresos; imágenes/WAR revisados sin revelar valores. BOOKING conserva rol de desarrollo
amplio, registrado como recomendación D, no requisito inventado. Oracle healthy, JDBC read
coincide con marcador original, puertos localhost. Semillas repetidas:0 filas cada vez.

Conclusión del agente: local funcional con hallazgos; seguridad no certificada integralmente;
entrega WebLogic bloqueada T015 y Dev Container no acreditado plenamente. T018–T022 son
correcciones propuestas pendientes de autorización; T023–T024 opcionales. Informes y
regresiones se registran en commit T017/DEC-015, sin push. El WAR existente queda iniciado.
Las seis regresiones harán fallar verify hasta corregir; usar start después de una
verificación fallida. No confundir HEAD auditado con commit posterior de informes.

## DEC-016 — Autorización humana para corregir T018–T022

2026-09-14, America/Bogota. El usuario autoriza implementar T018–T022 y ejecutar
09_Prompt_Corregir_Auditoria_y_Verificar_Entrega.md del material de apoyo. Base real:
main e170418, árbol limpio y sincronizado con GitHub tras el push autorizado en el
turno anterior. Esta fase no autoriza otro push/publicación. T023/T024 excluidas;
T015/WebLogic independiente, starter/defecto excluidos, arquitectura por funciones.

El agente elige tratar mitigaciones/parches compatibles y continuar correcciones
independientes cuando una excepción de stack necesite decisión humana. No se acepta
riesgo residual en nombre del usuario. Estados solo en tasks; continuidad H006/H007.
Se preserva la auditoría de47a6af4 con sus seis fallos como evidencia histórica.

Correo no localizado en carpeta de apoyo al inicio. Esta instrucción humana aporta
la condición de instrucciones/ejecución Dev Container; no se afirma lectura del correo.
Se registrará esa nueva fuente en el cierre sin cambiar retrospectivamente la auditoría.
Antes de modificar se guardaron hashes privados de secretos, copia del WAR vigente y
snapshot de reservas existentes en .local/corrections, nunca en el commit.

## DEC-017 — Coerción numérica y semántica MVC, T019/T020

2026-09-14, America/Bogota. Decisión del agente dentro de DEC-016: solo tokens JSON
enteros para spaceId/occurrences. Rechazar 1.75, 1.0, 1e0 y cadenas como "1";
conservar occurrences ausente/null=1, límites y campos desconocidos. Desactivar
ACCEPT_FLOAT_AS_INT y ALLOW_COERCION_OF_SCALARS en Jackson; comprobar fechas y otros
DTO. No nueva regla de reservas ni validación solo del cliente.

Antes de cambiar el test, revisión de mappings: BookingController tiene DELETE/{id};
GET sobre esa misma ruta corresponde a405 con Allow DELETE, no404. La expectativa
histórica missingResourceShouldBe404Not500 usaba /api/bookings/99999999 y era errónea.
Se conserva el nombre para trazabilidad pero se dirige a una ruta realmente inexistente;
se añade el caso GET/{id}=405. Esto no oculta el defecto500: todos estos casos siguen
fallando contra el handler anterior. Se mantienen negativa de ID inválido400 y PUT405.

T019/T020, evidencia inicial: 12 casos dirigidos de AuditSecurityTest pasan con Oracle
real (0 fallos/errores/omitidas). Incluyen los dos fraccionarios, formatos/límites,
null válido, tres regresiones históricas y GET405. Log .local/corrections/t019-t020.log.
Pendiente comprobar HTTP contra WAR nuevo y suite final; no se declara todavía cierre.
