# Plan de correcciones de auditoría — DEC-015

Este documento propone aceptación y orden; **el único estado está en [tasks.md](../specs/001-booking-espacios/tasks.md)**. T017 autorizó pruebas e informes; DEC-016 autoriza ahora implementar T018–T022. No son funcionalidades nuevas, ni el defecto del starter excluido. Conservar arquitectura por funcionalidades, credenciales, datos, historial y regresiones. [Diagnóstico](AUDITORIA_CUMPLIMIENTO_Y_SEGURIDAD.md), [dependencias](AUDITORIA_DEPENDENCIAS.md).

## Antes de entregar

| Prioridad / tarea canónica | Hallazgo | Propuesta mínima y aceptación | Dependencias / decisión |
|---|---|---|---|
| 1. T018 | AUD-01 | Cerrar aplicabilidad pendiente de Security/estáticos/multipart y elegir BOM/parches coherentes. Matriz antes/después y fuentes; sin avisos aplicables sin tratar. Verificar cabeceras éxito/error, auth/CSRF/propiedad, Oracle/concurrencia, WAR y catálogo actualizado. | T017. Acordar con evaluador las familias si Boot3.5 cambia Security/Data; revisar soporte de ramas anteriores. No forzar versiones incompatibles. |
| 2. T019 | AUD-02 | Rechazar decimales para spaceId/occurrences antes del servicio. Dos regresiones existentes pasan con400 y cero escrituras; enteros válidos siguen creando. | T017; independiente de aclaración WebLogic. |
| 3. T020 | AUD-03 | Clasificar excepciones MVC en400/404/405, incluyendo Allow si corresponde; 500solo inesperados. Tres regresiones existentes pasan, cuerpos redactados. | T017; sin inventar edición. |
| 4. T021 | AUD-04 | Anotar/configurar OpenAPI con Basic, flujo CSRF y todos los resultados reales. Regresión existente pasa; comprobar UI Swagger y contrato por operación. | T020 para contrato final de errores. No poner secretos en ejemplos. |
| 5. T022 | AUD-05 | Ajustar perfil de Dev Container restaurado a ambos Compose y mensajes de sonda vigentes. Abrir realmente con VS Code o CLI up/exec (admitido por DEC-016), sin reconstrucciones/volúmenes ajenos ni regenerar credenciales; leer marcador original. | T017. Reutilizar instalación; conservar procedimiento de instalación original cuando corresponda. |
| Gestión externa paralela: T015 | AUD-06 | Obtener aclaración del runtime, registrar decisión humana y probar el despliegue acordado. WAR/Tomcat no demuestra WebLogic12.2.1.4. | Bloqueo externo ya existente; no reabrir starter/T014. |

T018 tiene prioridad de revisión por seguridad. T019–T022 pueden prepararse de forma independiente de T015 bajo la autorización DEC-016. No se ejecuta automáticamente un cambio de stack ni una migración por este plan. Para Tomcat, 10.1.59 es candidato dentro de la familia, pero la elección final debe satisfacer todos los avisos activos y pruebas. Parches de Spring en ramas antiguas pueden requerir soporte Enterprise: comprobar disponibilidad/licencia antes de proponer una instalación concreta.

## Mejoras opcionales, no requisitos originales adicionales

| Tarea canónica | Hallazgo | Evaluación y aceptación propuesta |
|---|---|---|
| T023 | AUD-07 | Separar permisos de runtime/aprovisionamiento cuando se acuerde endurecer; conservar datos y contraseñas originales. CRUD funciona con permisos necesarios; DDL de objetos propios de prueba se rechaza. No retirar privilegios del esquema actual durante la auditoría. |
| T024 | AUD-08 | Medir consultas/listado con volumen pequeño y explícito, justificar paginación/fetch si es necesario; revisar cabeceras/servidor/TLS antes de exposición distinta de localhost. No inventar SLA, WAF ni cambios de negocio. |

## Cierre de una corrección

Cada tarea debe tener cambio mínimo, prueba antes/después, evidencia real en bitácora y commit incremental T/DEC. No eliminar/desactivar las seis regresiones rojas para obtener verde. Al completar el bloque, repetir verify completo, flujo navegador y JDBC read; confirmar diff de secretos/puertos y aplicación iniciada. Conservar los hallazgos históricos enlazando la evidencia que los cierra; no reescribir el informe como si la revisión47a6af4 hubiese pasado.

## Evidencia posterior

[Anexo de correcciones](CIERRE_CORRECCIONES_AUDITORIA.md), [tratamiento T018](TRATAMIENTO_DEPENDENCIAS_T018.md) y decisión humana DEC-019 de mantener familias. Estados solo en tasks.md. No reinterpretar pendientes como aceptación de riesgo.
