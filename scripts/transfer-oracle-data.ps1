#requires -Version 7.0
param(
    [ValidateSet('export','import','encrypt','decrypt')]
    [string]$Action = 'export',
    [string]$Directory = '.local/transfer-private',
    [string]$KeyFile = '.local/transfer-private/restore-key.pem'
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $root
$backupPath = [IO.Path]::GetFullPath($Directory, $root)
$volume = 'booking-local_oracle-data'
$helper = 'booking-local-dev:latest'
function Invoke-BackupDocker {
    & docker @args
    if ($LASTEXITCODE -ne 0) { throw "Docker fallo con codigo $LASTEXITCODE." }
}
$platform = Invoke-BackupDocker info --format '{{.OSType}}/{{.Architecture}}'
if ($platform -notin @('linux/x86_64','linux/amd64')) { throw 'Se requiere Docker Linux amd64.' }
# The imported development image can serve as the backup helper.
$imageManifest = Join-Path $root '.local/transfer/manifest.json'
if (Test-Path -LiteralPath $imageManifest) {
    $images = Get-Content -LiteralPath $imageManifest -Raw | ConvertFrom-Json
    $helper = ($images.images | Where-Object service -eq 'dev').tag
}
Invoke-BackupDocker image inspect $helper --format '{{.Os}}/{{.Architecture}}'
$manifestPath = Join-Path $backupPath 'oracle-state.json'
if ($Action -in @('encrypt','decrypt')) {
    $transferPath = Join-Path $root '.local/transfer'
    New-Item -ItemType Directory -Force -Path $backupPath,$transferPath | Out-Null
    $keyPath = [IO.Path]::GetFullPath($KeyFile, $root)
    if ($Action -eq 'encrypt') {
        if (-not (Test-Path -LiteralPath $manifestPath)) { throw 'Exporte primero un respaldo consistente.' }
        Invoke-BackupDocker run --rm --network none --user 0:0 --mount "type=bind,src=$backupPath,dst=/private" --mount "type=bind,src=$transferPath,dst=/transfer" --entrypoint bash $helper -c 'set -euo pipefail
if ! test -f /private/restore-key.pem; then
  openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out /private/restore-key.pem 2>/private/key-generation.log
fi
openssl req -new -x509 -key /private/restore-key.pem -out /private/restore-cert.pem -subj /CN=BookingTransfer -days 3650
tar -cf /private/oracle-state.tar -C /private oracle-data.tar.gz oracle-password app-password persistence-token oracle-state.json
openssl cms -encrypt -binary -stream -aes-256-gcm -in /private/oracle-state.tar -out /transfer/oracle-state.cms -outform DER -recip /private/restore-cert.pem -keyopt rsa_padding_mode:oaep -keyopt rsa_oaep_md:sha256
openssl cms -decrypt -binary -inform DER -in /transfer/oracle-state.cms -inkey /private/restore-key.pem -out /private/roundtrip.tar
cmp -s /private/oracle-state.tar /private/roundtrip.tar'
        $cipherPath = Join-Path $transferPath 'oracle-state.cms'
        $hash = (Get-FileHash -LiteralPath $cipherPath -Algorithm SHA256).Hash.ToLower()
        [IO.File]::WriteAllText(($cipherPath + '.sha256'), "$hash  oracle-state.cms`n")
        Write-Output 'Respaldo cifrado y descifrado de prueba: OK. Guarde restore-key.pem aparte; no se sube a GitHub.'
    } else {
        if (-not (Test-Path -LiteralPath $keyPath)) { throw 'Falta la clave privada. Indiquela con -KeyFile; no se recupera de GitHub.' }
        if (Test-Path -LiteralPath $manifestPath) { throw 'El destino ya contiene un respaldo. Use -Directory con una carpeta vacia.' }
        $cipherPath = Join-Path $transferPath 'oracle-state.cms'
        $expected = ((Get-Content -LiteralPath ($cipherPath + '.sha256') -Raw).Trim() -split '\s+')[0]
        if ((Get-FileHash -LiteralPath $cipherPath -Algorithm SHA256).Hash.ToLower() -ne $expected) { throw 'Checksum incorrecto del respaldo cifrado.' }
        Invoke-BackupDocker run --rm --network none --user 0:0 --mount "type=bind,src=$backupPath,dst=/private" --mount "type=bind,src=$transferPath,dst=/transfer,readonly" --mount "type=bind,src=$keyPath,dst=/key.pem,readonly" --entrypoint bash $helper -c 'set -euo pipefail
openssl cms -decrypt -binary -inform DER -in /transfer/oracle-state.cms -inkey /key.pem -out /private/decrypted.tar
tar -xf /private/decrypted.tar -C /private -- oracle-data.tar.gz oracle-password app-password persistence-token oracle-state.json'
        Write-Output 'Respaldo descifrado. Ejecute import para restaurar en un volumen vacio.'
    }
    exit 0
}if ($Action -eq 'import') {
    $manifest = Get-Content -LiteralPath $manifestPath -Raw | ConvertFrom-Json
    foreach ($entry in $manifest.files) {
        if ($entry.file -notin @('oracle-data.tar.gz','oracle-password','app-password','persistence-token')) {
            throw 'Nombre de archivo inesperado en el respaldo.'
        }
        if ((Get-FileHash -LiteralPath (Join-Path $backupPath $entry.file) -Algorithm SHA256).Hash.ToLower() -ne $entry.sha256) {
            throw "Checksum incorrecto: $($entry.file)"
        }
    }
    foreach ($secret in @('oracle-password','app-password')) {
        $target = Join-Path $root ".local/secrets/$secret"
        if ((Test-Path -LiteralPath $target) -and
            (Get-FileHash -LiteralPath $target).Hash -ne (Get-FileHash -LiteralPath (Join-Path $backupPath $secret)).Hash) {
            throw 'Ya existen credenciales diferentes. Use una carpeta/clon nuevo para restaurar.'
        }
    }
    & docker volume inspect $volume *> $null
    if ($LASTEXITCODE -ne 0) {
        Invoke-BackupDocker volume create --label com.docker.compose.project=booking-local --label com.docker.compose.volume=oracle-data $volume
    }
    # Never restore over an existing database, including a running one.
    Invoke-BackupDocker run --rm --network none --user 0:0 --mount "type=volume,src=$volume,dst=/data" --mount "type=bind,src=$backupPath,dst=/backup,readonly" --entrypoint bash $helper -c 'set -euo pipefail; if test -n "$(ls -A /data)"; then echo "El volumen destino no esta vacio. Se cancela sin sobrescribir." >&2; exit 1; fi; tar --numeric-owner -xzpf /backup/oracle-data.tar.gz -C /data'
    New-Item -ItemType Directory -Force -Path '.local/secrets' | Out-Null
    foreach ($secret in @('oracle-password','app-password')) {
        Copy-Item -LiteralPath (Join-Path $backupPath $secret) -Destination (Join-Path $root ".local/secrets/$secret")
    }
    Copy-Item -LiteralPath (Join-Path $backupPath 'persistence-token') -Destination '.local/persistence-token'
    Write-Output 'Datos y credenciales restaurados en volumen vacio. Ejecute prepare y el arranque con compose.images.yaml.'
    exit 0
}
if (Test-Path -LiteralPath $manifestPath) { throw 'Ya existe respaldo terminado; use -Directory con una carpeta nueva.' }
$metadata = (Invoke-BackupDocker volume inspect $volume --format '{{json .Labels}}') | ConvertFrom-Json
if ($metadata.'com.docker.compose.project' -ne 'booking-local') { throw 'El volumen no pertenece a booking-local.' }
foreach ($secret in @('oracle-password','app-password')) {
    if (-not (Test-Path -LiteralPath ".local/secrets/$secret")) { throw "Falta el archivo local $secret." }
}
New-Item -ItemType Directory -Force -Path $backupPath | Out-Null
try {
    Invoke-BackupDocker compose stop oracle
    $oracleId = Invoke-BackupDocker compose ps -a -q oracle
    $running = Invoke-BackupDocker inspect --format '{{.State.Running}}' $oracleId
    if ($running -ne 'false') { throw 'Oracle no se detuvo. Se cancela el respaldo.' }
    Invoke-BackupDocker run --rm --network none --user 0:0 --mount "type=volume,src=$volume,dst=/source,readonly" --mount "type=bind,src=$backupPath,dst=/backup" --entrypoint bash $helper -c 'set -euo pipefail; tar --numeric-owner --sparse -czpf /backup/oracle-data.tar.gz -C /source .; gzip -t /backup/oracle-data.tar.gz; tar -tzf /backup/oracle-data.tar.gz > /backup/oracle-files.txt'
    foreach ($secret in @('oracle-password','app-password')) {
        Copy-Item -LiteralPath ".local/secrets/$secret" -Destination (Join-Path $backupPath $secret)
    }
    Copy-Item -LiteralPath '.local/persistence-token' -Destination (Join-Path $backupPath 'persistence-token')
    $files = foreach ($name in @('oracle-data.tar.gz','oracle-password','app-password','persistence-token')) {
        [ordered]@{
            file = $name
            bytes = (Get-Item -LiteralPath (Join-Path $backupPath $name)).Length
            sha256 = (Get-FileHash -LiteralPath (Join-Path $backupPath $name) -Algorithm SHA256).Hash.ToLower()
        }
    }
    $manifest = [ordered]@{
        createdUtc = [DateTime]::UtcNow.ToString('o')
        database = 'Oracle Free 23.26.3'
        volume = $volume
        consistentShutdown = $true
        confidential = $true
        files = $files
    }
    [IO.File]::WriteAllText($manifestPath, ($manifest | ConvertTo-Json -Depth 5) + "`n")
    Write-Output 'Respaldo privado creado: contiene datos y credenciales. No subir esta carpeta a GitHub.'
} finally {
    Invoke-BackupDocker compose up -d --wait --wait-timeout 600 oracle
}
Invoke-BackupDocker compose exec -T dev bash scripts/jdbc-check.sh read