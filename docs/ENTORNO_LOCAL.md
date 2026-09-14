# Entorno local provisional de Booking

> Actualización de alcance — DEC-004 (2026-09-14): este documento conserva el
> registro/propuesta de su etapa original. Las instrucciones de esperar, recibir,
> comparar o migrar un starter e investigar su defecto quedaron superadas: el
> repositorio actual es la base definitiva. No aplica por cambio de alcance;
> no se acredita implementación de esas tareas. WebLogic sigue independiente.
> Reglas y estado vigentes: AGENTS.md, tasks.md y docs/BITACORA.md.

Estado inicial: carpeta de documentos, sin starter, código de aplicación ni historial Git.
El correo recibido contiene literalmente [ENLACE AL REPOSITORIO STARTER].
Esta preparación implementa infraestructura y sondas técnicas; no implementa reservas.

## Uso en Windows

Requisitos: Docker Desktop con motor Linux y Compose, PowerShell 7, VS Code con
Dev Containers. Se reutilizaron las instalaciones existentes. No es necesario
instalar Java, Maven o Angular globalmente ni otra distribución WSL.

Desde la raíz del proyecto:

```powershell
pwsh -NoProfile -File scripts/local.ps1 diagnose
pwsh -NoProfile -File scripts/local.ps1 prepare
pwsh -NoProfile -File scripts/local.ps1 up
pwsh -NoProfile -File scripts/local.ps1 verify
pwsh -NoProfile -File scripts/local.ps1 status
pwsh -NoProfile -File scripts/local.ps1 stop
```

Después de prepare, abra la carpeta en VS Code y ejecute **Dev Containers: Reopen
in Container**. La configuración usa el mismo servicio dev de Compose, usuario
node y carpeta /workspace. El postCreateCommand ejecuta las sondas técnicas.
La sesión actual de Codex está en Windows; sus pruebas se ejecutan explícitamente
mediante docker compose exec, dentro de dev. Abrir archivos no cambia ese contexto.

prepare es repetible y no cambia contraseñas existentes. up espera el healthcheck
real de Oracle. verify resuelve dependencias, comprueba JDBC, ejecuta npm ci y el
compilador Angular con strict/strictTemplates y comprueba persistencia mediante una
fila técnica antes/después de reiniciar Oracle. stop conserva contenedores y volúmenes.
No hay scripts que borren el volumen.

## Red y credenciales

| Origen | Destino | Estado |
|---|---|---|
| dev | oracle:1521/FREEPDB1 | JDBC, usuario BOOKING |
| Windows | 127.0.0.1:8080 | Puerto reservado para backend futuro |
| Windows | 127.0.0.1:4200 | Puerto reservado para Angular futuro |
| Windows | Oracle | Sin puerto publicado |
| Angular futuro en dev | 127.0.0.1:8080/api | Proxy de ejemplo; mismo contenedor |

Los puertos del host se limitan a 127.0.0.1. Si están ocupados, cambie
LOCAL_HTTP_PORT / LOCAL_ANGULAR_PORT en .env. Los puertos internos permanecen iguales.
No hay todavía una URL de Booking funcionando.

Los archivos .local/secrets/oracle-password y app-password son secretos locales
generados criptográficamente e ignorados por Git. Compose los monta como archivos
en /run/secrets. El usuario administrativo solo inicializa Oracle; la sonda JDBC
usa BOOKING en FREEPDB1. La imagen crea ese schema en el primer arranque y conserva
los datos en booking-local_oracle-data. No modifique los archivos de contraseñas de
una base ya inicializada esperando que eso cambie la contraseña dentro de Oracle.

No imprima secretos ni incluya .local, .env o el correo .eml en una entrega.
El contexto de build solo permite el Dockerfile; las credenciales no entran en la imagen.
El montaje de desarrollo permite acceso al workspace local, como en un devcontainer
habitual; no es una imagen para publicar ni se debe compartir con terceros.

## Versiones y reproducción

- Temurin JDK 21.0.10+7 y Node 22.22.0 en imágenes fijadas por digest.
- npm 10.9.4 y Git 2.39.5, incluidos en la imagen Node fijada por digest.
- Maven 3.9.9, Wrapper oficial 3.3.4 de tipo only-script, distribución fijada por SHA-256.
- Oracle Free: gvenzl/oracle-free:23.26.3-slim, índice por digest; soporte amd64/arm64
  verificado en el registro. Es una imagen comunitaria documentada por su mantenedor;
  la edición Oracle Free no acredita una edición empresarial.
- BOM Spring Boot 3.3.13 sin sobrescribir versiones transitivas, SpringDoc 2.6.0.
  El BOM resuelve ojdbc11 21.9.0.0.
- Dependencias de la sonda Angular: core/compiler 20.3.31 y CLI 20.3.37, CDK 20.2.0,
  PrimeNG 20.0.0, Tailwind 3.4.17, RxJS 7.8.2, TypeScript 5.9.2,
  MSAL Angular 3.1.0 y Browser 3.28.1.

infra/checks es una sonda aislada: su POM tiene packaging=pom, no genera un WAR vacío.
Su componente Angular solo se compila, no se sirve ni simula una pantalla de reservas.
npm ci usa lockfile y --ignore-scripts; se invoca ngc explícitamente.
No se usa --force, --legacy-peer-deps ni se relaja strict o skipLibCheck.

La tabla ENVIRONMENT_PROBE contiene solo un marcador técnico de persistencia.
Cada verificación actualiza una única fila y comprueba que sobrevive al reinicio.
No crea tablas de negocio.

## Incorporación del starter

Conserve el historial del starter real y compare estas configuraciones antes de
trasladarlas. No reemplace su estructura ni su seguridad automáticamente.
Las plantillas de infra/templates no están activadas: el datasource requiere
JDBC_URL y JDBC_USER; carga la contraseña app_password mediante configtree desde
/run/secrets/. Esa plantilla aún no pertenece a un proceso Spring real. Conserve Basic y CSRF.

El backend real debe usar packaging=war, SpringBootServletInitializer y Tomcat
provided. Generación y arranque del WAR, pruebas del starter, build de su frontend,
flujo de reservas y defecto sembrado quedan pendientes de recibir ese código.

Spring Boot 3 requiere Servlet 5+; WebLogic 12.2.1.4 es Java EE 7 y ese runtime
es incompatible. Un WAR local o una comprobación opcional en Tomcat 10.1 no
acredita WebLogic. No se instaló WebLogic ni se modificó el stack para ocultarlo.

MSAL Angular 3 documenta soporte para Angular 15–18; la instalación y compilación
de esta sonda no certifican su integración con Angular 20 ni un tenant Azure.
Las versiones se eligieron por ajuste a la spec. La resolución de dependencias
no equivale a una auditoría de vulnerabilidades ni a soporte vigente.

## Fuentes contrastadas

- [Imagen Oracle: tags, secrets, usuario e inicialización](https://github.com/gvenzl/oci-oracle-free).
- [Compatibilidad de Angular](https://angular.dev/reference/versions).
- [Requisitos de Spring Boot 3.3](https://docs.spring.io/spring-boot/3.3/system-requirements.html).
- [Compatibilidad de WebLogic 12.2.1.4](https://docs.oracle.com/en/middleware/fusion-middleware/weblogic-server/12.2.1.4/intro/compatibility.html).
- [MSAL Angular 3](https://learn.microsoft.com/en-us/entra/msal/javascript/angular/v2-v3-upgrade-guide).
- [BOM exacto](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/3.3.13/spring-boot-dependencies-3.3.13.pom).
- Metadata de registry.npmjs.org consultada con npm view para versiones, engines y peers.