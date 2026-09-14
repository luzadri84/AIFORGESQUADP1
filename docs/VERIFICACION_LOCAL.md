# Evidencia de verificación local

> Actualización de alcance — DEC-004 (2026-09-14): este documento conserva el
> registro/propuesta de su etapa original. Las instrucciones de esperar, recibir,
> comparar o migrar un starter e investigar su defecto quedaron superadas: el
> repositorio actual es la base definitiva. No aplica por cambio de alcance;
> no se acredita implementación de esas tareas. WebLogic sigue independiente.
> Reglas y estado vigentes: AGENTS.md, tasks.md y docs/BITACORA.md.

Fecha: 2026-09-14. Equipo Windows del workspace; comandos de aplicación de
herramientas ejecutados en el servicio Linux dev mediante Docker Compose.
No se inspeccionó un starter: sigue sin estar disponible.

## Resultado

| Comprobación | Resultado real |
|---|---|
| Docker Desktop, daemon Linux y WSL2 | Reutilizados y operativos |
| Compose config --quiet | Correcto, sin expandir/imprimir credenciales |
| Docker Compose up con --wait | Oracle healthy y dev ejecutándose |
| Dev Containers CLI 0.89.0: read-configuration y up | outcome=success, usuario node, /workspace, proyecto booking-local |
| postCreateCommand de Dev Containers | Terminó correctamente: JDBC, npm ci y ngc |
| Java | Temurin 21.0.10+7, Linux amd64 |
| Node / npm / Git del contenedor | 22.22.0 / 10.9.4 / 2.39.5 |
| Maven Wrapper / Maven | 3.3.4 / 3.9.9, checksum de distribución verificado |
| Resolución del BOM y dependency:tree | BUILD SUCCESS |
| Oracle mediante JDBC | Oracle AI Database 26ai Free, versión 23.26.3.0.0 |
| Usuario / PDB | BOOKING / FREEPDB1, no SYS/SYSTEM |
| Driver JDBC | 21.9.0.0.0, conexión y SQL ejecutados realmente |
| Instalación npm final | npm ci: 415 paquetes añadidos, 416 auditados |
| Auditoría npm final | 0 vulnerabilidades reportadas en la sonda frontend |
| Compilación Angular | ngc con strict, strictTemplates y skipLibCheck=false: correcta |
| Persistencia después de restart oracle | Marcador técnico conservado y leído por JDBC |
| Persistencia después de stop / up | Marcador técnico conservado y leído por JDBC |
| Sintaxis de scripts Bash | bash -n correcto |
| Git dentro del contenedor | Operativo tras confiar específicamente en /workspace |
| Secretos y artefactos | git check-ignore confirma exclusiones; .env y .local no versionados |
| Puertos publicados | 127.0.0.1:4200 y 127.0.0.1:8080; Oracle sin puerto al host |

La CLI oficial de Dev Containers se ejecutó desde la extensión de VS Code ya
instalada, sin instalar otra. Se verificó ese flujo mediante CLI; no se automatizó
la apertura visual de la ventana de VS Code.

## Dependencias Java resueltas

| Dependencia | Versión |
|---|---|
| Spring Boot | 3.3.13 |
| Spring Framework core | 6.1.21 |
| Spring Security core | 6.3.10 |
| Spring Data JPA | 3.3.13 |
| Hibernate core | 6.5.3.Final |
| SpringDoc webmvc-ui | 2.6.0 |
| ojdbc11 | 21.9.0.0 |

El árbol completo se puede regenerar dentro de dev:

```bash
bash mvnw -B -ntp -f infra/checks/pom.xml dependency:tree
```

## Evidencia JDBC observada

```text
JDBC: 21.9.0.0.0
Database: Oracle AI Database 26ai Free Release 23.26.3.0.0 - Develop, Learn, and Run for Free
Version 23.26.3.0.0
Schema=BOOKING, PDB=FREEPDB1, JDBC connection OK
Persistence after restart: OK
```

Ese resultado de persistencia también se obtuvo después de usar las acciones
stop y up del script entregado. No se eliminó el volumen.

## Correcciones y límites

- Las candidatas Angular 20.3.0 / CLI 20.3.0 dieron 23 avisos de vulnerabilidades.
  Se verificaron y fijaron core/compiler 20.3.31 y CLI 20.3.37. La primera
  actualización produjo ERESOLVE por referencias del árbol anterior; se resolvió
  el manifiesto en una carpeta limpia sin forzar peers. npm ci y ngc pasaron
  después. El reporte cero se limita al snapshot npm, no al backend, SO o imágenes.
- npm avisa que @angular/animations está deprecado. Se conserva porque PrimeNG 20
  lo declara como peer; no es un error de compilación.
- Git detectó dubious ownership del bind mount Windows. Se añadió confianza solo
  para /workspace en la configuración del usuario del contenedor. El script
  reproduce esa configuración y valida git status.
- Una consulta Maven desde PowerShell dividió el argumento -DoutputFile y falló
  con Unknown lifecycle phase ".txt". Ejecutada dentro de bash con el argumento
  completo, produjo BUILD SUCCESS; no se cambió el POM por ese error de shell.
- El alert log inicial de Oracle incluyó "Unable to obtain current patch
  information due to error: 20013". La base abrió FREEPDB1 read/write, pasó su
  healthcheck y JDBC/persistencia. No se acredita una inspección de inventario de
  parches ni capacidades de edición empresarial.
- Consumo observado en reposo: Oracle ~1,97 GiB de su límite de 3 GiB; dev ~274 MiB
  de su límite de 2 GiB. Es una medición puntual, no una garantía de consumo de la
  futura aplicación.

## Pendientes expresos

No hay URL funcional de Booking comprobada. 4200 y 8080 están reservados y
publicados solo en loopback, pero no existe una aplicación escuchando en ellos.

No se ejecutaron pruebas de negocio, del starter, seguridad Basic/CSRF, compilación
del frontend real ni generación/arranque de un WAR: falta el código. No se
sustituyeron por tests omitidos o una demo inventada. El POM de sondas usa
packaging=pom; el backend del starter deberá conservar packaging=war.

Falta obtener el starter y su historial, inspeccionar su defecto y adaptar las
plantillas locales a su configuración real. Sigue pendiente resolver la
incompatibilidad Boot 3 / WebLogic 12.2.1.4 documentada en ENTORNO_LOCAL.md.
No se instaló WebLogic, se desplegó públicamente ni se envió el proyecto.