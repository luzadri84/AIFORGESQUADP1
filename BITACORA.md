# Bitácora consolidada de Booking

**Consolidación posterior de registros reales**,2026-09-14 America/Bogota.
Base documental: main/eee557b, sin cambios al iniciar; ajuste operativo posterior
 d6e45ad. Los hechos históricos también están fechados 2026-09-14 en los registros;
esta consolidación no sustituye esas fechas ni inventa duración efectiva de trabajo.
Fuentes: bitácora previa, Git, especificaciones, informes y pruebas. Documentos completos
permanecen en Git y en respaldo externo verificado; la instalación no depende de ese respaldo.

## Requisitos, fuentes y evolución de alcance

A: requisitos originales conservados en [AGENTS](AGENTS.md) e historia de usuario
recuperada en etapas anteriores. B: instrucciones humanas posteriores. C: concreciones
técnicas del agente. D: recomendaciones de auditoría, no requisitos originales nuevos.
La infraestructura de origen registró lectura del correo MIME; la auditoría posterior
en Windows 10 no lo encontró y no afirmó leerlo. DEC-016 aporta por instrucción humana
el requisito de ejecución por Dev Container; no cambia retrospectivamente las fuentes.

Se pedían Angular 20 strict/PrimeNG/Tailwind/RxJS, Java 21/Boot/JPA/Security, Oracle 19c+
y WAR para WebLogic 12.2.1.4. El núcleo era crear/listar reservas propias sin colisiones;
se concretaron cancelación y recurrencia parcial. Notificaciones, administración e
identidad Azure real no estaban en alcance. La obligación WebLogic sigue contradictoria
con Boot 3/Jakarta; no se considera resuelta por producir un WAR.

## Entorno original y transferencia (antes de DEC-001)

En el equipo de origen Windows 11 había seis documentos, sin Git ni aplicación. El
usuario pidió infraestructura provisional; el agente preservó AGENTS e inició historial:
552db96. Docker/WSL2/VS Code/Git existentes se reutilizaron, Java/Maven se ubicaron dentro
del contenedor, Oracle 3 GB y dev 2 GB. No se cerraron procesos ni eliminaron recursos ajenos.

b219204 creó sondas JDBC/ngc, secretos locales y Dev Container. Se verificaron artefactos,
peers y digest públicos. Primer npm audit:23 paquetes afectados;4b66e6d actualizó Angular
20.3.31/CLI 20.3.37 dentro de la rama, sin force ni peers relajados. ERESOLVE se resolvió
en directorio temporal limpio. Hubo corrección de propietario del volumen node_modules,
Git safe.directory limitado a/workspace y argumento de CLI. JDBC BOOKING/FREEPDB1,
ngc y persistencia tras restart/stop-up pasaron; eso aún no demostraba negocio ni WAR.

3a7184f preparó transferencia;8e384df corrigió función PowerShell que ocultaba Docker;
31782c5 agregó respaldo consistente Oracle con base detenida, credenciales originales y
CMS AES-256-GCM/RSA-OAEP. Se probaron hashes, descifrado y restauración aislada.5dcdb7c
registró repositorio privado y ocho assets de Release. La clave se entregó aparte.
Los cachés no se transfirieron. La Release contiene infraestructura de 31782c5, no la
aplicación posterior. Restauración Windows 10 se comprobó en su sesión, sin atribuirla
al equipo de origen. La lectura del marcador original se conserva como evidencia separada.

## Decisiones y construcción por etapas

### DEC-001 — T001/H001, b977ca3

Usuario pidió integrar Spec Kit y .handoffs mediante comparación selectiva. Agente
reutilizó Specify 0.8.1, generación temporal offline y manifests, sin overlay completo,
workflow ni agentes paralelos. Nueve skills se descubrieron habilitadas; problema de
Unicode cp1252 se resolvió con UTF8. No demostró negocio ni UI del selector de skills.

### DEC-002 — T002/H001,8ba745a

Agente consolidó una feature 001-booking-espacios y una fuente de estado tasks.md,
preservando bitácora/historial. Alternativa de reinicializar o duplicar documentos no
aplicada. Se revisaron 20 documentos/100 enlaces, scripts, precondiciones y marcador.
Los 37 minutos mencionados previamente no tienen cronometraje auditable; fechas de commits
no miden trabajo efectivo. Sondas no acreditaban reservas/seguridad/despliegue.

### DEC-003 — Propuesta,8ba745a

Agente propuso controlador/servicio/JPA y funciones de intervalos. Consideró puertos y
adaptadores: más interfaces/mapeos sin segunda persistencia que los justificara. La
arquitectura quedó pendiente entonces; no hubo rechazo humano ficticio de capas.

### DEC-004 y DEC-005 — Decisiones humanas,75af194

Usuario decidió que el starter no llegaría: adoptar repositorio actual como base
**definitiva**, sin recepción/comparación/migración/defecto equivalente. T014 no aplica,
no implementada. WebLogic/T015 sigue separado. Después eligió monolito por funcionalidades:
booking con controlador/servicio/repositorio/DTO/entidad, space, security y errors si es
compartido; Angular acceso/reservas. Esta elección concreta/modifica la organización
propuesta, no implica otra infraestructura ni una discusión técnica que no ocurrió.

### DEC-006 — T003/H002,992bc41

Agente implementó entidades/JPA y DDL explícito Oracle, dos tablas y dos secuencias,
Hibernate validate, sin H2 ni create-drop. Prefirió SQL controlado a instalar un motor
de migraciones para una V001; DDL Oracle no es reversible por rollback. Semilla MERGE
insert-only. OffsetDateTime/NATIVE conserva instante/precisión. Primeros 12 tests:6 fallos
del test por buscar SQLException solo en causa más profunda; corregida inspección de
cadena y 12/12 correctos. Reaplicar schema rechazó ORA-20001; tres espacios/cero reservas.
No había API/UI/colisiones propias aún. Fue un defecto real de prueba, no el del starter.

### DEC-007 y DEC-008 — T004/T005/H002,14fa96d

Usuario autorizó continuar T004–T013 autónomamente. Parámetros concretos semanales,
Bogotá y límites fueron decisiones técnicas del agente dentro de ese alcance.
Agente eligió Basic por petición, BCrypt para verificación, secretos externos y sesión
solo CSRF con token XOR; evitó cookie de token legible. Primer ciclo 15 tests:un fallo
por rotación del token con Basic; NullAuthenticatedSessionStrategy conserva CSRF y
corrige múltiples escrituras.15/15 finales. DTO/campos desconocidos y errores redactados.

### DEC-009 — T006/T007/H003,8ba5cf3

Agente concretó intervalos semiabiertos y bloqueo PESSIMISTIC_WRITE de Espacio bajo
READ_COMMITTED para crear/cancelar. Bloquear reservas no protege sala vacía; synchronized
no cubre varias JVM. Dueño del principal, propia ACTIVE cuyo fin aún no pasa, cancelación
idempotente y ajena/ausente 404 indistinguible. No requisito que prohíba pasado.27/27
contra Oracle; no se acreditó carrera hasta la etapa dedicada.

### DEC-010 — T008/H004,7360c03

Angular por funcionalidades, credenciales/token en memoria e interceptor solo/api/.
Agente descartó localStorage por persistencia innecesaria; proxy local sin CORS abierto.
HTTP real encontró 403 tras GET paralelos aunque MockMvc pasaba: cambiar contexto de
seguridad de NEVER a STATELESS conservó la sesión explícita CSRF. No se desactivó CSRF.
Regresión 27/27 + prueba HTTP 1/1, frontend build/test y navegador (#55 cancelada).

### DEC-011 — T009/T010/H005,bc5a56a

Semanas 1–12, primera incluida, zona Bogotá; una transacción para todo el pedido y
resultados created/rejected. Conflictos no lanzan excepción; fallo técnico sí revierte.
All-or-nothing para conflictos contradice aceptación parcial y no se implementó.
Sin entidad Serie/cron.35/35 Java, UI 3 creadas/1 rechazada (#72–75 luego canceladas).
Se corrigieron lectura UTF8 y vigilancia Windows por polling; no eran defecto sembrado.

### DEC-012 — T011/T012/H006,0344b9a

Prueba HTTP con dos transacciones y bloqueo JDBC externo:201+409, una fila. Spy observa
y delega Oracle real; el primer spy falló al invocar método abstracto del proxy, se
corrigió delegación. Fallo técnico después de flush demuestra rollback fuera de la
transacción. Otro hallazgo: excepción inesperada iba a/error protegido y parecía 401;
ApiErrors conservó 500 genérico.39/39 finales. JWT/MSAL quedan condicionados e inactivos:
claims/derivación de identidad y compilación comprobadas, no Azure/JWKS/firma real.
Revisión npm 0 entonces no era SCA Java integral.

### DEC-013 — T013/H007,4100673

Entrega local con app.ps1 y WAR ejecutable Tomcat.50/50 Java, frontend build/test 1/1,
stop/start, marcador y secretos intactos. Agente documentó 12contenidos,15preguntas y
6ejercicios analíticos. start reutilizaba WAR si existía: decisión operativa que luego
se corrigió en DEC-021. T015 no se consideró resuelta.

### DEC-014 — T016,47a6af4

Usuario reportó fechas iguales y espera. HTTP devolvía 400; no se estableció causa exacta
de la espera inicial ni se culpó a extensiones/Oracle. Agente agregó validación de
FormGroup y timeout 15s, sin retry de escritura: timeout no demuestra rollback.13 tests
frontend/build pasaron; navegador corrigió fechas, creó/rechazó duplicado/canceló#157.
No se repitieron las 50 pruebas backend para este cambio solo cliente.

### DEC-015 — T017/AUD-01–08,e170418

Usuario pidió auditoría sin correcciones. Revisión 47a6af4:75 tests Java (69pasan/6fallan)
y otra ejecución posterior 1/1 de espera Oracle;76casos distintos, no una sola suite 76.
Frontend 13/13/build. Fallaron dos coerciones decimales, tres clasificaciones MVC y
OpenAPI incompleto. Fixtures#204–209 quedaron CANCELLED; no se cancelaron originales.

Consulta OSV con inventario privado fue rechazada por revisión automática. Se optó por
catálogos públicos completos y comparación **local**, sin eludirla.65 coincidencias Maven,
0npm, no 65explotaciones. Comparador corregido para unión de intervalos,3 tests. Revisión
274blobs de secretos conocidos sin coincidencias, con límites. Las seis regresiones
quedaron rojas; WAR previo seguía operativo. No se aceptó cumplimiento integral.

### DEC-016 — Autorización humana,1a4efd0

Usuario autoriza T018–T022, excluye T023/T024, mantiene T015 separado y pide alternativa
concreta para excepciones al stack. Se conserva base e170418 sincronizada tras push
previo autorizado. Instrucción aporta condición Dev Container sin afirmar lectura del
correo ausente en este equipo. Copia privada inicial de hashes/filas/WAR protege datos.

### DEC-017 — T019/T020,3447ad3

Agente desactiva coerción float/string a integer:1válido,1.75/1.0/1e0/"1"400 antes del
servicio; null/ausente occurrences=1. Corrige MVC 400/404/405+Allow y conserva 500/503.
Antes de modificar expectativa se explicó que GET de ruta existente DELETE es 405;
se usa ruta realmente inexistente para 404 y se añade GET 405. No se relajó para ocultar
500.12casos dirigidos pasaron; cierre posterior incluyó HTTP real/WAR nuevo.

### DEC-018 y DEC-019 — T018,5a7d688

Agente elige Tomcat 10.1.59 público y alineado; eager headers según mitigación oficial
CVE-2026-22732 en Basic/JWT. Prueba del filtro efectivo falla antes (nosniff ausente
al entrar al downstream), pasa después; no PoC del camino original del CVE. Caché explícita
posterior puede sobrescribir su cabecera.5 tests cabeceras/Oracle y 2recursos/multipart pasan.

Propuesta exacta resuelta aparte: Boot 3.5.16/Framework 6.2.19/Security 6.5.11/DataJPA 3.5.13/
Hibernate 6.6.53.Final/SpringDoc 2.8.17/Tomcat 10.1.59. Usuario eligió **mantener stack obligatorio
por ahora y dejar pendiente lo no resuelto**. No se aplica ni se acepta riesgo en su nombre.
43 coincidencias restantes:1 mitigada,13 no aplicables a configuración comprobada y 29 pendientes.
22 coincidencias Tomcat corregidas por versión. Detalle individual en documento de seguridad.

### DEC-020 — T021,5a7d688

Agente documenta Basic AND csrfToken en un mismo requisito OpenAPI, respuestas específicas,
sesión/token real y docs protegidas mediante desafío Basic.4 tests dirigidos y HTTP pasan.
Escritura UI quedó pendiente por autenticación/control de navegador. Usuario abrió Swagger
posteriormente; herramienta Windows se detuvo al no determinar URL con suficiente confianza.
No se ejecutó escritura ni se cierra la tarea por esa apertura. No desactivar CSRF.

### DEC-021 — T022,e859c80 y eee557b

Agente une Composes con imágenes importadas y elimina build para reapertura; initialize
no genera claves. start verifica fuentes/hash del WAR; clean verify registra revisión.
CLI up inicial falló por CRLF de edición Bash; corregido LF, up exitoso y script de
postCreate ejecutado explícitamente por exec al omitir CLI el lifecycle ya intentado.

**Una ejecución final 88/88 Java,0 fallos/errores/omitidas**, fuentes e859c80; frontend 13/13,
build, comparador 3/3. WAR SHA256 a2ab5fe1a6d301d45a94fc7f26415326b7e1fdfbc2567e37f527e269a7d13aa5,
2026-09-14T23:52:53Z. HTTP 12 comprobaciones, decimales 400 sin escrituras; navegador
creación/listado/duplicado/3+1/cancelaciones#256–259. Dos avisos de acceso inválido entre
pestañas se resolvieron reingresando; causa no trazada.17 filas preexistentes y 5 archivos
clave/credenciales/marcador idénticos; Oracle saludable y localhost. No nuevo reinicio Oracle.

No se probó instalación limpia ni editor VS Code visual. eee557b consolida evidencias;
el usuario luego pidió push: cinco commits e170418..eee557b subidos, remoto/local iguales.
El bundle local anterior sirve como entrega de esa revisión, no de cambios futuros.

### DEC-022 y DEC-023 — T025/H007,d6e45ad

Usuario autoriza esta consolidación, tres documentos principales, estudio fuera del
repositorio y ajustes operativos mínimos para Windows/Linux; no negocio/arquitectura/push.
Se mostró tabla de destinos antes de retirar. Respaldo fechado externo:60 archivos,
41 Markdown propios/19 anexos; siete sin copia previa idéntica, todos con SHA256 verificado.
Se conservan rutas relativas/copias anteriores. Inventario externo registra destino.

Agente decide un operador Node/Docker común frente a exigir pwsh Linux o duplicar Bash.
Tradeoff: Node>=18 en host, Linux nuevo UID1000 para montaje/secretos 0600. Dev Container
usa ese check; no regenera credenciales. prepare-new rechaza proyectos/datos existentes;
images solo base nueva. SQL por stdin mantiene clave dentro de Oracle. Pruebas 4/4 Windows
Node 18.19.1 y 4/4 Linux Node 22.22 dentro de dev, con Docker simulado en tests de guardas.
Windows real check/start/db-status y CLI up pasaron. Primer db-status tuvo error por
sentencias SQLPlus en una línea; salto de línea lo corrigió. El test de start inicialmente
esperaba HTTP real; se acotó su stub, sin atribuir un fallo funcional a Booking.

No se vuelve a ejecutar 88 tests por consolidación. Comprobaciones de enlaces, invariancia
de negocio, secretos y operación se registran en [VERIFICACION](docs/VERIFICACION.md).
La explicación personal y guía de ejercicios son externas; ningún comando depende de ellas.

Cierre del agente para DEC-022/023: 20 Markdown y 11 anexos redundantes retirados
solo tras comprobar sus copias; 23 Markdown propios vigentes, herramientas intactas.
Requisitos originales de AGENTS conservados literalmente; constitución 1.1.2 editorial.
Documentos externos: 16 secciones técnicas, 25 preguntas respondidas y 8 ejercicios
sin implementar. 137 enlaces locales válidos entre repo y documentos de estudio.
Ciclo real stop/start posterior: Oracle healthy, 22 filas actuales y cinco archivos
sensibles intactos, marcador original leído de nuevo, HTTP 4200/8080 correcto y WAR
actual. Sin diff de negocio, infraestructura base ni terceros; sin secretos conocidos
introducidos. La instalación nueva completa/Linux host sigue no probada. Cierre T025
por evidencia disponible, no por suponer resueltos T018/T021/T015. Sin push.

## Hallazgos finales y pendientes (resumen; estado canónico en tareas)

| Hallazgo | Evolución y evidencia |
|---|---|
| AUD-01/T018 |65→43 coincidencias. Tomcat parchado; Security mitigado;29 pendientes, sin aceptación del riesgo. |
| AUD-02/T019 |Coerción silenciosa corregida; regresiones y HTTP al WAR nuevo 400/cero escrituras. |
| AUD-03/T020 |MVC 500 indebidos corregidos;400/404/405+Allow,500/503 técnicos preservados. |
| AUD-04/T021 |OpenAPI corregido y probado; escritura interactiva pendiente. |
| AUD-05/T022 |Dev Container CLI real acreditado; no instalación limpia/editor visual. |
| AUD-06/T015 |WebLogic sin aclaración/validación de runtime; independiente del starter. |
| AUD-07/T023 |Permisos Oracle mínimos: recomendación opcional, no implementada. |
| AUD-08/T024 |Escala/endurecimiento adicional: opcional, sin implementar ni inventar SLA. |

## Documentos retenidos y trazabilidad

README: ejecución; AGENTS: requisitos/extensiones; BITACORA: historia consolidada.
Contrato API conserva detalles de Swagger; tratamiento T018 conserva aviso por aviso y
alternativa no aplicada; VERIFICACION conserva matrices/resultados; transferencia conserva
procedimiento cifrado todavía utilizable; SPECKIT conserva herramientas. specs/.handoffs
mantienen continuidad y estados. Anexos actuales de correction-evidence conservan inventario
resuelto/rangos/WAR/HTTP sin repetir informes narrativos. Los informes y planes iniciales
redundantes se retiran del árbol después de su respaldo; Git conserva toda su historia.

Para consultar un original sin restaurarlo sobre el checkout: `git show eee557b:RUTA`.
La ruta/versión exacta de cada archivo consta en el inventario externo de consolidación;
no se necesita ese material personal para ejecutar o evaluar los requisitos actuales.

## DEC-024 — Recuperar verify en un clon nuevo, 2026-09-15

Origen humano: el usuario clonó el repositorio, completó prepare-new/images/up/schema/seed
y reportó el fallo de verify. El diagnóstico y los ajustes mínimos los eligió el agente.
El log muestra ORA-01017 y fallos derivados al cargar ApplicationContext. SQL*Plus
conecta como BOOKING/FREEPDB1; ambos contenedores montan el mismo secreto de 52 bytes,
sin espacios. La sonda JDBC de solo lectura falla con 21.9.0.0 y conecta con
23.26.3.0.0 usando exactamente esa misma configuración. Oracle documenta el soporte
de claves de más de 30 bytes en JDBC 23; versión confirmada en Maven Central.

Se fija ojdbc11 23.26.3.0.0 en backend/pom.xml e infra/checks/pom.xml. Se conserva
la contraseña existente en lugar de rotarla o regenerar el entorno. No se cambia
Spring Boot, el modelo ni el SQL. La elección de versión es del agente, no una
petición literal del usuario. No constituye el cierre de la auditoría T018.

También se reproduce el rechazo Git por propiedad del montaje Windows /workspace.
backend-artifact.sh establece la excepción limitada a esa ruta dentro de Docker
antes de registrar metadatos; verify-local.sh ya la aplicaba, pero demasiado tarde
para el primer build. Se conserva su comportamiento idempotente y no se usa '*'.

Validación final y límites: véase docs/VERIFICACION.md, sección DEC-024. Cambios
locales; sin publicación. Los resultados anteriores siguen siendo históricos.

Fuentes: [Oracle JDBC, soporte de contraseñas largas](https://docs.oracle.com/en/database/oracle/oracle-database/23/jjdbc/jdbc-developers-guide.pdf),
[artefacto oficial en Maven Central](https://repo.maven.apache.org/maven2/com/oracle/database/jdbc/ojdbc11/23.26.3.0.0/).

## DEC-025 — Guía Docker Windows/Linux, 2026-09-15

El usuario pidió ampliar README.md para futuras instalaciones Docker en Windows y
Linux, incluyendo los fallos encontrados y otros puntos útiles. El agente añadió
preparación por plataforma, comprobaciones previas, acceso GitHub con código,
requisitos de la revisión DEC-024, resultados esperados, puertos, persistencia y
recuperación de errores, y captura de logs en PowerShell/Bash. Se verificaron las
instrucciones contra scripts/Compose/Dockerfile y documentación oficial de Docker
y GitHub. El UID1000 sigue siendo una restricción del bootstrap Linux.

Cambio documental: no reinstalar, reiniciar ni repetir pruebas funcionales. La
verificación real DEC-024 en Windows se conserva separada de la guía para host
Linux aún no probado. No se publica código ni se cambia estado de T018/T021/T015.
Validación: revisión de comandos/enlaces y git diff --check.
