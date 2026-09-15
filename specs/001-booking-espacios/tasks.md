# Tareas de Booking

H001 reconcilió el repositorio main/5dcdb7c el 2026-09-14; T003 añadió persistencia y
DEC-007 autorizó T004–T013. API/UI/Oracle y pruebas actuales en
[VERIFICACION_FINAL](../../docs/VERIFICACION_FINAL.md). Una sola feature; IDs T001–T015 conservados, T016 añadida para el fallo reportado.

**Alcance vigente:** base definitiva DEC-004; arquitectura humana DEC-005 por
funcionalidades. DEC-007 autoriza la solución local sin pausas entre handoffs;
DEC-008–013 registran concreciones técnicas. T014 no aplica; T015 conserva bloqueo
independiente. No reiniciar integración ni repetir trabajo ya verificado.

## Convención de estado

Este archivo es la lista canónica. `[x]` solo cuando se cumple la aceptación con evidencia. `[ ]` permanece para pendiente, en curso, bloqueada o **No aplica por cambio de alcance**; esta última es una disposición terminal sin implementación y no bloquea dependencias; indicar la situación en la anotación de la tarea. Solo una tarea en curso por ejecutor. Las dependencias externas no bloquean tareas independientes. No usar [P] para sugerir ejecución paralela en este paquete.

No duplicar estados en índices de handoffs. Conservar IDs y añadir los nuevos al final si el análisis descubre trabajo necesario. Si ya existen IDs al importar, crear una correspondencia y actualizar todas las referencias sin romper el historial.

## T001 Integrar Spec Kit y reconciliar evidencias del entorno previo

- [x] T001 Integrar Spec Kit y reconciliar evidencias del entorno previo en configuración Spec Kit de la versión instalada, .devcontainer y scripts existentes.

- Dependencias: sin dependencia interna.
- Handoff: H001.
- Aceptación: CLI/integración identificados, diff preserva entorno, evidencia de los tres commits y reporte previo clasificada; no repetir instalaciones.
- Situación: completada; integración local y diagnóstico reconciliados.
- Evidencia: specify 0.8.1/check/integration list, manifests SHA-256, nueve skills habilitadas descubiertas por Codex skills/list. Commit b977ca3, DEC-001 y DEC-002; detalle en docs/INTEGRACION_H001.md.

## T002 Reconciliar constitución, spec, plan, backlog y reglas de handoff

- [x] T002 Reconciliar constitución, spec, plan, backlog y reglas de handoff en AGENTS.md, .specify/memory/constitution.md, specs/001-booking-espacios/*.md, .handoffs/.

- Dependencias: T001.
- Handoff: H001.
- Aceptación: Referencias válidas, versiones reales, criterios del evaluador preservados y bitácora enlazada.
- Situación: completada; documentos y reglas fusionados sin implementar negocio.
- Evidencia: 20 documentos/100 enlaces locales y referencias T/FR/H validados; 15 IDs únicos; precondiciones Spec Kit correctas; JDBC read/ngc correctos; infraestructura sin diff; AGENTS y bitácora originales conservados. DEC-002/DEC-003 y docs/INTEGRACION_H001.md.

## T003 Preparar entidades Oracle, esquema y espacios semilla idempotentes

- [x] T003 Preparar entidades Oracle, esquema y espacios semilla idempotentes en backend/: base Maven WAR, paquetes space/booking y SQL versionado; conservar infra/checks y reutilizar conexión existente.

- Dependencias: T002.
- Handoff: H002.
- Aceptación: Conexión con usuario de aplicación, FK/check/estados, semillas repetibles y round trip temporal sin pérdida de instante.
- Situación: completada; autorización DEC-004, arquitectura DEC-005, detalle técnico DEC-006.
- Evidencia: docs/VERIFICACION_T003.md; Maven verify genera WAR y pasa 12 pruebas Oracle reales, seed repetido sin duplicar/sobrescribir, 3 espacios/0 reservas al finalizar; marcador original conservado.

## T004 Implementar identidad Basic, CSRF y manejo seguro de credenciales

- [x] T004 Implementar identidad Basic, CSRF y manejo seguro de credenciales en backend: security; frontend: configuración proxy si existe.

- Dependencias: T002.
- Handoff: H002.
- Aceptación: Dos identidades, GET protegido 401, escritura sin CSRF 403, credenciales externas y respuesta de identidad sin secretos.
- Situación: completada; DEC-008.
- Evidencia: SecurityContractTest (3 escenarios, validación MVC en ruta exclusiva de test), 15 tests totales correctos y WAR ejecutable. docs/CONTRATO_API.md; reservas reales se integran en H003.

## T005 Fijar DTO, validación, errores y documentación del contrato

- [x] T005 Fijar DTO, validación, errores y documentación del contrato en backend: DTO/controller/advice y configuración SpringDoc.

- Dependencias: T003, T004.
- Handoff: H002.
- Aceptación: Campos obligatorios, offset y rango inválidos 400, userId/state no aceptados, errores sin datos ajenos; ejemplos API incluidos.
- Situación: completada; DEC-008.
- Evidencia: SecurityContractTest (3 escenarios, validación MVC en ruta exclusiva de test), 15 tests totales correctos y WAR ejecutable. docs/CONTRATO_API.md; reservas reales se integran en H003.

## T006 Implementar regla de intervalos y pruebas parametrizadas

- [x] T006 [US1] Implementar regla de intervalos y pruebas parametrizadas en backend: TimeRange o helper equivalente y tests.

- Dependencias: T002.
- Handoff: H003.
- Aceptación: Parciales a ambos lados, igualdad y contenciones chocan; ambas adyacencias y rangos separados permiten.
- Situación: completada; DEC-009.
- Evidencia: TimeRangeTest (10 intervalos), BookingApiTest (2 escenarios transaccionales Oracle con MockMvc), verify 27 tests sin fallos; sin reservas duraderas de prueba.

## T007 Implementar crear, listar propias y cancelar con transacción y bloqueo de Espacio

- [x] T007 [US1] Implementar crear, listar propias y cancelar con transacción y bloqueo de Espacio en backend: servicio/repositories/controllers y tests API Oracle.

- Dependencias: T003, T004, T005, T006.
- Handoff: H003.
- Aceptación: Creación 201, colisión 409, espacio ausente 404, listado propio, cancelación ajena 404 sin cambio, propia idempotente 204 y horario liberado.
- Situación: completada; DEC-009.
- Evidencia: TimeRangeTest (10 intervalos), BookingApiTest (2 escenarios transaccionales Oracle con MockMvc), verify 27 tests sin fallos; sin reservas duraderas de prueba.

## T008 Integrar el primer recorrido Angular de reserva individual

- [x] T008 [US2] Integrar el primer recorrido Angular de reserva individual en frontend: auth, API service, formulario y lista.

- Dependencias: T007.
- Handoff: H004.
- Aceptación: Cubre US1 y US2: navegador real crea/lista/cancela en Oracle; strict pasa; errores y loading visibles; Basic no se adjunta a URL ajena.
- Situación: completada; DEC-010.
- Evidencia: ng build strict correcto, npm test 1/1, HttpSecurityTest real 1/1 y regresión backend 27/27; navegador localhost:4200 crea/lista/rechaza duplicado/cancela reserva #55. H004 y bitácora detallan corrección CSRF.

## T009 Expandir recurrencia semanal finita y probar límites

- [x] T009 [US3] Expandir recurrencia semanal finita y probar límites en backend: generador y tests de fechas.

- Dependencias: T006.
- Handoff: H005.
- Aceptación: Count incluye primera fecha; 4 genera 4; límites 0/13 rechazan; hora Bogotá preservada y conflicto interno previsto.
- Situación: completada; DEC-011.
- Evidencia: verify 35/35, WeeklyRecurrenceTest y BookingApiTest; Angular build/test correctos; navegador Ana #72 segunda semana, Bruno #73–75 (3 creadas/1 rechazada), canceladas luego por sus propietarios.

## T010 Integrar aceptación parcial en API y pantalla

- [x] T010 [US3] Integrar aceptación parcial en API y pantalla en backend: servicio/DTO/tests; frontend: opciones semanales y resultado.

- Dependencias: T007, T008, T009.
- Handoff: H005.
- Aceptación: Conflicto solo en segunda de cuatro deja tres filas; ninguna válida 409; UI identifica ocurrencias sin datos ajenos y refresca lista.
- Situación: completada; DEC-011.
- Evidencia: verify 35/35, WeeklyRecurrenceTest y BookingApiTest; Angular build/test correctos; navegador Ana #72 segunda semana, Bruno #73–75 (3 creadas/1 rechazada), canceladas luego por sus propietarios.

## T011 Verificar concurrencia, rollback técnico y seguridad con Oracle real

- [x] T011 Verificar concurrencia, rollback técnico y seguridad con Oracle real en backend: tests integración y scripts de ejecución existentes.

- Dependencias: T010.
- Handoff: H006.
- Aceptación: Dos transacciones con sala vacía: un 201, un 409 y una fila; fallo técnico revierte pedido; autorización/CSRF/rangos/cancelación comprobados.
- Situación: completada; DEC-012.
- Evidencia: verify 39/39; OracleConcurrencyTest HTTP/Oracle (201+409+1 fila y rollback tras flush), JwtValidationTest; adaptadores MSAL compilan, Basic sin Azure, npm ls correcto y audit 0. Azure real no verificado.

## T012 Preparar MSAL/Resource Server y revisar dependencias finales

- [x] T012 Preparar MSAL/Resource Server y revisar dependencias finales en frontend: adaptador/providers; backend: configuración modo JWT condicionada.

- Dependencias: T004, T008.
- Handoff: H006.
- Aceptación: Adaptadores reales compilan, Basic no requiere Azure, no peers forzados, scope de revisión de dependencias declarado; Azure real no acreditado.
- Situación: completada; DEC-012.
- Evidencia: verify 39/39; OracleConcurrencyTest HTTP/Oracle (201+409+1 fila y rollback tras flush), JwtValidationTest; adaptadores MSAL compilan, Basic sin Azure, npm ls correcto y audit 0. Azure real no verificado.

## T013 Construir WAR y cerrar verificación local y documentación candidata

- [x] T013 Construir WAR y cerrar verificación local y documentación candidata en backend build, README, AGENTS, bitácora, docs/EXPLICACION_IMPLEMENTACION.md e informes existentes.

- Dependencias: T011, T012.
- Handoff: H007.
- Aceptación: WAR generado y evidencia disponible de ejecución; flujo limpio devcontainer+Oracle+Angular; historial y secretos revisados; bloqueos externos visibles; docs/EXPLICACION_IMPLEMENTACION.md explica archivos y flujos reales, incluye al menos 12 preguntas respondidas y 6 retos con impacto en código, riesgos y pruebas; no implementar esos retos como ampliación del MVP. Cumplir íntegramente las 12 secciones y el contenido por reto de docs/ENTREGA_Y_STARTER.md (sección T013).
- Situación: completada; entrega local operable, DEC-013.
- Evidencia: docs/VERIFICACION_FINAL.md; app.ps1 verify pasa 50 tests Java, ngc/build y npm test; stop/start reales, Oracle healthy, marcador/credenciales originales intactos y login navegador posterior. WAR ejecutable, secretos excluidos, 12 secciones/15 preguntas/6 retos analíticos. WebLogic no acreditado.

## T014 Inspeccionar starter recibido e integrar su defecto y regresión

- [ ] T014 Inspeccionar starter recibido e integrar su defecto y regresión en starter real y archivos afectados que determine la investigación.

- Dependencias: sin dependencia interna.
- Handoff: H008.
- Aceptación histórica, sin vigencia por DEC-004: Reproducción demostrada, causa y fix mínimos, prueba apropiada antes/después e historial conservado; si llega tarde comparar sin reescribir trabajo; inspeccionar en carpeta separada, conservar el punto previo e historiales, documentar procedencia del portado y actualizar explicación final y pruebas afectadas. Seguir los nueve pasos de docs/ENTREGA_Y_STARTER.md (sección T014); mantener T015 separada.
- Situación: **No aplica por cambio de alcance** — DEC-004, decisión del usuario del 2026-09-14. No implementada; se conserva solo para trazabilidad.
- Evidencia: instrucción explícita del usuario recogida en DEC-004; sin recepción, migración ni prueba de defecto ejecutadas.

## T015 Resolver con el evaluador la contradicción de runtime y su evidencia exigida

- [ ] T015 Resolver con el evaluador la contradicción de runtime y su evidencia exigida en README/plan/bitácora y configuración de despliegue si se acuerda.

- Dependencias: sin dependencia interna.
- Handoff: H008.
- Aceptación: Aclaración recibida con alcance exacto, prueba del runtime acordado si se exige; no declarar WebLogic válido por tener WAR ni desplegar públicamente sin instrucción.
- Situación: bloqueada por aclaración externa del runtime.
- Evidencia: sin prueba funcional ejecutada; ver alcance real de H001.

## T016 Corregir envío de intervalo inválido y recuperación del formulario

- [x] T016 Corregir validación visible de fechas y espera sin límite en el cliente.
- Dependencias: T008, T013.
- Handoff: H004, seguimiento posterior a la entrega.
- Aceptación: igualdad/rango inverso se señalan junto a Fin sin POST; corregir fechas permite reservar; errores/timeout liberan el formulario sin reintento automático; navegador y regresiones pertinentes.
- Situación: completada; corrección y recuperación verificadas, DEC-014.
- Evidencia: 13 tests frontend y build strict correctos; navegador muestra aviso/inhabilita igualdad, permite corregir/crear #157, rechaza duplicado y cancela #157. Timeout probado con tiempo virtual sobre BookingApi real (HTTP simulado), sin reintentos. API directa/proxy 400; causa de espera inicial no confirmada. Ver seguimiento T016 en docs/VERIFICACION_FINAL.md.

## Regla de cierre de la prueba

T013 permite preparar un resultado local candidato. T014 no aplica por DEC-004 y no es un bloqueo ni una tarea implementada. T015 mantiene su aceptación y bloqueo independiente: no acreditar compatibilidad de despliegue sin resolverlo. La descripción histórica de recepción/migración se conserva, pero no se ejecutará ni se inventará un defecto sustituto.

## T017 Auditar cumplimiento y seguridad de la solución real

- [x] T017 Auditar revisión 47a6af4, sin corregir código productivo.
- Dependencias: T013, T016.
- Handoff: H006 y H007.
- Aceptación: fuentes originales distinguidas A/B/C/D, matriz, pruebas adversas/Oracle/navegador, dependencias y secretos, informes y propuesta mínima; conservar regresiones fallidas.
- Situación: completada la auditoría disponible; **no equivale a aprobación de entrega**, DEC-015.
- Evidencia: docs/AUDITORIA_CUMPLIMIENTO_Y_SEGURIDAD.md y docs/AUDITORIA_DEPENDENCIAS.md. 75 Java (69 pasan/6 fallan) + espera Oracle1/1; frontend13/13/build; comparador3/3. Seis fixtures visuales/HTTP CANCELLED, marcador original intacto. WebLogic/Dev Container completo/correo no verificables según informe.

## T018 Resolver avisos de dependencias aplicables y acordar parches compatibles

- [ ] T018 Resolver avisos de dependencias aplicables y acordar parches compatibles.
- Dependencias: T017.
- Handoff: H006.
- Aceptación: Inventario antes/después, avisos aplicables tratados o descartados con evidencia, BOM compatible, pruebas seguridad/Oracle/WAR correctas.
- Situación: parcial, mitigación anticipada de cabeceras y Tomcat10.1.59 verificados de forma dirigida; 29 avisos pendientes,13 no aplicables a esta configuración con evidencia y1 mitigado de43 coincidencias restantes. Usuario mantiene stack obligatorio (DEC-019); docs/TRATAMIENTO_DEPENDENCIAS_T018.md.
- Evidencia de origen: AUD-01, DEC-015; docs/PLAN_CORRECCIONES_AUDITORIA.md.

## T019 Rechazar coerción decimal de identificadores y ocurrencias

- [x] T019 Rechazar coerción decimal de identificadores y ocurrencias.
- Dependencias: T017.
- Handoff: H006.
- Aceptación: Dos regresiones decimales devuelven400 sin escritura; enteros válidos mantienen comportamiento.
- Situación: completada; suite completa88/88 y HTTP real contra WAR e859c80, DEC-017. Evidencia en docs/CIERRE_CORRECCIONES_AUDITORIA.md.
- Evidencia de origen: AUD-02, DEC-015; docs/PLAN_CORRECCIONES_AUDITORIA.md.

## T020 Preservar estados HTTP de errores MVC

- [x] T020 Preservar estados HTTP de errores MVC.
- Dependencias: T017.
- Handoff: H006.
- Aceptación: Tres regresiones400/404/405 pasan, 500solo inesperados, cuerpo redactado y Allow apropiado.
- Situación: completada; suite completa88/88 y HTTP real contra WAR e859c80, DEC-017. Evidencia en docs/CIERRE_CORRECCIONES_AUDITORIA.md.
- Evidencia de origen: AUD-03, DEC-015; docs/PLAN_CORRECCIONES_AUDITORIA.md.

## T021 Completar contrato OpenAPI real

- [ ] T021 Completar contrato OpenAPI real.
- Dependencias: T020.
- Handoff: H007.
- Aceptación: Seguridad Basic/CSRF y respuestas reales documentadas, prueba OpenAPI y uso Swagger verificados.
- Situación: implementada y pruebas OpenAPI/HTTP del WAR nuevo correctas; pendiente verificación interactiva de Swagger por autenticación nativa que debe completar el usuario. No cerrada, DEC-020.
- Evidencia de origen: AUD-04, DEC-015; docs/PLAN_CORRECCIONES_AUDITORIA.md.

## T022 Alinear Dev Container restaurado y mensajes de verificación

- [x] T022 Alinear Dev Container restaurado y mensajes de verificación.
- Dependencias: T017.
- Handoff: H007.
- Aceptación: Ambos Compose con imágenes importadas, apertura real sin pérdida de datos/credenciales y JDBC read; sonda sin afirmaciones obsoletas.
- Situación: completada para reapertura local con CLI up/exec, toolchain/compilación/app/JDBC read verificados, DEC-021. Bootstrap nuevo documentado y guardado; no se acredita instalación limpia ni apertura visual VS Code.
- Evidencia de origen: AUD-05, DEC-015; docs/PLAN_CORRECCIONES_AUDITORIA.md.

## T023 Evaluar permisos mínimos de ejecución Oracle

- [ ] T023 Evaluar permisos mínimos de ejecución Oracle.
- Dependencias: T017.
- Handoff: H006.
- Aceptación: Propuesta opcional aprobada antes de migrar permisos; CRUD verificado y separación de aprovisionamiento justificada.
- Situación: opcional, pendiente de decisión; no implementada.
- Evidencia de origen: AUD-07, DEC-015; docs/PLAN_CORRECCIONES_AUDITORIA.md.

## T024 Evaluar límites y endurecimiento según exposición y volumen

- [ ] T024 Evaluar límites y endurecimiento según exposición y volumen.
- Dependencias: T017.
- Handoff: H006.
- Aceptación: Medición acotada y decisión justificada sobre consultas/listado/cabeceras; sin nuevas obligaciones o funcionalidades inventadas.
- Situación: opcional, pendiente de decisión; no implementada.
- Evidencia de origen: AUD-08, DEC-015; docs/PLAN_CORRECCIONES_AUDITORIA.md.

T013 conserva la ejecución histórica local; su criterio de Dev Container queda matizado por AUD-05/T022. T012 conserva su revisión de dependencias anterior, cuyo alcance no era auditoría integral Java. T014 permanece No aplica; T015 conserva su texto y bloqueo independiente.

## T025 Consolidar entrega y estudio, operación Windows/Linux

- [ ] T025 Consolidar documentos y preparar instrucciones portables.
- Dependencias: T013, T022; no depende del cierre T018/T021/T015.
- Handoff: H007.
- Aceptación: respaldo íntegro verificado, inventario externo, README/AGENTS/BITACORA
  canónicos, dos documentos de estudio externos, enlaces válidos y operaciones probadas
  con límites explícitos, sin negocio ni push.
- Situación: en curso, DEC-022/023. Operador portable y pruebas4/4 por runtime comprobados;
  falta consolidación documental y revisión final.
