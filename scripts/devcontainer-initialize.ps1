#requires -Version 7.0
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    foreach ($required in @('.local/transfer/compose.images.yaml','.local/secrets/app-password','.local/secrets/oracle-password')) {
        if (-not (Test-Path -LiteralPath $required -PathType Leaf)) {
            throw "Falta $required. Complete la restauración o el bootstrap NUEVO de docs/DEV_CONTAINER.md antes de abrir. No se generaron credenciales."
        }
    }
    $files = @('-f','compose.yaml','-f','.local/transfer/compose.images.yaml','-f','.devcontainer/compose.restored.yaml')
    & docker compose @files config --quiet
    if ($LASTEXITCODE -ne 0) { throw 'Configuración Compose inválida.' }
    $images = @(& docker compose @files config --images)
    if ($LASTEXITCODE -ne 0) { throw 'No se pudieron resolver las imágenes.' }
    foreach ($image in $images) {
        & docker image inspect $image --format '{{.Os}}/{{.Architecture}}'
        if ($LASTEXITCODE -ne 0) { throw "Falta imagen local $image. Importe el respaldo o complete el bootstrap nuevo." }
    }
    Write-Host 'Configuración existente validada; secretos y datos conservados.'
} finally { Pop-Location }
