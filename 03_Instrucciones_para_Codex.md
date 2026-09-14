# Instrucciones para desarrollar la prueba con Codex

> Actualización de alcance — DEC-004 (2026-09-14): este documento conserva el
> registro/propuesta de su etapa original. Las instrucciones de esperar, recibir,
> comparar o migrar un starter e investigar su defecto quedaron superadas: el
> repositorio actual es la base definitiva. No aplica por cambio de alcance;
> no se acredita implementación de esas tareas. WebLogic sigue independiente.
> Reglas y estado vigentes: AGENTS.md, tasks.md y docs/BITACORA.md.

Estos textos están preparados para usarse dentro del repositorio starter. No sustituyen la inspección del repositorio ni autorizan cambios de stack incompatibles con la prueba. El trabajo actual es planificación; los prompts de implementación se usarán cuando se decida comenzar a desarrollar.

## 1 Preparar el contexto

1. Obtener el starter real y abrir su devcontainer. Conservar su historial.
2. Mantener el AGENTS.md original. Comparar la propuesta `AGENTS.propuesto.md` con él e incorporar las decisiones aceptadas sin borrar requisitos ni ocultar contradicciones.
3. Copiar el plan técnico a documentación del proyecto si resulta útil. La guía de entrevista es material de estudio y no hace falta cargarla siempre como contexto de desarrollo.
4. Comprobar instrucciones adicionales del repositorio, versiones, scripts y estado de Git antes de generar código.

Codex lee AGENTS.md como instrucciones del proyecto; conviene mantenerlo breve y verificable. Esta conducta está descrita en la [documentación oficial sobre AGENTS.md](https://developers.openai.com/codex/guides/agents-md). El archivo `AGENTS.propuesto.md` no se descubre por ese nombre: se ofrece para comparar e incorporar, no como reemplazo automático.

No hace falta construir un framework de orquestación, múltiples agentes, un servidor MCP ni un sistema de evaluación de modelos. Una sesión de Codex con fases acotadas y verificación real cubre el objetivo. Tampoco se necesita agregar una API de OpenAI a la aplicación Booking.

## 2 Prompt inicial de inspección y refinamiento

```text
Vamos a desarrollar la prueba técnica Booking de Espacios Físicos de OTIC SED.
En esta primera fase inspecciona y planifica, sin implementar funcionalidades.

Lee las instrucciones aplicables, AGENTS.md, la historia de usuario y el correo
disponibles en el proyecto. Inspecciona el starter, su historial, devcontainer,
pom.xml, package.json, lockfiles, scripts y configuración de seguridad.
No supongas rutas o comandos que no existen.

Objetivo: el menor sistema completo que cumpla la prueba en 4–6 horas.
Debe crear reservas individuales y semanales, consultar y cancelar solo las
propias, impedir solapamientos también concurrentes y aceptar ocurrencias
válidas de una recurrencia aunque otras colisionen.

Contrasta el plan técnico si está disponible con el código inicial. Investiga
el defecto sembrado: distingue hipótesis de reproducción. No lo inventes.
Verifica versiones y dependencias antes de instalarlas; examina scripts de
instalación. No ejecutes scripts desconocidos solo por estar en el starter.

Revisa especialmente:
- Boot 3 y jakarta frente a WebLogic 12.2.1.4.
- Ramas de Boot y versiones realmente administradas por su BOM.
- Angular 20 con MSAL Angular 3: instalación, compilación y soporte documentado.
- Oracle real, WAR y entorno reproducible.
- Propietario tomado del principal, autorización por recurso y CSRF con Basic.

Entrega hallazgos con evidencia, requisitos trazados, decisiones propuestas,
dependencias verificadas, riesgos y un orden de implementación breve.
Puedes refinar AGENTS.md y documentos de planificación preservando la spec.
No afirmes que algo está probado si solo lo inferiste. Distingue ejecutado,
no ejecutado y bloqueado. Señala las aclaraciones de stack necesarias.
```

El resultado de esta fase debe revisarse, especialmente el defecto y las incompatibilidades. Una hipótesis no se convierte en hallazgo porque el agente la repita con seguridad.

## 3 Prompt para ejecutar el plan aceptado

```text
Implementa el plan aceptado en este repositorio. Trabaja por etapas pequeñas y
continúa de forma autónoma con decisiones reversibles dentro del alcance.
No pidas confirmación para cada archivo o prueba. Detén solo el componente
afectado si falta acceso esencial o si es necesario cambiar un requisito
obligatorio; avanza en lo que no dependa de ello y documenta el bloqueo.

Conserva y extiende AGENTS.md antes de cada decisión relevante. Mantén el
stack obligatorio, su modo strict, Oracle y WAR. No uses sustituciones
silenciosas, peers forzados, tests omitidos o versiones inexistentes.

Orden de trabajo:
1. Reproduce el defecto sembrado, añade evidencia o una prueba de regresión
   apropiada y corrige la causa con el menor cambio posible.
2. Construye datos semilla, identidad Basic, CSRF, contrato y flujo individual.
3. Implementa intervalos [inicio, fin), propiedad, bloqueo de Espacio dentro
   de transacción, repetición semanal acotada y cancelación lógica propia.
4. Implementa aceptación parcial por conflictos de negocio y rollback ante
   errores técnicos. No permitas que una colisión intermedia elimine las válidas.
5. Integra la pantalla Angular con PrimeNG, formularios tipados, RxJS y estados
   de carga, vacío, error, éxito parcial y cancelación.
6. Incluye puntos de integración compilables de MSAL y Resource Server,
   desactivados en Basic. No implementes autenticación real con Azure.
7. Verifica integración Oracle, autorización, concurrencia, CSRF, fechas y WAR.
8. Cierra README, bitácora, comandos reproducibles y matriz de criterios.

Mantén controladores pequeños, servicio transaccional y repositorios JPA.
Evita microservicios, CQRS, repositorios genéricos, abstracciones sin uso,
CRUD de salas, notificaciones, calendario complejo y funcionalidades extra.

Después de cada etapa, ejecuta las verificaciones pertinentes, inspecciona
el diff y crea un commit incremental que refleje ese trabajo real. No cambies
fechas ni fabriques historia. No modifiques cambios ajenos sin necesidad.

Actualiza la bitácora con propuesta, decisión, motivo, evidencia y resultado.
Separa sugerencias del agente de decisiones humanas confirmadas. No escribas
experiencias humanas ficticias ni resultados de herramientas no ejecutadas.

En cada actualización indica qué funciona, qué se comprobó y qué falta.
Antes de terminar, comprueba el flujo completo en la interfaz y proporciona
un resumen de cambios, pruebas, límites y requisitos bloqueados.
No publiques el repositorio, invites colaboradores ni envíes mensajes.
```

## 4 Prompt de revisión antes de entregar

```text
Revisa la implementación actual contra los adjuntos originales y AGENTS.md.
Empieza por los riesgos más graves, no por estilo ni preferencias personales.

Comprueba con código y pruebas:
- Propietario desde principal, lista propia y cancelación ajena rechazada.
- Colisiones parciales, exactas, contenciones y adyacencias.
- Recurrencia con conflicto no inicial y válidas conservadas.
- Dos transacciones sobre una sala inicialmente sin reservas.
- Rollback técnico, respuestas parciales y ausencia de datos ajenos en errores.
- CSRF con Basic, secretos, destino del interceptor y dependencias reales.
- Oracle, strict, versiones, WAR y veracidad del estado de WebLogic y MSAL.
- Reproducción y regresión del defecto sembrado.
- Arranque desde devcontainer siguiendo solo README.

Para cada hallazgo da severidad, archivo, comportamiento reproducible,
requisito afectado y arreglo mínimo. No propongas características nuevas.
Corrige fallos concretos dentro del alcance y ejecuta las pruebas relacionadas.
No cambies tests para acomodar un comportamiento contrario al requisito.

Entrega una matriz final: criterio, evidencia y estado real. Si una prueba
necesita infraestructura no disponible, márcala no ejecutada. Un build exitoso
no acredita seguridad, despliegue en WebLogic ni integración Oracle.
```

## 5 Prompts para explicar código y preparar cambios

```text
Explícame el flujo real de creación, desde el formulario hasta el commit en
Oracle y la respuesta visible. Usa archivos y métodos existentes. Define
brevemente cada concepto cuando aparezca. Explica el porqué de las decisiones,
qué alternativa se descartó y qué prueba comprueba cada garantía.
No modifiques código. Si el plan y la implementación difieren, señálalo.
Al final hazme tres preguntas para comprobar que entendí, sin responderlas.
```

```text
Actúa como entrevistador técnico sobre este repositorio. Haz una pregunta por
vez, espera mi respuesta y contrástala con el código real. Luego plantea una
repregunta o un cambio pequeño. Evalúa corrección, seguridad, capacidad de
localizar archivos y calidad de las pruebas. No escribas la solución del
cambio hasta que yo lo solicite. No supongas que una prueba pasa sin ejecutarla.
```

## 6 Plantilla de bitácora que debe llenarse con hechos

```markdown
## Decisión número N

Fecha y fase:
Problema observado:
Propuesta de la IA:
Alternativas consideradas:
Decisión y quién la tomó:
Motivo de aceptación o rechazo:
Fuente o evidencia revisada:
Verificación ejecutada y resultado:
Lo que sigue sin verificarse:
Archivos o commit relacionados:
```

No completar por anticipado afirmaciones como «rechacé una dependencia inventada» o «encontré IDOR» si no ocurrió. Sí puede registrarse desde el inicio que se contrastaron los requisitos de runtime con fuentes oficiales y se detectó una contradicción, dejando claro que esto no identifica por sí mismo el defecto sembrado.

## 7 Comandos y pruebas según el repositorio real

Los siguientes son orientativos. Codex debe verificar scripts y perfiles antes de copiarlos al README final.

| Objetivo | Comando orientativo | Evidencia que realmente aporta |
|---|---|---|
| Versiones backend | `java -version` y `./mvnw -version` | Runtime y herramienta disponibles |
| Dependencias Java | `./mvnw dependency:tree` | Árbol resuelto, no ausencia de vulnerabilidades |
| Pruebas y WAR | `./mvnw verify` | Solo las pruebas enlazadas al ciclo; revisar configuración de integración |
| Existencia y peers npm | `npm view paquete@version peerDependencies engines` | Metadata pública, no compatibilidad ejecutada |
| Instalación reproducible | `npm ci` | Instalación desde lockfile sin regenerarlo |
| Compilación Angular | `npm run build` | Compilación según script existente, revisar strict |
| Pruebas cliente | Script de test realmente definido | Evidencia del runner y escenarios que ejecutó |
| Integración Oracle | Perfil o script real del starter | Verificar datasource y versión, evitando fallback oculto |
| Historial y diff | `git status`, `git diff`, `git log --oneline` | Estado y trazabilidad de cambios |

No introducir comandos `npm run lint`, `npm run e2e` o perfiles Maven que todavía no existen y después documentarlos como ejecutados. Los tests terminados en `IT` no se ejecutan necesariamente con la configuración predeterminada: enlazar Failsafe al ciclo si se adopta esa convención o usar la existente y comprobar informes. Si Oracle no está listo, la verificación debe fallar explícitamente o marcarse bloqueada, no omitir silenciosamente las pruebas.
