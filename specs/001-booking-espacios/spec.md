# Feature Specification: Booking de Espacios Físicos

**Feature**: `001-booking-espacios` | **Created**: 2026-09-14 | **Status**: Implementación local verificada; pendiente externo T015
**Git al inicio**: main, 5dcdb7c; esta feature documental no crea otra rama.

**Alcance actualizado por DEC-004:** el repositorio actual es la base definitiva.
El starter y su defecto quedan excluidos; T014 no aplica y T015 sigue independiente.

**Input histórico H001**: Prompt A y semillas contrastadas con AGENTS y código real.
Entonces solo había infraestructura y sondas. T003 añadió persistencia; DEC-007
autorizó T004–T013, ahora con API, UI y pruebas locales. Las etapas históricas
permanecen en Git y sus informes. El texto original
de AGENTS se conserva. Los adjuntos originales fueron registrados en la documentación
previa; no se acredita una nueva recepción del starter ni revisión del correo en H001.

Los FR conservan los IDs de las semillas. DEC-007 autoriza el alcance local existente;
DEC-008–013 documentan concreciones técnicas del agente sin atribuir aprobación
individual al usuario. Véanse [plan](plan.md), [trazabilidad](trazabilidad.md),
[orígenes](../../docs/INTEGRACION_H001.md) y [verificación](../../docs/VERIFICACION_FINAL.md).

## User Scenarios & Testing

### US1 Reservar un espacio (P1)
Como funcionario autenticado quiero reservar un espacio para realizar una actividad sin colisionar con otra reserva activa.

### US2 Consultar y cancelar reservas propias (P1)
Como funcionario quiero ver mis reservas activas y cancelar una propia sin poder consultar o cancelar las de otros.

### US3 Repetir una reserva semanal (P2, implementada bajo DEC-007/011)
Como funcionario quiero solicitar varias ocurrencias y saber cuáles se crearon y cuáles chocaron, conservando las válidas.

## Requirements

### Functional Requirements

- FR001 Identidad verificable vía HTTP Basic local en cada operación de negocio. Dos usuarios distinguibles; no selector de identidad confiado al cliente.
- FR002 Espacios semilla con id, nombre, tipo, capacidad y sede; sin CRUD administrativo.
- FR003 Crear reserva válida en espacio existente; rechazar rango inválido o campos obligatorios ausentes. Identidad y estado no aceptados desde el body.
- FR004 Ninguna pareja de reservas ACTIVE del mismo espacio puede solaparse, incluidas igualdad, ambas contenciones y solapamientos parciales. Adyacencias permitidas.
- FR005 Dos peticiones concurrentes para el mismo horario inicialmente libre producen una reserva activa, no dos.
- FR006 Listar solo propias activas y cancelar solo propias; un intento sobre otra no cambia datos ni revela su propietario.
- FR007 Expandir recurrencia semanal y validar cada ocurrencia; crear válidas y señalar específicamente las rechazadas. Comprobar conflicto no inicial.
- FR008 API REST documentada y frontend mínimo que ejecuta creación, consulta y cancelación, incluyendo resultado parcial.
- FR009 Oracle 19c o superior, Java 21, stack frontend/backend obligatorio, strict y WAR; ejecución reproducible mediante devcontainer.
- FR010 MSAL Angular y Resource Server preparados como extensión, con Basic independiente de Azure real. Sin integración real requerida.
- FR011 Pruebas automatizadas críticas, dependencias reales, secretos fuera de código/historial, bitácora y commits reales.
- FR012 Resolver/documentar la contradicción del runtime objetivo (T015). Componente histórico de este requisito: inspeccionar, explicar y corregir el defecto del starter (T014), **No aplica por cambio de alcance** (DEC-004), no implementado.

### Casos de aceptación y bordes

- A reserva 10:00–11:00. Solicitar 10:30–11:30 o exactamente 10:00–11:00 en esa sala se rechaza. Solicitar 11:00–12:00 se permite.
- Cuatro lunes, conflicto solo en el segundo: tres creaciones y segundo rechazo identificado.
- A no ve reservas de B; A intenta cancelar la de B: rechazo y estado intacto. El propietario cancela y el horario queda disponible.
- Espacio ausente, inicio igual o mayor a fin y fechas sin offset: error explícito sin escrituras.
- Dos transacciones independientes contra Oracle: una creación y una colisión con una sola fila activa.

## Semántica implementada bajo DEC-007/009/011

Semanal, occurrences de 1 a 12 incluyendo primera fecha, ausente/null=1. Cancelar una
ocurrencia. Mis activas: ACTIVE y fin posterior al reloj actual, incluyendo iniciadas.
Mostrar/generar en America/Bogota y transportar ISO 8601 con offset. No prohibir pasado
ni agregar límite de duración sin requerimiento. Son concreciones del agente dentro
la continuación autorizada; no se inventa aceptación humana individual del contrato.

## Exclusiones

Notificaciones, administración de espacios, calendario drag and drop, RRULE completo, edición de reservas salvo interfaz existente que deba asegurarse, cancelación de serie, Azure real, microservicios y asistente IA dentro de Booking.

## Terminado

El criterio de terminado exige evidencia real de FR001–FR011 y resolver el componente vigente de FR012 con el evaluador (T015). T014 queda excluida por DEC-004. Si siguen pendientes externos, puede prepararse una entrega candidata, pero no declarar cumplimiento total. Handoffs y tareas terminadas no reemplazan pruebas ni la definición de terminado del evaluador.

## Key Entities implementadas

- Espacio: id, nombre, tipo, capacidad y sede; catálogo semilla sin administración.
- Reserva: espacio, propietario, inicio, fin y estado; vínculo con cada ocurrencia.
- Usuario: identidad del principal local; sin CRUD de usuarios en este alcance.

## Success Criteria

- SC001: el recorrido de US1 crea una reserva válida y rechaza cada tipo de
  solapamiento de FR004; permite las dos adyacencias bajo la propuesta semiabierta.
- SC002: en US2, dos identidades no ven ni cancelan datos ajenos; cancelar una propia
  libera el horario y repetir la cancelación conserva un resultado coherente.
- SC003: para US3, cuatro ocurrencias con
  conflicto solo en la segunda dejan tres creadas y un rechazo identificado.
- SC004: dos solicitudes independientes al mismo espacio/intervalo libre terminan
  con una sola reserva activa; un fallo técnico no deja un pedido parcialmente escrito.
- SC005: evidencias vinculadas a FR y pruebas reales, WAR y explicación verificables;
  ninguna afirmación de cumplimiento total mientras T015 siga abierta.

Evidencia local de estos resultados en VERIFICACION_FINAL; T015 sigue independiente. No se inventan metas de latencia,
usuarios concurrentes, implementación ni rendimiento para llenar una plantilla.
