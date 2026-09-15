#requires -Version 7.0
# Only for a NEW, empty installation. Never use this to restore a backup.
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    if (Test-Path '.local') { throw 'Existe .local: no es una instalación vacía. Use restauración/reapertura; no se modificó nada.' }
    $volumes = @(& docker volume ls --format '{{.Name}}')
    if ($LASTEXITCODE -ne 0) { throw 'Docker no disponible.' }
    if ($volumes -contains 'booking-local_oracle-data') { throw 'Existe el volumen Oracle del proyecto. No se regenerarán credenciales.' }
    $containers = @(& docker ps -aq --filter label=com.docker.compose.project=booking-local)
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo consultar Docker.' }
    if ($containers.Count -gt 0) { throw 'Ya existe el proyecto booking-local. Use otra máquina o revise el entorno existente.' }
    & pwsh -NoProfile -File scripts/local.ps1 prepare
    if ($LASTEXITCODE -ne 0) { throw 'Falló preparación local.' }
    & docker build -f .devcontainer/Dockerfile -t booking-local/dev:checkout .
    if ($LASTEXITCODE -ne 0) { throw 'Falló construcción. Se conservaron los secretos nuevos; revise README.md.' }
    $oracle = 'gvenzl/oracle-free:23.26.3-slim@sha256:6d61d267a3b978c24c5ac1790e62e927416a0aec446bd86e4b3a1527562757bd'
    & docker pull $oracle
    if ($LASTEXITCODE -ne 0) { throw 'Falló descarga Oracle; no se inició la base.' }
    New-Item -ItemType Directory -Path '.local/transfer' -Force | Out-Null
    $overlay = "services:`n  dev:`n    image: booking-local/dev:checkout`n    pull_policy: never`n  oracle:`n    image: $oracle`n    pull_policy: never`n"
    [IO.File]::WriteAllText((Join-Path (Get-Location) '.local/transfer/compose.images.yaml'),$overlay)
    Write-Host 'Instalación nueva preparada. Abra Dev Container y aplique schema/seed una sola vez; consulte README.md.'
} finally { Pop-Location }
