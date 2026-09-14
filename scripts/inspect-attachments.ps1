$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$mail = Get-ChildItem -LiteralPath $root -Filter *.eml | Select-Object -First 1
$raw = [IO.File]::ReadAllText($mail.FullName)
$parts = [regex]::Matches($raw, '(?ms)^Content-Type: (?<headers>.*?)\r?\n\r?\n(?<body>.*?)(?=\r?\n--)')
foreach ($part in $parts) {
    $headers = $part.Groups['headers'].Value
    if ($headers -notmatch 'Content-Transfer-Encoding: base64') { continue }
    if ($headers -match '^text/plain;|name="AGENTS.md"') {
        $decoded = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String(($part.Groups['body'].Value -replace '\s','')))
        if ($headers -match 'name="AGENTS.md"') {
            Write-Output '--- Especificacion original adjunta ---'
            Write-Output $decoded
        } else {
            Write-Output '--- Cuerpo del correo (sin cabeceras) ---'
            Write-Output $decoded
        }
    }
    if ($headers -match 'Historia_Usuario_Booking_Espacios.docx') {
        $bytes = [Convert]::FromBase64String(($part.Groups['body'].Value -replace '\s',''))
        $stream = [IO.MemoryStream]::new($bytes)
        $zip = [IO.Compression.ZipArchive]::new($stream)
        $reader = [IO.StreamReader]::new($zip.GetEntry('word/document.xml').Open())
        [xml]$xml = $reader.ReadToEnd()
        Write-Output '--- Historia de usuario adjunta ---'
        $xml.SelectNodes('//*[local-name()="p"]') | ForEach-Object {
            ($_.SelectNodes('.//*[local-name()="t"]') | ForEach-Object InnerText) -join ''
        }
        $reader.Dispose(); $zip.Dispose(); $stream.Dispose()
    }
}
