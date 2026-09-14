# Integración H001: estado y evidencia real

> Registro de su etapa histórica. La aplicación posterior y su operación vigente
> están en [README](../README.md) y [VERIFICACION_FINAL](VERIFICACION_FINAL.md).

> Actualización de alcance — DEC-004 (2026-09-14): este documento conserva el
> registro/propuesta de su etapa original. Las instrucciones de esperar, recibir,
> comparar o migrar un starter e investigar su defecto quedaron superadas: el
> repositorio actual es la base definitiva. No aplica por cambio de alcance;
> no se acredita implementación de esas tareas. WebLogic sigue independiente.
> Reglas y estado vigentes: AGENTS.md, tasks.md y docs/BITACORA.md.

Registro: 2026-09-14, America/Bogota. Ejecutó: Codex en Windows 10/PowerShell 7.
Estado de T001/T002 y siguientes únicamente en [tasks.md](../specs/001-booking-espacios/tasks.md).
[Handoff](../.handoffs/H001-integracion.md), [Spec Kit](SPECKIT.md), [bitácora](BITACORA.md).

## Punto de entrada e historia conservada

Repositorio de trabajo solicitado: `C:\PruebaAIFORGESQUAD`; Git main,
HEAD inicial `5dcdb7c4d0b42e812b3cca8169a5df3cd7948466`, árbol limpio y siete commits.
No existían .specify, specs, .handoffs, .agents o .codex en el repositorio. No había
backend/frontend Booking, entidades, tests funcionales ni WAR. La arquitectura de
los documentos históricos seguía siendo propuesta; las sondas no son aplicación.

Los tres commits de preparación existen: 552db96 (documentos/requisitos), b219204
(infraestructura/Oracle), 4b66e6d (parches Angular/evidencia). Git muestra sus horas
12:12:26, 12:20:21 y 12:33:06 -05 del mismo día. Los otros cuatro commits documentan
transferencia, corrección del exportador, respaldo cifrado y verificación en origen.
Los «37 minutos» son reporte previo sin cronometraje verificable; no se infieren
minutos efectivos a partir de timestamps ni se altera la historia para coincidir.

La bitácora y VERIFICACION_LOCAL.md previas describen el equipo origen; se conservan.
La restauración Windows 10 fue ejecutada en el turno anterior y quedó resumida en
.local/RESTAURACION_VERIFICADA.md (no necesario para usar SDD): ocho assets validados,
imágenes/datos/credenciales importados, JDBC/ngc y marcador antes/después de reinicio.
H001 verifica preservación ahora; no pretende repetir ni fechar retrospectivamente
esas operaciones. El marcador original y las contraseñas permanecen en .local.

## Comparación y fusión selectiva

| Fuente comparada | Incorporación/decisión |
|---|---|
| Guía 07, sección 3 Prompt A | H001 solamente; decisión de arquitectura y autorización futuras |
| Guía 06 | Una feature, scripts PowerShell, integración Codex y handoffs sin motor |
| Referencia 05 | Reglas agregadas a AGENTS; DEC en bitácora existente, separando origen/decisor/prueba |
| Semilla constitution | Principios adaptados a la plantilla real; ratificación operativa no aprueba arquitectura |
| Semilla spec/plan | IDs FR/US conservados; versiones efectivas y ausencia de negocio; diseño pendiente explícito |
| Semilla tasks/trazabilidad | Mismos T001–T015, estado basado en código; criterios H007/H008 ampliados sin duplicar tareas |
| Ocho handoffs e índice | Contexto y próximo paso; criterios y estados canónicos en tasks |
| Fragmento AGENTS_ADICION_SDD | Fusionado con reglas previas; no se crea un segundo AGENTS rector |
| Guía 07, secciones 6/7 | Criterios autocontenidos en ENTREGA_Y_STARTER.md; sin explicación ficticia de código |
| Generación oficial 0.8.1 temporal | 22 archivos seleccionados mediante manifests: nueve skills, cuatro scripts, cinco plantillas y cuatro metadatos |
| AGENTS/workflow generados | No incorporados; se conservó AGENTS original y no se añadió workflow automático |
| README existente | Mantiene contexto y enlaces; comandos actuales usan imágenes importadas y lectura del marcador |

No se copió overlay/ encima del proyecto. La trazabilidad de todos los documentos
comparados figura por nombre relativo y SHA-256 en
[support-sources.json](../.specify/support-sources.json). Estos nombres identifican
el paquete recibido, no dependencias de ejecución externas. El repositorio puede
usarse sin la carpeta de apoyo. El plan histórico versionado se conserva como referencia;
las versiones posteriores verificadas prevalecen sobre sus candidatas antiguas.

## Comprobaciones ejecutadas en H001

| Comprobación | Resultado y alcance |
|---|---|
| Git status/branch/log iniciales | main, 5dcdb7c, siete commits, sin cambios ajenos pendientes |
| uv tool list / metadata de instalación | specify-cli 0.8.1, origen oficial v0.8.1/commit a63f64b; se reutilizó |
| specify version / --help / init --help / check | Correctos después de ajustar UTF-8 para la salida del proceso |
| specify init temporal offline/no-git | Correcto; no inicializa otro Git ni modifica documentos del proyecto |
| specify integration list en raíz fusionada | codex installed; versión registrada 0.8.1 |
| Manifests SHA-256 | Scripts, plantillas y skills coinciden con los archivos oficiales generados |
| Codex app-server skills/list | Nueve speckit-* scope=repo/enabled=true; cero errores del proyecto; sin modelo ni tarea adicional |
| check-prerequisites.ps1 -Json -RequireTasks -IncludeTasks | Con SPECIFY_FEATURE=001-booking-espacios devuelve la feature y tasks.md |
| docker compose con ambos archivos: config --quiet | Configuración válida sin imprimir secretos |
| exec dev bash scripts/jdbc-check.sh read | BOOKING/FREEPDB1, JDBC 21.9.0.0.0; marcador original coincide |
| exec dev npm --prefix infra/checks/frontend run check | ngc estricto correcto usando node_modules existente |
| compose ps | dev activo, Oracle healthy, 127.0.0.1:4200/8080; Oracle sin publicación |

La primera ayuda Unicode falló con cp1252; PYTHONIOENCODING=utf-8/PYTHONUTF8=1 lo
resolvió. La consulta inicial de integración antes de fusionar devolvió correctamente
«no .specify»; después identifica codex instalado. El sandbox de lectura Git detectó
otro propietario: se usó ejecución autorizada como usuario del repositorio, sin
safe.directory=* ni cambiar propietarios. No se ocultaron esos intentos fallidos.

No se ejecutó prepare, npm ci, write, reinicio, importación ni rebuild en H001.
No se ejecutaron speckit-implement/taskstoissues, workflows, otros agentes o push.
La consulta skills/list demuestra descubrimiento por el motor, no interacción con
la UI del selector ni ejecución completa de las nueve skills generadoras.

## Revisión de cierre

Se comprobaron 20 documentos y sus 100 enlaces locales, referencias T/FR/H y los 15
IDs de tareas sin duplicados. Los cuatro scripts PowerShell oficiales pasan análisis
sintáctico. AGENTS y bitácora originales son prefijos íntegros del texto ampliado.
El diff contra 5dcdb7c en Compose, devcontainer, scripts, infra, Maven wrapper,
.dockerignore y .env.example está vacío. Contraseñas y marcador coinciden con la
copia del respaldo, sin imprimir valores; clave/secretos/archivos privados ignorados.
Los documentos canónicos son versionables. Se conservó la licencia oficial en
`.specify/LICENSE`. `.gitattributes` fija LF para skills/plantillas de manera que un
clon Windows no altere sus hashes. Diff revisado con `git diff --check`.

## Recomendación y pendientes

[DEC-003/plan](../specs/001-booking-espacios/plan.md): monolito sencillo con controlador,
servicio transaccional y Spring Data JPA, más funciones de dominio pequeñas; Angular
standalone. Alternativa: puertos/adaptadores dentro del monolito, con más interfaces
y mapeos. No existe negocio que necesite migración; conservar infra actual en ambos.
Propiedad, CSRF, concurrencia, validación y pruebas reales son garantías exigidas.
La elección humana todavía no está registrada. El backlog no equivale a aprobación.

Primera tarea funcional: T003/H002, entidades/esquema/semillas idempotentes y mapeo
Oracle, tras decisión de arquitectura y autorización del bloque. T004/T005 continúan
con identidad/CSRF y contrato. Starter/T014 y aclaración de runtime/T015 siguen abiertos.
El límite actual es H001: no se creó aplicación, WAR, compatibilidad ficticia ni despliegue.

## Commits locales

- `b977ca3`: integración técnica ya comprobada, T001/DEC-001.
- El cierre documental posterior se identifica por T002/DEC-002/DEC-003 en Git; no
  se inserta su hash futuro en el mismo commit. No se publicó ni envió al remoto.
