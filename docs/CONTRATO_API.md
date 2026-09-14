# Contrato HTTP implementado

DEC-008/009/011/012. Todas las rutas de negocio requieren Authorization Basic en
cada petición. GET `/api/csrf` público devuelve `{token,headerName}`: conservar la
cookie de sesión y enviar el token con `X-CSRF-TOKEN` en POST/DELETE. La cookie no
sustituye Basic. El cliente obtiene el token después de comprobar `/api/me`.

| Método y ruta | Resultado |
|---|---|
| GET /api/me | `{username}` sin contraseña |
| GET /api/spaces | Catálogo `{id,name,type,capacity,site}` |
| GET /api/bookings | Array de propias ACTIVE con fin posterior al reloj actual, ordenadas por inicio |
| POST /api/bookings | `{created:[{id,spaceId,spaceName,startsAt,endsAt,status}],rejected:[{startsAt,endsAt,reason}]}` |
| DELETE /api/bookings/{id} | 204 propia, incluso ya cancelada; 404 ajena/ausente |

Solicitud individual o semanal (occurrences ausente/null equivale a 1):

```json
{"spaceId":1,"startsAt":"2035-01-15T10:00:00-05:00","endsAt":"2035-01-15T11:00:00-05:00","occurrences":4}
```

Fechas ISO 8601 con offset, inicio anterior al fin, espacio positivo existente,
occurrences entero 1–12 con primera incluida. Repetición en America/Bogota.
Campos desconocidos, incluidos userId/ownerId/state/status, se rechazan; propietario
y estado los determina el servidor. No hay prohibición del pasado ni duración máxima.

201 todas creadas, 200 algunas, 409 ninguna por conflicto. Los tres usan el resultado
anterior; rechazos muestran la fecha solicitada y “Horario no disponible”, sin
identidad ni datos de la reserva que ocupa el espacio. 400 entrada inválida, 401
identidad ausente/incorrecta, 403 CSRF, 404 recurso ausente/inaccesible. Errores MVC
usan ProblemDetail; seguridad devuelve JSON genérico. Errores de acceso a datos 503,
inesperados 500, sin detalles SQL; la transacción revierte y el cliente debe consultar
su lista antes de reintentar. No hay clave de idempotencia para POST.

OpenAPI autenticado: `/v3/api-docs`, `/swagger-ui/index.html`. El modo JWT alternativo
está condicionado por configuración; Basic es el modo ejecutado. Los detalles y
límites de identidad empresarial están en [la explicación](EXPLICACION_IMPLEMENTACION.md).
