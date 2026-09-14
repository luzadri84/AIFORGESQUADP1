param(
    [ValidateSet('diagnose','prepare','up','verify','stop','status')]
    [string]$Action = 'diagnose'
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $root
function Invoke-Docker {
    & docker @args
    if ($LASTEXITCODE -ne 0) { throw "Docker fallo (codigo $LASTEXITCODE). Revise Docker Desktop y el mensaje anterior." }
}
function Compose { Invoke-Docker compose --project-directory $root -f (Join-Path $root 'compose.yaml') @args }
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw 'Falta Docker Desktop con Compose y motor Linux.' }
Invoke-Docker info --format '{{.OSType}}/{{.Architecture}}'
switch ($Action) {
    'diagnose' {
        Get-CimInstance Win32_OperatingSystem | Select-Object Caption,Version,FreePhysicalMemory,TotalVisibleMemorySize | Format-List
        Get-CimInstance Win32_Processor | Select-Object Name,NumberOfCores,VirtualizationFirmwareEnabled | Format-List
        Get-PSDrive -PSProvider FileSystem | Select-Object Name,Used,Free | Format-Table
        Invoke-Docker version
        Invoke-Docker compose version
        Invoke-Docker info --format 'Docker CPUs={{.NCPU}} RAM={{.MemTotal}}'
        Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object LocalPort -in 1521,4200,8080 | Format-Table LocalAddress,LocalPort,OwningProcess
    }
    'prepare' {
        New-Item -ItemType Directory -Force -Path '.local/secrets' | Out-Null
        foreach ($name in @('oracle-password','app-password')) {
            $path = Join-Path $root ".local/secrets/$name"
            if (-not (Test-Path -LiteralPath $path)) {
                $password = 'Bkg9' + [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(12))
                [IO.File]::WriteAllText($path, $password)
            }
        }
        if (-not (Test-Path '.env')) { Copy-Item -LiteralPath '.env.example' -Destination '.env' }
        Compose config --quiet
        Write-Output 'Configuracion valida; secretos locales conservados/creados sin imprimir valores.'
    }
    'up' {
        if (-not (Test-Path '.local/secrets/app-password')) { throw 'Ejecute primero: pwsh -File scripts/local.ps1 prepare' }
        $freeGiB = (Get-CimInstance Win32_OperatingSystem).FreePhysicalMemory / 1MB
        Write-Output ('RAM libre de Windows antes del arranque: {0:N2} GiB' -f $freeGiB)
        if ($freeGiB -lt 1) { Write-Warning 'Memoria disponible baja. Cierre aplicaciones pesadas si el arranque no progresa; no se cierran procesos ajenos.' }
        Compose config --quiet
        Compose up -d --build --wait --wait-timeout 600
    }
    'verify' {
        Compose exec -T dev bash scripts/verify-local.sh
        Compose exec -T dev bash scripts/jdbc-check.sh write
        Compose restart oracle
        Compose up -d --wait --wait-timeout 600 oracle
        Compose exec -T dev bash scripts/jdbc-check.sh read
        Compose ps
    }
    'stop' { Compose stop }
    'status' { Compose ps }
}