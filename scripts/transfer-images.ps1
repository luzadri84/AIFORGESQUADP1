#requires -Version 7.0
param(
    [ValidateSet('export','import')]
    [string]$Action = 'export',
    [string]$Directory = '.local/transfer'
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $root
$destination = [IO.Path]::GetFullPath($Directory, $root)
function Invoke-TransferDocker {
    & docker @args
    if ($LASTEXITCODE -ne 0) { throw "Docker fallo con codigo $LASTEXITCODE." }
}
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw 'Falta Docker en PATH.' }
$platform = Invoke-TransferDocker info --format '{{.OSType}}/{{.Architecture}}'
if ($platform -notin @('linux/x86_64','linux/amd64')) {
    throw "Este paquete requiere Docker Linux amd64; motor actual: $platform. No se fuerza emulacion."
}
$manifestPath = Join-Path $destination 'manifest.json'
if ($Action -eq 'import') {
    if (-not (Test-Path -LiteralPath $manifestPath)) { throw "Falta $manifestPath" }
    $manifest = Get-Content -LiteralPath $manifestPath -Raw | ConvertFrom-Json
    if ($manifest.platform -ne 'linux/amd64') { throw 'Plataforma de paquete no soportada.' }
    # Validate all files before loading any image.
    foreach ($entry in $manifest.images) {
        if ([IO.Path]::GetFileName($entry.file) -ne $entry.file) { throw 'Nombre de archivo no valido en manifiesto.' }
        $archive = Join-Path $destination $entry.file
        if ((Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLower() -ne $entry.sha256) {
            throw "Checksum incorrecto: $($entry.file)"
        }
    }
    foreach ($entry in $manifest.images) {
        Invoke-TransferDocker image load --input (Join-Path $destination $entry.file)
        $loaded = Invoke-TransferDocker image inspect $entry.tag --format '{{.Os}}/{{.Architecture}}'
        if ($loaded -ne 'linux/amd64') { throw "Arquitectura inesperada: $($entry.tag)" }
    }
    Write-Output 'Imagenes cargadas. Prepare secretos nuevos y use compose.images.yaml con --no-build.'
    exit 0
}
if (Test-Path -LiteralPath $manifestPath) {
    throw 'Ya existe un paquete en ese directorio. Conservelo o use -Directory con una carpeta nueva.'
}
New-Item -ItemType Directory -Force -Path $destination | Out-Null
$sources = [ordered]@{
    dev = 'booking-local-dev:latest'
    oracle = 'gvenzl/oracle-free:23.26.3-slim@sha256:6d61d267a3b978c24c5ac1790e62e927416a0aec446bd86e4b3a1527562757bd'
}
$entries = @()
foreach ($service in $sources.Keys) {
    $source = $sources[$service]
    $details = (Invoke-TransferDocker image inspect $source --format '{{json .}}') | ConvertFrom-Json
    if ($details.Os -ne 'linux' -or $details.Architecture -ne 'amd64') { throw "Plataforma inesperada: $source" }
    $suffix = ($details.Id -replace '^sha256:', '').Substring(0,12)
    $tag = "booking-transfer/${service}:$suffix"
    Invoke-TransferDocker image tag $source $tag
    $tarPath = Join-Path $destination "$service-linux-amd64.tar"
    $gzipPath = $tarPath + '.gz'
    Write-Output "Exportando $service (imagen, sin volumenes ni capa mutable del contenedor)..."
    Invoke-TransferDocker image save --platform linux/amd64 --output $tarPath $tag
    $inputStream = [IO.File]::OpenRead($tarPath)
    try {
        $outputStream = [IO.File]::Create($gzipPath)
        try {
            $gzip = [IO.Compression.GZipStream]::new($outputStream, [IO.Compression.CompressionLevel]::Fastest, $true)
            try { $inputStream.CopyTo($gzip) } finally { $gzip.Dispose() }
        } finally { $outputStream.Dispose() }
    } finally { $inputStream.Dispose() }
    $entries += [ordered]@{
        service = $service
        tag = $tag
        source = $source
        imageId = $details.Id
        file = [IO.Path]::GetFileName($gzipPath)
        sha256 = (Get-FileHash -LiteralPath $gzipPath -Algorithm SHA256).Hash.ToLower()
        bytes = (Get-Item -LiteralPath $gzipPath).Length
    }
}
$devTag = $entries[0].tag
$oracleTag = $entries[1].tag
$override = "services:`n  dev:`n    image: $devTag`n    pull_policy: never`n  oracle:`n    image: $oracleTag`n    pull_policy: never`n"
[IO.File]::WriteAllText((Join-Path $destination 'compose.images.yaml'), $override)
$revision = git rev-parse HEAD
if ($LASTEXITCODE -ne 0) { throw 'No se pudo leer el commit de origen.' }
$manifest = [ordered]@{
    createdUtc = [DateTime]::UtcNow.ToString('o')
    platform = 'linux/amd64'
    gitCommit = $revision
    includesVolumes = $false
    includesSecrets = $false
    images = $entries
}
[IO.File]::WriteAllText($manifestPath, ($manifest | ConvertTo-Json -Depth 6) + "`n")
git bundle create (Join-Path $destination 'booking-source.bundle') --branches --tags HEAD
if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear el bundle Git.' }
git bundle verify (Join-Path $destination 'booking-source.bundle')
if ($LASTEXITCODE -ne 0) { throw 'Bundle Git invalido.' }
$checksums = foreach ($file in @('dev-linux-amd64.tar.gz','oracle-linux-amd64.tar.gz','booking-source.bundle','compose.images.yaml','manifest.json')) {
    $hash = (Get-FileHash -LiteralPath (Join-Path $destination $file) -Algorithm SHA256).Hash.ToLower()
    "$hash  $file"
}
[IO.File]::WriteAllLines((Join-Path $destination 'SHA256SUMS.txt'), $checksums)
Write-Output "Paquete creado en $destination. Transfiera los .tar.gz, el bundle, YAML, manifest y checksums; los .tar son intermediarios."