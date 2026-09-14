# Contrato HTTP local

DEC-008; implementación progresiva T004–T010. Todas las rutas salvo GET /api/csrf
requieren Authorization Basic. CSRF de sesión obtenido como {token,headerName}; enviar
cookie de sesión y X-CSRF-TOKEN en escrituras. Identidad/estado los determina servidor.

- GET /api/me: {username}; GET /api/spaces: catálogo de espacios (id,name,type,capacity,site).
- POST /api/bookings: {spaceId,startsAt,endsAt}; fechas ISO 8601 con offset y rango positivo.
  Ejemplo: {"spaceId":1,"startsAt":"2035-01-15T10:00:00-05:00","endsAt":"2035-01-15T11:00:00-05:00"}.
- GET /api/bookings: solo propias ACTIVE cuyo fin no haya pasado.
- DELETE /api/bookings/{id}: cancelación propia idempotente 204; ajena/ausente 404.
- Recurrencia semanal finita 1–12 se añadirá en T009/T010; no aceptar parámetros aún ausentes.
- 201 creación total, 200 parcial, 409 ninguna por conflicto, 400 entrada inválida,
  401 identidad, 403 CSRF, 404 recurso inaccesible, 503 fallo transitorio de persistencia.
- Campos desconocidos (incluidos userId/state) se rechazan. No se expone SQL ni propietario ajeno.

OpenAPI protegido disponible en /v3/api-docs y /swagger-ui/index.html al iniciar el WAR.
Los endpoints de reservas se implementan en H003; H002 verifica la validación MVC
con una ruta exclusiva de pruebas, no disponible en producción.

POST ahora siempre responde {created:[{id,spaceId,spaceName,startsAt,endsAt,status}],rejected:[{startsAt,endsAt,reason}]}. Un rechazo no revela datos del ocupante. 409 también contiene el resultado de cada ocurrencia; error técnico usa ProblemDetail y revierte todo el pedido.
