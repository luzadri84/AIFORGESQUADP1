# Spec Kit en este repositorio

Specify CLI **0.8.1** ya estaba instalado mediante uv desde el tag oficial
[v0.8.1](https://github.com/github/spec-kit/releases/tag/v0.8.1), commit
`a63f64b69dfd9785360cac83e6a0221f6ff9f444`. Se reutilizó, junto con su Python 3.11.9;
no se instaló otra copia ni se cambiaron Java, Node, Docker o las cachés.
[Configuración reproducible](../.specify/toolchain.json).

## Comandos comprobados y codificación Windows

En PowerShell 7 desde la raíz:

```powershell
$env:PYTHONIOENCODING = 'utf-8'
$env:PYTHONUTF8 = '1'
specify version
specify --help
specify init --help
specify check
specify integration list
```

La salida inicial de `specify version` falló al imprimir caracteres Unicode con
cp1252. Las variables anteriores corrigen la salida del proceso sin reinstalar
Python ni modificar globalmente el equipo. `specify check` comprueba herramientas;
no demuestra por sí solo descubrimiento de skills o funcionamiento de Booking.

## Integración seleccionada

Se generó en `.local/speckit-stage` con el comando fijado en toolchain.json, usando
recursos incluidos en la instalación (`--offline`) y `--no-git`. Se seleccionaron
individualmente los archivos de los manifests de Codex y del núcleo, se comprobó
la ausencia de destinos existentes y se preservaron sus bytes. No se incorporaron
el AGENTS generado ni el workflow automático. Las plantillas oficiales son ejemplos,
no nuevas instrucciones que deroguen AGENTS o la autorización de cada fase.

Nueve skills oficiales viven en `.agents/skills/`: `speckit-constitution`,
`speckit-specify`, `speckit-plan`, `speckit-tasks`, `speckit-implement`,
`speckit-clarify`, `speckit-analyze`, `speckit-checklist` y `speckit-taskstoissues`.
Su invocación es `$speckit-<nombre>` dentro de Codex; no es una orden PowerShell.
No se asumen los antiguos comandos `/speckit.*`. La
[documentación de Codex](https://learn.chatgpt.com/docs/build-skills)
indica descubrimiento en `.agents/skills` desde el directorio de trabajo hasta la
raíz del repositorio. Abrir Codex sobre este repositorio, no la carpeta de apoyo.

No se invocó `speckit-implement`: su alcance por defecto es todo tasks.md. H001 se
integra con instrucciones directas acotadas a T001/T002. Tampoco se ejecutan
`taskstoissues`, workflows, publicaciones o agentes paralelos. Cada futura
invocación debe preservar IDs y limitarse al bloque autorizado; si la skill no
permite ese límite, usar instrucciones directas con los documentos canónicos.

## Selección de feature sin cambiar la rama

La feature única es `specs/001-booking-espacios`. Los scripts 0.8.1 comprueban el
nombre de feature: desde `main` establecer el selector soportado solo en la sesión:

```powershell
$env:SPECIFY_FEATURE = '001-booking-espacios'
pwsh -NoProfile -File .specify/scripts/powershell/check-prerequisites.ps1 -Json -RequireTasks -IncludeTasks
```

Esto selecciona documentos; no crea ni cambia ramas Git. No ejecutar generadores
`create-new-feature.ps1` o `setup-plan.ps1` para volver a crear Booking o sustituir
el plan ya reconciliado. El estado de tareas reside únicamente en tasks.md.

## Reproducción en otra máquina

Primero comprobar versión/ayuda y reutilizar una instalación compatible. Solo si
falta, el comando oficial fijado de recuperación es:

```powershell
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git@v0.8.1
```

La integración del repositorio está versionada: un clon no necesita ejecutar
`specify init --here`. Para comparar una futura actualización, generar en otra
carpeta temporal nueva y revisar cada diff, nunca aplicar `--force` al proyecto.
Los manifests preservan hashes originales para detectar cambios locales.

## Evidencia de descubrimiento H001

El 2026-09-14, Codex CLI 0.154.0-alpha.6.2 respondió a `skills/list`
con `cwds` apuntando a la raíz y `forceReload=true`: las nueve skills anteriores
tenían `scope=repo`, `enabled=true` y cero errores del proyecto. Se consultó
por stdio tras `initialize`, sin crear tareas ni ejecutar modelos. La salida
local resumida queda en `.local/h001-skill-discovery.json`; no es necesaria para
usar el repositorio. La comprobación no automatizó la UI del selector de skills.
