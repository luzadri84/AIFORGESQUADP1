#requires -Version 7.0
[CmdletBinding()]
param([Parameter(Mandatory)][ValidateSet('schema','seed','status')][string]$Action)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = Split-Path $PSScriptRoot -Parent
Push-Location -LiteralPath $root
try {
    $body = switch ($Action) {
        'schema' { Get-Content -LiteralPath 'backend/src/main/resources/db/oracle/V001__booking_schema.sql' -Raw }
        'seed' { Get-Content -LiteralPath 'backend/src/main/resources/db/oracle/R__development_spaces.sql' -Raw }
        'status' { "SELECT USER, SYS_CONTEXT('USERENV','CON_NAME') AS PDB FROM dual;`nSELECT table_name FROM user_tables ORDER BY table_name;`nSELECT COUNT(*) AS SPACE_COUNT FROM BKG_SPACE;`nSELECT COUNT(*) AS BOOKING_COUNT FROM BKG_BOOKING;" }
    }
    # Password stays inside the container and enters SQL*Plus via stdin, not argv/logs.
    $connect = 'set -euo pipefail; { printf "WHENEVER OSERROR EXIT FAILURE\nWHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK\nSET ECHO OFF VERIFY OFF\nCONNECT BOOKING/\"%s\"@//localhost:1521/FREEPDB1\n" "$(cat /run/secrets/app_password)"; cat; } | sqlplus -s /nolog'
    $sql = "SET PAGESIZE 100 LINESIZE 180`n$body`nCOMMIT;`nEXIT;`n"
    $sql | docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T oracle bash -c $connect
    if ($LASTEXITCODE -ne 0) { throw "Oracle rechazó la acción $Action; revisar la salida antes de reintentar." }
} finally {
    Pop-Location
}
