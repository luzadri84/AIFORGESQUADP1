# Booking — infraestructura local provisional

Esta carpeta todavía no contiene el starter ni la aplicación Booking.
Se preparan Docker Compose, devcontainer, Oracle real y sondas de dependencias,
compilación y persistencia. No hay funcionalidades de negocio ni despliegue público.

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

La parada conserva los datos. La aplicación, su WAR y sus pruebas quedan pendientes de desarrollo autorizado;
el starter real es una dependencia externa independiente. Boot 3 y WebLogic 12.2.1.4 tienen una incompatibilidad de runtime.
Para cambiar de equipo, consulte [transferencia de imagenes e historial](docs/TRANSFERIR_EQUIPO.md).

[Paquete en GitHub y comprobacion final de restauracion](docs/TRANSFERENCIA_VERIFICADA.md).


## Spec Kit y continuación del trabajo

H001 integra metodología y planificación sobre esta infraestructura; no implementa
Booking. [Estado/evidencia de H001](docs/INTEGRACION_H001.md),
[uso de Spec Kit](docs/SPECKIT.md), [constitución](.specify/memory/constitution.md),
[spec](specs/001-booking-espacios/spec.md), [plan](specs/001-booking-espacios/plan.md),
[tareas y estados](specs/001-booking-espacios/tasks.md),
[handoffs](.handoffs/README.md) y [criterios de entrega/starter](docs/ENTREGA_Y_STARTER.md).

Recomendación pendiente de decisión humana (DEC-003): monolito Spring Boot con
controladores, servicio transaccional, Spring Data JPA y funciones pequeñas de dominio;
Angular standalone por funcionalidad. Alternativa: puertos/adaptadores en el mismo
monolito, con más interfaces/mapeos. Se conserva la infraestructura actual en ambas.
La primera tarea funcional es T003/H002 después de decidir arquitectura y autorizar
el bloque; la integración no dispara la siguiente fase.
