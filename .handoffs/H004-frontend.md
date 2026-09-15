# H004 — Conectar el primer recorrido de usuario

## Objetivo y alcance

Trabajar T008 y su corrección posterior T016, según alcance solicitado. Los criterios, dependencias y estados están en
[tasks.md](../specs/001-booking-espacios/tasks.md); no mantener otro tablero aquí.

## Contexto de entrada

H003. Leer [AGENTS](../AGENTS.md), [constitución](../.specify/memory/constitution.md),
[spec](../specs/001-booking-espacios/spec.md), [plan](../specs/001-booking-espacios/plan.md)
y [historia consolidada](../BITACORA.md), especialmente DEC-002/DEC-003 y decisiones posteriores.
Inspeccionar Git/código antes de actuar: aprovechar lo existente, no repetir trabajo.

## Límites y evidencia exigida

UI mínima y recorrido real de navegador hasta Oracle. No calendario complejo ni presentar mocks como E2E.

Los criterios completos se mantienen en las tareas canónicas. Registrar comandos,
resultados, pruebas omitidas/fallidas y alcance real; no inferir aprobación humana.

## Continuación

Sin ejecución de este bloque durante H001. Al trabajarlo registrar fecha/entorno,
responsable, archivos/diff, evidencia, referencias T/DEC y commit real si existe;
actualizar el estado solo en tasks.md. Conservar el punto de continuación si se interrumpe.
Siguiente bloque orientativo: H005, sujeto a autorización y dependencias.

## Ejecución H004 — 2026-09-14, DEC-010

Angular standalone strict, PrimeNG/Tailwind/RxJS, funcionalidades acceso/reservas;
Basic/token en memoria y ruta propia probada. ng build correcto, npm test 1/1;
backend regresión 27/27 y nuevo HttpSecurityTest 1/1 con servidor y cookies reales.
Navegador localhost:4200: ana creó #55 en Auditorio 20/01/2035 10–11 Bogotá; apareció
en listado, duplicado rechazado y cancelación dejó agenda vacía. Se conserva fila
cancelada, no se borra evidencia. Captura revisada visualmente. Hubo carga prematura
antes de terminar ng serve (ERR_EMPTY_RESPONSE), resuelta esperando compilación.
El 403 real tras lecturas paralelas llevó a STATELESS para contexto de seguridad;
sesión explícita solo CSRF, protección activa. Comprobación reproducible: iniciar
backend/frontend con scripts/app-process.sh dentro de dev; URL http://localhost:4200/.
Claves en .local/runtime/booking.properties, no impresas. Siguiente H005 T009/T010.

## Corrección posterior T016 — DEC-014, 2026-09-14

Reporte de captura del usuario: inicio/fin iguales y botón cargando. Se añadió
bookingRangeValidator de FormGroup, mensaje junto a Fin y bloqueo de envío inválido;
BookingApi limita espera a 15 s, finalize libera estado y nunca se reintenta POST.
Favicon vacío evita el 404 accesorio. No cambiar servidor/seguridad/datos ni atribuir
sin pruebas los mensajes de extensiones al fallo. La causa exacta de la espera
inicial no quedó identificada; repetición instrumentada recibió error y finalizó.

ngc/build y 13 tests frontend correctos; tiempo virtual para timeout, no corte de red
real. Navegador verificó igualdad, corrección, creación #157, duplicado y cancelación
propia; #157 quedó CANCELLED. npm test compila el servicio real antes de probarlo.
Comando dentro de dev: cd frontend && npm test && npm run build. URL localhost:4200.
Estado en tasks.md, evidencia en docs/VERIFICACION.md (desde raíz). Commit por T016/DEC-014; sin push.
No quedan tareas locales del reporte; T015 sigue externo, T014 no aplica.
