# Comparadores locales de avisos y evidencia de seguridad

Se conserva este documento para ejecutar las herramientas junto a las que está ubicado.
Estado actual: [tratamiento T018](../../docs/TRATAMIENTO_DEPENDENCIAS_T018.md) y
[verificación](../../docs/VERIFICACION.md). La auditoría T017 sobre 47a6af4 tuvo seis
regresiones; las fuentes e859c80 pasan 88 casos Java. Es historia real, no una instrucción
vigente de esperar fallos. Los anexos anteriores están en Git e170418/eee557b;
los actuales en docs/correction-evidence. No sobrescribir evidencia histórica al repetir.

## Verificar aplicación

Desde raíz, con Oracle ya preparado: `node scripts/environment.mjs verify` en el host.
Conserva volúmenes/secretos, reconstruye y prueba WAR/Angular, lee JDBC y deja la app
iniciada si pasa. No ejecutar verificaciones concurrentes contra el mismo esquema.
Para investigar una prueba dirigida, usar Docker exec dentro de dev y Maven Wrapper;
registrar comando/resultado/revisión, sin atribuir al total de la suite una ejecución parcial.

## Repetir comparación de catálogos (opcional, no arranque normal)

Además de Node/Docker, estos scripts de auditoría requieren **Python 3.11 o superior en el host**.
Desde raíz y con dev iniciado; comandos comunes en PowerShell y shell Linux:

```text
python -c "from pathlib import Path; Path('.local/audit').mkdir(parents=True, exist_ok=True)"
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash -c "bash mvnw -B -ntp -f backend/pom.xml dependency:tree -DoutputType=text -DoutputFile=/workspace/.local/audit/maven-tree.txt"
python scripts/audit/prepare_advisories.py --download
python -m unittest discover -s scripts/audit -p test_range_match.py
python scripts/audit/match_advisories.py
```

Si Python se llama python3 en Linux, sustituir el ejecutable; no instalarlo para operar
Booking. Si ambos ZIP ya existen en .local/audit, omitir --download para reutilizarlos.
prepare descarga catálogos públicos completos Maven/npm y filtra localmente contra
el árbol Maven y lock npm. No envía coordenadas privadas a servicios de consulta.
match usa ComparableVersion de Maven 3.9.9 y semver del frontend dentro del contenedor;
requiere haber descargado Wrapper/npm con el procedimiento normal. Evalúa rangos,
fronteras y versiones explícitas; no compara versiones lexicográficamente.

Los tres tests del comparador no demuestran exhaustividad del catálogo, explotación ni
alcanzabilidad. No se afirma soporte de rangos Git. Registrar hash/fecha de catálogos y
versiones; nuevas fotos pueden cambiar. Salida en .local/audit/dependency-findings.json,
no en los anexos históricos. Maven produjo texto en la ejecución original pese a pedir
JSON: por eso se solicita text. No interpretar ese árbol como JSON.

## Otros controles

Revisar secretos en memoria, nunca mostrar valores ni pasarlos como argumentos.
Escanear archivos e historia contra secretos conocidos tiene límites y no certifica
secretos desconocidos. SQL*Plus usa contraseña dentro del contenedor por stdin;
`scripts/booking-db.ps1` muestra el patrón. No publicar inspecciones crudas de Docker,
logs, cookies, headers de autorización ni conexiones Oracle.

Las pruebas manuales usan dos identidades y fixtures propios, con IDs registrados.
Crear/listar, conflicto/adyacencia, serie parcial y cancelar exclusivamente esos fixtures;
verificar SQL sin modificar filas preexistentes. No reemplazar el marcador de transferencia
para demostrar persistencia. WebLogic, Swagger interactivo y T018 mantienen los límites
descritos en los documentos vigentes, aunque los tests automatizados pasen.
