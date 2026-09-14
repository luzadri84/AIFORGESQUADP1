# Contrato HTTP local

DEC-008; implementaciÃ³n progresiva T004â€“T010. Todas las rutas salvo GET /api/csrf
requieren Authorization Basic. CSRF de sesiÃ³n obtenido como {token,headerName}; enviar
cookie de sesiÃ³n y X-CSRF-TOKEN en escrituras. Identidad/estado los determina servidor.

- GET /api/me: {username}; GET /api/spaces: catÃ¡logo de espacios (id,name,type,capacity,site).
- POST /api/bookings: {spaceId,startsAt,endsAt}; fechas ISO 8601 con offset y rango positivo.
  Ejemplo: {"spaceId":1,"startsAt":"2035-01-15T10:00:00-05:00","endsAt":"2035-01-15T11:00:00-05:00"}.
- GET /api/bookings: solo propias ACTIVE cuyo fin no haya pasado.
- DELETE /api/bookings/{id}: cancelaciÃ³n propia idempotente 204; ajena/ausente 404.
- Recurrencia semanal finita 1â€“12 se aÃ±adirÃ¡ en T009/T010; no aceptar parÃ¡metros aÃºn ausentes.
- 201 creaciÃ³n total, 200 parcial, 409 ninguna por conflicto, 400 entrada invÃ¡lida,
  401 identidad, 403 CSRF, 404 recurso inaccesible, 503 fallo transitorio de persistencia.
- Campos desconocidos (incluidos userId/state) se rechazan. No se expone SQL ni propietario ajeno.

OpenAPI protegido disponible en /v3/api-docs y /swagger-ui/index.html al iniciar el WAR.
Los endpoints de reservas se implementan en H003; H002 verifica la validaciÃ³n MVC
con una ruta exclusiva de pruebas, no disponible en producciÃ³n.

POST ahora siempre responde {created:[{id,spaceId,spaceName,startsAt,endsAt,status}],rejected:[{startsAt,endsAt,reason}]}. Un rechazo no revela datos del ocupante. 409 también contiene el resultado de cada ocurrencia; error técnico usa ProblemDetail y revierte todo el pedido.
