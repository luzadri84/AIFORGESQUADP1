# Booking — repositorio base definitivo

Este repositorio es la base definitiva por decisión del usuario (DEC-004).
El starter queda fuera de alcance. T003 incorpora la base Maven WAR, entidades y
repositorios por funcionalidades y esquema Oracle con espacios semilla. Se conservan
Docker Compose, devcontainer y sondas. Todavía no hay API ni interfaz de reservas
ni despliegue público. [Resultado T003](docs/VERIFICACION_T003.md).

Consulte [uso y configuración local](docs/ENTORNO_LOCAL.md),
[evidencia de verificación](docs/VERIFICACION_LOCAL.md) y
[bitácora de decisiones](docs/BITACORA.md).

## Operación del entorno restaurado

Desde la raíz del repositorio, reutilizar las imágenes y credenciales importadas:

```powershell
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml up -d --no-build --wait --wait-timeout 600
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml ps
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash scripts/jdbc-check.sh read
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml stop
```

Oracle no publica puertos; dev reserva 127.0.0.1:4200/8080. No hay una interfaz Booking
que abrir todavía. La preparación original sigue documentada en ENTORNO_LOCAL.md;
`local.ps1 up` reconstruye imágenes y `local.ps1 verify` escribe otro marcador.
Para conservar la evidencia restaurada usar el modo `read` anterior. Si se restaura
en otra máquina, seguir TRANSFERIR_EQUIPO.md e importar datos antes de prepare.

La parada conserva los datos. El WAR base y sus pruebas de persistencia existen;
la aplicación funcional y su verificación de entrega siguen pendientes;
T014 es No aplica por cambio de alcance, conservada como trazabilidad y no implementada. Boot 3 y WebLogic 12.2.1.4 tienen una incompatibilidad de runtime.
Para cambiar de equipo, consulte [transferencia de imagenes e historial](docs/TRANSFERIR_EQUIPO.md).

[Paquete en GitHub y comprobacion final de restauracion](docs/TRANSFERENCIA_VERIFICADA.md).


## Spec Kit y continuación del trabajo

H001 integra metodología y planificación sobre esta infraestructura; no implementa
Booking. [Estado/evidencia de H001](docs/INTEGRACION_H001.md),
[uso de Spec Kit](docs/SPECKIT.md), [constitución](.specify/memory/constitution.md),
[spec](specs/001-booking-espacios/spec.md), [plan](specs/001-booking-espacios/plan.md),
[tareas y estados](specs/001-booking-espacios/tasks.md),
[handoffs](.handoffs/README.md) y [criterios de entrega e historial del starter](docs/ENTREGA_Y_STARTER.md).

Arquitectura elegida por el usuario (DEC-005): monolito Spring Boot por funcionalidades
booking/space/security, con errors solo si hay tratamiento compartido. Dentro de booking,
controlador–servicio transaccional–Spring Data JPA. Angular standalone por acceso/reservas. Alternativa: puertos/adaptadores en el mismo
monolito, con más interfaces/mapeos. Se conserva la infraestructura actual en ambas.
T003/H002 quedó verificada bajo DEC-006. La primera tarea pendiente es T004:
identidad Basic, CSRF y manejo seguro de credenciales. No repetir H001 ni T003.

[Explicación de la implementación real](docs/EXPLICACION_IMPLEMENTACION.md):
se actualiza con código y pruebas, preguntas respondidas y ejercicios solo de análisis.

## Base de persistencia T003

En este equipo el esquema y las semillas ya están aplicados. Para consultar y verificar:

```powershell
pwsh -NoProfile -File scripts/booking-db.ps1 status
docker compose -f compose.yaml -f .local/transfer/compose.images.yaml exec -T dev bash mvnw -B -ntp -f backend/pom.xml verify
```

Genera `backend/target/booking.war` y ejecuta pruebas transaccionales en Oracle real.
No inicia HTTP ni despliega el WAR. Requiere Oracle saludable, secretos restaurados
y conexión montada en dev. El primer Maven puede descargar dependencias.
Preparación de una base nueva y límites de DDL: [VERIFICACION_T003](docs/VERIFICACION_T003.md).
No ejecutar schema nuevamente aquí; seed conserva filas existentes, no las actualiza.
