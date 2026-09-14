# Constitución de Booking

## Core Principles

### I. Requisitos y alcance explícitos
[AGENTS.md](../../AGENTS.md), los requisitos originales y las aclaraciones humanas
prevalecen. Los documentos de apoyo son propuestas, no prueba de aceptación.
DEC-004 adopta el repositorio actual como base definitiva y autoriza continuar
a la siguiente tarea pendiente; el límite anterior H001 queda superado.

### II. Simplicidad con garantías
Resolver necesidades actuales con la menor complejidad suficiente. No añadir
microservicios, CQRS, repositorios genéricos, orquestadores o dependencias sin una
necesidad demostrable. La arquitectura concreta de [plan.md](../../specs/001-booking-espacios/plan.md)
fue concretada por el usuario en DEC-005: monolito por funcionalidades, manteniendo
controlador–servicio–repositorio dentro de booking y Angular por acceso/reservas.

### III. Seguridad en el servidor
Preservar identidad autenticada, autorización por propietario, validación,
CSRF para Basic en navegador y secretos externos. No confiar userId/estado del
cliente ni revelar datos ajenos. La evidencia de implementación se mantiene en tasks.md.

### IV. Datos y concurrencia reales
Preservar Oracle y sus datos actuales. La solución debe impedir solapamientos
incluso ante peticiones concurrentes y verificar recurrencias. El bloqueo de Espacio,
intervalos semiabiertos, aceptación parcial y rollback del pedido se concretaron bajo DEC-007/009/011 y se probaron con Oracle; conservar el protocolo
y su evidencia al modificarlo.

### V. Evidencia y continuidad
Probar riesgos reales con herramientas existentes. Una sonda JDBC/ngc no acredita
Booking, seguridad ni concurrencia. La bitácora distingue propuesta, decisor,
aceptación, implementación y prueba; los commits conservan historia real.

## Restricciones estables

Conservar Java 21, Boot 3 permitido, Angular 20 standalone/strict, Oracle 19+ y
objetivo WAR. Las versiones resueltas están en el plan; no degradarlas por semillas
antiguas. No certificar WebLogic 12.2.1.4 por producir un WAR, ni Azure por resolver
MSAL. Starter excluido por DEC-004: T014 no aplica, sin migración ni investigación
de defecto equivalente. La aclaración de runtime (T015) sigue independiente.

## Flujo de desarrollo

Una feature Booking y una lista de tareas/estados en
[tasks.md](../../specs/001-booking-espacios/tasks.md). [.handoffs](../../.handoffs/README.md)
solo conserva contexto, evidencia y siguiente paso. Leer AGENTS y documentos
canónicos, ejecutar exclusivamente el bloque autorizado, verificar lo pertinente
y registrar decisiones/commits reales. No crear agentes ni servicios adicionales.

## Governance

Reglas operativas adoptadas por solicitud humana de Prompt A; fusión de redacción
a cargo del agente. DEC-005 registra la decisión humana posterior de arquitectura;
no aprueba automáticamente cada detalle de contrato o propuesta funcional. Cambiar un principio exige registrar DEC y revisar spec/plan/tasks
más pruebas afectadas; conservar la justificación anterior. Las instrucciones
expresas posteriores pueden cambiar alcance, dejando su trazabilidad.

**Version**: 1.1.1 | **Ratified**: 2026-09-14 (reglas operativas solicitadas) | **Last Amended**: 2026-09-14

Aclaración editorial DEC-013: referencias a implementación actualizadas tras DEC-007;
no se cambian los principios ni se atribuye aprobación individual de detalles.
