# Explicación de la implementación real

Documento vivo, iniciado tras DEC-004. Fuente de estado: [tasks.md](../specs/001-booking-espacios/tasks.md).
Describe únicamente código/pruebas existentes; no acredita T013 por su creación.
La base definitiva es este repositorio. Starter, migración y defecto sembrado no
aplican por cambio de alcance. WebLogic conserva su evaluación independiente T015.

## 1. Estado y alcance

Al registrar el cambio documental existen infraestructura, sondas JDBC/Angular y
metodología SDD. No hay todavía API Booking, UI de reservas ni WAR de aplicación.
T003 inicia la base de datos y entidades; este texto se ampliará con su resultado real.

## 2. Ejecución

[README](../README.md) contiene los comandos de las imágenes importadas.
[INTEGRACION_H001](INTEGRACION_H001.md) conserva verificaciones de herramientas,
Oracle saludable, JDBC y ngc ya ejecutadas; no se repiten para documentar el cambio.

## 3. Arquitectura y archivos reales

[compose.yaml](../compose.yaml) configura dev y Oracle; [Dockerfile](../.devcontainer/Dockerfile)
combina Java/Node. [POM de sondas](../infra/checks/pom.xml) resuelve dependencias,
con packaging=pom. [JdbcCheck.java](../infra/checks/JdbcCheck.java) verifica base,
schema/PDB y marcador. [probe.ts](../infra/checks/frontend/probe.ts) es sonda de tipos.
DEC-005 elige monolito por funcionalidades para el código que se construya.

## 4–6. Flujos, reglas, seguridad y concurrencia

No existe todavía un recorrido Angular → API → Oracle. No atribuir a la sonda
las reservas, identidad HTTP, propiedad, CSRF, recurrencias ni protección concurrente.
Los requisitos/criterios permanecen en [spec](../specs/001-booking-espacios/spec.md)
y [plan](../specs/001-booking-espacios/plan.md); no se presentan como implementación.

## 7. Evidencia de pruebas

[verify-local.sh](../scripts/verify-local.sh) comprueba herramientas/dependencias y
ngc; [jdbc-check.sh](../scripts/jdbc-check.sh) ejecuta JdbcCheck. El modo read compara
el marcador original sin sustituirlo; write crea otro y no debe usarse para aparentar
persistencia del respaldo. Los resultados históricos están en la documentación enlazada.
No hay todavía tests de API ni carrera entre reservas.

## 8–9. Decisiones y procedencia

[Bitácora](BITACORA.md): DEC-001/002 integración real, DEC-003 propuesta previa,
DEC-004 base definitiva decidida por usuario y DEC-005 organización por funcionalidades.
No habrá integración ni investigación del defecto de otro repositorio y no se inventará
un sustituto. Los commits y verificaciones anteriores conservan su fecha y alcance.

## 10. Preguntas respondidas sobre lo existente

1. ¿Qué prueba JDBC? Conexión real, versión Oracle, identidad BOOKING/FREEPDB1 y,
   en read, coincidencia del marcador. Mostrar JdbcCheck.main; no prueba reservas.
2. ¿Por qué no ejecutar write para validar el traslado? Sustituye el token y elimina
   la evidencia original; comparar read con el archivo restaurado.
3. ¿Por qué no hay WAR? El POM de infra/checks tiene packaging=pom y es una sonda,
   no la aplicación. Empaquetar por sí solo tampoco demuestra compatibilidad WebLogic.
4. ¿Compilar la sonda Angular demuestra E2E? No: probe.ts verifica compilación/tipos,
   sin navegador, endpoints de Booking ni persistencia del flujo funcional.

Se ampliarán las preguntas a al menos doce con respuestas, archivos y errores
conceptuales ligados a la implementación al cerrar T013, sin fingir código futuro.

## 11. Ejercicios de análisis, sin implementar

- Cambiar un puerto local: analizar compose.yaml, .env.example y README; conservar
  enlace 127.0.0.1, comprobar puerto libre y config/ps; no modifica autorización ni
  concurrencia de negocio, aún inexistentes. No ejecutar el cambio como parte del ejercicio.
- Detectar marcador ausente: estudiar JdbcCheck.java (rama read), su SELECT y comparación;
  proponer prueba en un recurso aislado, nunca alterar el respaldo actual. Explicar
  por qué un error debe fallar y no crear automáticamente otro marcador.

Los seis retos finales se vincularán al código real y cubrirán comportamiento,
archivos/DTO/UI/SQL, riesgos de propiedad/concurrencia, pruebas y secuencia mínima,
según [criterios de entrega](ENTREGA_Y_STARTER.md). Son análisis, no ampliación del MVP.

## 12. Límites

La aceptación de arquitectura no acredita código ni pruebas. T015 sigue pendiente;
T014 no aplica y no se considera implementada. No hay despliegue público ni
certificación de producción. Este documento se actualiza al finalizar cada tarea real.
