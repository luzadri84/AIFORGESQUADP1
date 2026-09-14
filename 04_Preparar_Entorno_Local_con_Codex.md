# Preparar el entorno local de Booking con Codex

> Actualización de alcance — DEC-004 (2026-09-14): este documento conserva el
> registro/propuesta de su etapa original. Las instrucciones de esperar, recibir,
> comparar o migrar un starter e investigar su defecto quedaron superadas: el
> repositorio actual es la base definitiva. No aplica por cambio de alcance;
> no se acredita implementación de esas tareas. WebLogic sigue independiente.
> Reglas y estado vigentes: AGENTS.md, tasks.md y docs/BITACORA.md.

Este documento complementa el plan técnico. Contiene una propuesta de entorno y un prompt listo para ejecutar en Codex con acceso al equipo del candidato. No se ha instalado nada en ese equipo desde esta conversación.

## 1 Desarrollo local y cumplimiento de la prueba

Desarrollar en local es compatible con la prueba. El correo pide código, historial, documentación e instrucciones de ejecución vía devcontainer; no pide expresamente una URL pública. AGENTS.md sí exige Oracle 19c o superior, WAR y un servidor objetivo WebLogic 12.2.1.4. El desarrollo local permite verificar funcionalidad y preparar el artefacto, pero no acredita un despliegue en ese servidor.

No sustituir Oracle por H2 o PostgreSQL en la entrega, ni Java/Angular por otro stack para facilitar el arranque. Usar el devcontainer del starter cuando esté disponible y conservar sus decisiones que cumplan. Una solución que solo funciona por instalaciones manuales no documentadas incumpliría la expectativa de reproducción.

Se mantiene la incompatibilidad de runtime ya identificada: Boot 3.3 requiere Servlet 5 o superior y el WebLogic especificado pertenece a Java EE 7. Generar WAR no la corrige. Se puede desarrollar con el runtime local de Spring Boot y proponer una comprobación del WAR en Tomcat 10.1, dejando claro que esa comprobación no sustituye WebLogic. Fuentes: [Spring Boot](https://docs.spring.io/spring-boot/3.3/system-requirements.html) y [Oracle WebLogic](https://docs.oracle.com/en/middleware/fusion-middleware/weblogic-server/12.2.1.4/intro/compatibility.html).

## 2 Entorno recomendado

Para Windows 11, se propone Docker Desktop con backend WSL2, una distribución Ubuntu compatible, VS Code y la extensión Dev Containers. Si alguna pieza ya existe y funciona, reutilizarla. Docker documenta su [integración con WSL2](https://docs.docker.com/desktop/features/wsl/). Un [devcontainer](https://containers.dev/overview) permite definir y compartir el entorno de desarrollo junto con el código.

| Ubicación | Componentes | Propósito |
|---|---|---|
| Equipo Windows | WSL2, Docker Desktop, VS Code, navegador y cliente Codex local | Ejecutar y editar el proyecto |
| Devcontainer del proyecto | JDK 21, Node compatible con Angular 20, npm, Git y utilidades básicas | Compilar y ejecutar backend/frontend sin cambiar instalaciones globales |
| Repositorio | Maven Wrapper, package.json, lockfile, configuración devcontainer y Compose | Fijar y reproducir dependencias |
| Contenedor Oracle | Versión 19c o superior, volumen persistente y usuario de aplicación | Base real para desarrollo e integración |
| Perfil opcional de verificación | Tomcat 10.1 compatible con Java 21 | Comprobar el WAR localmente, sin presentarlo como WebLogic |

Angular, PrimeNG, CDK, Tailwind, RxJS y MSAL son dependencias del proyecto, no aplicaciones que deban instalarse globalmente en Windows. Java y Node pueden vivir exclusivamente dentro del devcontainer. Maven Wrapper evita exigir una instalación global de Maven. No instalar otro daemon Docker dentro de Ubuntu si Docker Desktop ya ofrece la integración necesaria.

Oracle puede ejecutarse mediante una edición Free de versión superior a 19c si el starter no fija una edición diferente y la versión exacta es compatible con el driver, Hibernate y la máquina. Verificar una imagen publicada y su documentación antes de fijar tag o digest; no inventar nombres de imagen. Si el evaluador proporciona Oracle, utilizar su servicio puede ser más sencillo. Una edición de desarrollo permite probar este alcance, pero no acredita todas las características de una edición empresarial. Los [requisitos publicados de Oracle Free](https://docs.oracle.com/en/database/oracle/oracle-database/26/xeinl/requirements.html) corresponden a esa edición y no al consumo del stack completo.

Como presupuesto inicial propuesto para todo el entorno: 8–12 GB de RAM disponibles para Docker/WSL y 25–40 GB de espacio libre para imágenes, datos y caches. No son mínimos oficiales ni mediciones del equipo; Codex debe revisar recursos reales y la imagen elegida. No hace falta GPU para Booking. El código corre localmente, pero Codex sigue necesitando conexión a su servicio y las descargas iniciales requieren Internet.

## 3 Red local y secretos

La pantalla puede abrirse en http://localhost:4200, con proxy `/api` al backend del entorno. El backend suele usar 8080. Son puertos propuestos: comprobar que estén libres antes de usarlos. Si el backend y Angular comparten devcontainer, el proxy puede dirigirse a 127.0.0.1:8080 dentro de ese contenedor; Oracle se alcanza por el nombre del servicio Compose y su puerto interno 1521.

No usar localhost para acceder a otro contenedor. Exponer Oracle al host solo si hace falta un cliente SQL externo y, en ese caso, limitar el puerto publicado a 127.0.0.1. Diferenciar escuchar en 0.0.0.0 dentro de un contenedor para permitir forwarding de publicar puertos en todas las interfaces del host.

Guardar contraseñas locales en archivos ignorados o variables de entorno; `.env.example` solo contiene nombres y valores vacíos o marcadores. La aplicación se conecta con un usuario propio, no como SYS o SYSTEM. No imprimir `docker compose config` completo si expande credenciales ni copiar tokens de Codex al repositorio o la imagen.

## 4 Pasar de desarrollo a comprobación del WAR

Se conserva `packaging=war` desde el inicio. Durante el desarrollo se puede usar `./mvnw spring-boot:run` con el perfil local. Después se ejecuta el ciclo de build/pruebas y se comprueba el WAR en un contenedor Servlet compatible. Spring documenta WAR, SpringBootServletInitializer y dependencia del contenedor con scope provided en su [guía de despliegue tradicional](https://docs.spring.io/spring-boot/3.3/how-to/deployment/traditional-deployment.html).

El artefacto, las reglas de negocio y los tests se mantienen. Al pasar a otro entorno cambian configuración de base de datos, credenciales, puertos, hostname, contexto de aplicación y configuración de servidor. Externalizar esas opciones; usar rutas relativas `/api` en Angular y comprobar el contexto del WAR. La compatibilidad del servidor destino se resuelve expresamente antes de prometer un despliegue allí.

## 5 Dónde ejecutar Codex

Abrir una sesión local de Codex con acceso a la carpeta del proyecto y capacidad de ejecutar comandos en el entorno seleccionado. Si se usa Codex CLI, puede ejecutarse desde el terminal adecuado; si se usa la integración del editor, comprobar que sus comandos corren en el devcontainer o configurar el acceso explícito a él. Estar leyendo archivos del contenedor no implica automáticamente que todos los comandos se ejecuten dentro.

Codex admite trabajo en Windows y WSL. WSL es una elección adecuada para este flujo basado en herramientas Linux y contenedores, no una condición universal de Codex. Consultar [Codex en WSL](https://learn.chatgpt.com/docs/windows/wsl). Una sesión remota de Codex o este chat no tiene por defecto acceso administrativo a tu Windows.

Activar virtualización, instalar componentes Windows, aceptar UAC, iniciar sesión o reiniciar puede requerir intervención local. Codex debe completar lo que pueda y señalar el paso exacto que falta; no prometer que un prompt elimina los permisos del sistema ni solicitar desactivar las protecciones del agente.

## 6 Prompt completo para copiar en Codex

```text
Prepara y verifica un entorno LOCAL reproducible para la prueba técnica
Booking de Espacios Físicos de OTIC SED. Esta fase es infraestructura y
arranque; no implementes todavía funcionalidades de negocio nuevas.

CONTEXTO Y OBJETIVO
- Lee AGENTS.md, los adjuntos y el plan técnico disponibles antes de cambiar
  archivos. Conserva los requisitos y las decisiones válidas del starter.
- Quiero desarrollar en local, ejecutar pruebas con Oracle real y generar WAR
  desde el comienzo. No quiero un despliegue público en esta fase.
- Usa el starter existente. Si no está, prepara solo una base de entorno
  identificada como provisional; no inventes su defecto ni simules inspección.

PASO 1 DIAGNÓSTICO
- Identifica dónde estás ejecutándote: Windows, WSL, contenedor o equipo remoto.
  Si no tienes acceso al equipo objetivo, dilo y prepara archivos/instrucciones;
  no afirmes que instalaste herramientas en un host al que no puedes acceder.
- Inspecciona sistema, CPU, RAM, disco, WSL si aplica, Docker y Compose,
  herramientas disponibles, puertos, Git y configuración del proyecto.
- Comprueba comunicación entre editor, entorno y daemon Docker. Reutiliza lo
  compatible. No elimines contenedores, volúmenes o instalaciones ajenas.

PASO 2 ENTORNO REPRODUCIBLE
- Prefiere devcontainer más Docker Compose. Aprovecha el starter existente.
- Prepara JDK 21, Node compatible con Angular 20, npm, Git y Maven Wrapper.
  Fija versiones exactas verificadas, no latest. Instala herramientas del
  proyecto dentro del devcontainer, evitando cambios globales innecesarios.
- Mantén Angular 20 standalone, strict y strictTemplates, PrimeNG 20,
  Tailwind 3.4, RxJS 7.8, CDK 20 y MSAL Angular 3 según la spec.
- Mantén Boot 3 permitido y BOM coherente, jakarta, JPA, Security, Resource
  Server, SpringDoc compatible, driver Oracle y packaging WAR.
- Verifica existencia, engines y peers antes de instalar. No uses --force,
  --legacy-peer-deps ni desactives strict para silenciar problemas.
- Si no hay servicio Oracle suministrado, verifica una imagen Oracle 19c o
  superior, compatible con la CPU, y fija tag/digest real. Prefiere una edición
  de desarrollo apropiada y documenta versión/edición. No sustituyas por H2.
- Configura healthcheck de Oracle y espera real de disponibilidad; no uses un
  sleep fijo como garantía. Crea usuario/schema de aplicación, inicialización
  reproducible y volumen persistente. No conectes la aplicación como SYS/SYSTEM.

PASO 3 CONFIGURACIÓN LOCAL
- Separa configuración del entorno y lógica. Usa perfil local, datasource
  configurable, API relativa /api y proxy Angular al backend correcto.
- Documenta rutas y puertos desde host y desde contenedores. Limita los puertos
  publicados al host local; no abras acceso LAN/Internet sin necesidad.
- Mantén CSRF y autenticación Basic cuando ya existan en el starter. No
  desactives seguridad para lograr una comprobación de arranque.
- Crea .env.example sin credenciales y configura archivos locales ignorados
  para secretos. Genera valores de desarrollo solo en esos archivos si hace
  falta. No los registres, imprimas ni incluyas en git o imágenes.
- Mantén los datos al detener y volver a iniciar. No ejecutes down -v ni un
  borrado de base de datos como parte de bootstrap, test o parada normal.
- Si requiere intervención por UAC, virtualización, reinicio, login o permiso
  del sistema, explica el paso exacto y continúa con tareas independientes.
  No eludas restricciones ni reinstales herramientas que ya funcionan.

PASO 4 EVIDENCIA
- Ejecuta comandos dentro del entorno que se entregará. Comprueba Java/Node,
  instalación reproducible, conexión JDBC y versión real de Oracle, arranque
  del código disponible, compilación strict y generación del WAR si ya hay app.
- Ejecuta las pruebas existentes que correspondan y revisa sus informes.
  No conviertas fallos en skips ni uses mocks para acreditar Oracle.
- Comprueba persistencia al reiniciar el servicio sin borrar el volumen.
- Si faltan funciones del starter, márcalas pendientes; no simules una demo
  de reservas ni resultados de tests que aún no existen.
- Documenta que Boot 3 y WebLogic 12.2.1.4 tienen una incompatibilidad de runtime.
  Un WAR local no acredita ese despliegue. No cambies el stack para ocultarlo.
- Si haces una comprobación opcional en Tomcat 10.1, identifícala como runtime
  local de verificación y no como cumplimiento de WebLogic. Evita instalar
  WebLogic en el host mientras no se aclare esa incompatibilidad.

ENTREGABLES
- Ajusta o crea solo los archivos necesarios: devcontainer.json, Dockerfile
  si hace falta, Compose, Maven Wrapper/configuración, lockfile, perfil local,
  proxy, scripts de diagnóstico/arranque/pruebas/parada y README local.
- Los scripts deben poder repetirse, detectar dependencias faltantes, conservar
  datos y fallar con mensajes claros. No añadas wrappers redundantes a scripts
  ya existentes. No incrustes rutas absolutas del equipo en archivos compartidos.
- Actualiza AGENTS.md y bitácora con decisiones, comandos y resultados reales.
  Conserva el historial y registra cambios incrementales, sin inventar historia.
- Termina con instalado/reutilizado, comandos de uso, URLs locales realmente
  comprobadas, pruebas ejecutadas, pendientes y bloqueos. No publiques ni envíes
  el proyecto. No presentes este bootstrap como la solución funcional completa.
```

## 7 Cómo usarlo con los documentos anteriores

Coloca este documento en la carpeta del proyecto junto al plan. Abre Codex localmente y pide: «Lee 04_Preparar_Entorno_Local_con_Codex.md y ejecuta el prompt de la sección 6 sobre este equipo y este repositorio».

Cuando el entorno esté comprobado, usa el prompt de implementación del documento `03_Instrucciones_para_Codex.md`. Si aún no tienes el starter, la preparación provisional puede avanzar, pero la inspección y corrección del defecto sembrado quedan pendientes. Los archivos de entorno se adaptarán al starter real sin reemplazar su historial.
