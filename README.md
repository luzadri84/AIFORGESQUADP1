# Booking — infraestructura local provisional

Esta carpeta todavía no contiene el starter ni la aplicación Booking.
Se preparan Docker Compose, devcontainer, Oracle real y sondas de dependencias,
compilación y persistencia. No hay funcionalidades de negocio ni despliegue público.

Consulte [uso y configuración local](docs/ENTORNO_LOCAL.md),
[evidencia de verificación](docs/VERIFICACION_LOCAL.md) y
[bitácora de decisiones](docs/BITACORA.md).

```powershell
pwsh -NoProfile -File scripts/local.ps1 prepare
pwsh -NoProfile -File scripts/local.ps1 up
pwsh -NoProfile -File scripts/local.ps1 verify
pwsh -NoProfile -File scripts/local.ps1 stop
```

La parada conserva los datos. La aplicación, su WAR y sus pruebas quedan pendientes
del starter real. Boot 3 y WebLogic 12.2.1.4 tienen una incompatibilidad de runtime.
Para cambiar de equipo, consulte [transferencia de imagenes e historial](docs/TRANSFERIR_EQUIPO.md).

[Paquete en GitHub y comprobacion final de restauracion](docs/TRANSFERENCIA_VERIFICADA.md).
