#requires -Version 7.0
[CmdletBinding()]
param([ValidateSet('start','verify','stop','status')][string]$Action = 'status')
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = Split-Path $PSScriptRoot -Parent
$ComposeFiles = @('-f','compose.yaml','-f','.local/transfer/compose.images.yaml')
function Compose([string[]]$Arguments) {
    & docker compose @ComposeFiles @Arguments
    if ($LASTEXITCODE -ne 0) { throw 'Docker Compose no pudo completar la operación. Revise la salida anterior.' }
}
function Run-Bash([string]$Command) { Compose @('exec','-T','dev','bash','-c',$Command) }
function Ensure-Credentials {
    $runtime = Join-Path $root '.local/runtime'
    New-Item -ItemType Directory -Force -Path $runtime | Out-Null
    $file = Join-Path $runtime 'booking.properties'
    if (-not (Test-Path -LiteralPath $file)) {
        $one = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(24))
        $two = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(24))
        $content = "booking.auth.first.username=ana`nbooking.auth.first.password=$one`nbooking.auth.second.username=bruno`nbooking.auth.second.password=$two`n"
        $stream = [IO.File]::Open($file,[IO.FileMode]::CreateNew,[IO.FileAccess]::Write)
        try { $bytes=[Text.Encoding]::UTF8.GetBytes($content); $stream.Write($bytes,0,$bytes.Length) } finally { $stream.Dispose() }
        Write-Host 'Credenciales nuevas guardadas solo en .local/runtime/booking.properties; no se modificó Oracle.'
    }
}
function Ensure-Dependencies {
    if (-not (Test-Path -LiteralPath 'frontend/node_modules/@angular/build/package.json')) {
        Run-Bash 'cd frontend && npm ci --no-audit --no-fund'
    }
}
function Wait-Url([string]$Url) {
    $deadline = [DateTime]::UtcNow.AddSeconds(120)
    do {
        try { $response=Invoke-WebRequest -Uri $Url -TimeoutSec 10; if ($response.StatusCode -eq 200) { return } } catch { }
        Start-Sleep -Seconds 1
    } while ([DateTime]::UtcNow -lt $deadline)
    throw "No respondió $Url. Revise .local/runtime/backend.log y frontend.log."
}
function Start-Application {
    Compose @('exec','-T','dev','bash','scripts/app-process.sh','backend')
    Wait-Url 'http://localhost:8080/api/csrf'
    Compose @('exec','-T','dev','bash','scripts/app-process.sh','frontend')
    Wait-Url 'http://localhost:4200/'
    Write-Host 'Booking: http://localhost:4200/ | Usuarios: consultar .local/runtime/booking.properties (sin imprimir claves).'
}
Push-Location -LiteralPath $root
try {
    if ($Action -in @('start','verify')) {
        Compose @('up','-d','--no-build','--wait','--wait-timeout','600')
        Ensure-Credentials
        Ensure-Dependencies
    }
    switch ($Action) {
        'start' {
            if (-not (Test-Path -LiteralPath 'backend/target/booking.war')) { Run-Bash 'bash mvnw -B -ntp -f backend/pom.xml -DskipTests package' }
            Start-Application
        }
        'verify' {
            Compose @('exec','-T','dev','bash','scripts/app-process.sh','stop')
            Run-Bash 'bash mvnw -B -ntp -f backend/pom.xml verify'
            Run-Bash 'cd frontend && npm run check && npm run build && npm test'
            Run-Bash 'bash scripts/jdbc-check.sh read'
            foreach ($ignored in @('.local/restore-key.pem','.local/runtime/booking.properties','.local/secrets/app-password','.env')) {
                & git check-ignore -q -- $ignored
                if ($LASTEXITCODE -ne 0) { throw "Secreto no excluido: $ignored" }
            }
            Start-Application
        }
        'stop' {
            $running = @(Compose @('ps','--status','running','--services'))
            if ($running -contains 'dev') { Compose @('exec','-T','dev','bash','scripts/app-process.sh','stop') }
            Compose @('stop')
            Write-Host 'Entorno detenido; volúmenes y datos conservados.'
        }
        'status' {
            Compose @('ps')
            $running = @(Compose @('ps','--status','running','--services'))
            if ($running -contains 'dev') { Compose @('exec','-T','dev','bash','scripts/app-process.sh','status') }
        }
    }
} finally { Pop-Location }
