# Guía para comprender y defender la solución de Booking

Esta guía explica el diseño propuesto en `01_Plan_Tecnico_Booking_SED.md`. Todavía no describe una implementación ejecutada. Después de desarrollar, sustituir nombres propuestos por los reales y comprobar cada afirmación contra archivos, métodos y pruebas. Poder repetir una explicación no basta: hay que localizar la decisión en código, predecir su comportamiento y modificarla con seguridad.

## 1 El recorrido completo de una reserva

Imagina que Ana quiere una sala el lunes de 10:00 a 11:00 durante cuatro semanas y la segunda ya está ocupada.

1. Ana escribe sus credenciales de desarrollo. Angular consulta `/api/me` con HTTP Basic; Spring Security verifica esas credenciales. Una selección visual de «Ana» no sería autenticación.
2. La pantalla carga espacios y reservas de Ana. La consulta al servidor obtiene el propietario del contexto de seguridad, sin confiar en un `userId` recibido.
3. El cliente obtiene un token CSRF para escribir. Es una protección adicional del navegador, distinta de la contraseña.
4. Ana completa sala, fechas y repetición. Angular puede avisar que fin debe ser posterior a inicio, pero el servidor vuelve a comprobarlo.
5. El interceptor adjunta Basic únicamente a esta API y Angular incluye la cabecera XSRF configurada. No se envían credenciales a servicios externos.
6. Spring Security autentica y comprueba CSRF. El controlador deserializa el DTO, valida campos y entrega al servicio la identidad verificada.
7. El servicio expande cuatro intervalos y, dentro de una transacción, bloquea la fila de la sala. Otra creación para esa misma sala espera.
8. Por cada intervalo, pregunta si hay una reserva ACTIVE que se solape. La segunda se marca rechazada; las demás se acumulan como válidas. También se comprueba que las aceptadas del mismo pedido no choquen entre sí.
9. Se guardan tres filas y se confirma la transacción. La respuesta informa tres creaciones y una colisión. Si ocurre un fallo técnico antes del commit, no deben quedar esas tres filas parcialmente guardadas.
10. Angular muestra el resultado parcial y vuelve a consultar la lista. Ana sabe cuáles quedaron reservadas y cuál necesita reprogramar.

El servidor es la autoridad. Una validación Angular mejora la experiencia, pero se puede eludir llamando directamente a la API. Oracle conserva las reservas entre reinicios y permite coordinar solicitudes concurrentes.

## 2 Qué función cumple cada capa

El controlador conoce HTTP: códigos de estado, JSON, parámetros y usuario autenticado. El servicio conoce el caso de uso: cuándo bloquear, cómo clasificar las ocurrencias y qué se guarda en una transacción. El repositorio conoce el acceso a datos. El motor de intervalos conoce fechas y comparaciones, sin depender de Spring ni Oracle para sus pruebas unitarias.

Esta separación permite cambiar la interfaz sin reescribir la regla de colisiones. También permite probar la matemática del intervalo sin iniciar una aplicación completa. No exige crear una interfaz y una implementación para cada clase.

Un DTO es la forma del mensaje que cruza la API. Una entidad JPA representa persistencia. Separarlos evita que una propiedad agregada a la tabla termine aceptándose automáticamente desde el cliente, y permite excluir propietario, estado e identificador del DTO de creación.

El mapper puede ser un método pequeño. Para seis campos, una biblioteca de mapeo y una jerarquía de objetos no necesariamente aportan valor. El tamaño correcto de la arquitectura es el que permite entender las reglas y probarlas con pocos lugares de cambio.

## 3 Por qué funciona la comparación de horarios

Dos reservas no se cruzan si una termina antes o justo cuando empieza la otra. Las dos formas de estar separadas son `A.fin <= B.inicio` o `B.fin <= A.inicio`. Al negar esa separación se obtiene `A.inicio < B.fin && B.inicio < A.fin`.

El extremo final se excluye. Por eso `[10:00, 11:00)` y `[11:00, 12:00)` son adyacentes. Cambiar `<` por `<=` convierte la adyacencia en colisión e incumple un caso borde explícito.

No basta con preguntar si el comienzo de la nueva reserva está dentro de la existente: una nueva reserva de 09:00 a 12:00 envuelve la de 10:00 a 11:00 y su comienzo no está dentro. La fórmula general contempla ambos extremos y ambas contenciones.

La regla de intervalos no sabe quién es el usuario: dos funcionarios diferentes también pueden chocar. Propietario sirve para permisos y listado; espacio y estado sirven para disponibilidad. Filtrar disponibilidad solo por el usuario actual sería un defecto grave.

## 4 Por qué una transacción no basta

Una transacción agrupa operaciones que deben confirmarse o deshacerse juntas. Esto protege de errores técnicos, pero no implica que otra transacción no observe también un horario libre.

Sin bloqueo, Ana consulta y no encuentra reservas. Bruno consulta antes del commit de Ana y tampoco encuentra. Ambos insertan filas distintas. Ninguno viola una clave única por ID y las dos reservas quedan activas.

Con el bloqueo de la fila de Espacio, Ana adquiere el turno y Bruno espera. Ana consulta, inserta y confirma. Bruno obtiene el bloqueo después y su consulta posterior observa la reserva confirmada. Entonces rechaza la colisión. El bloqueo debe abarcar consulta y escritura; liberarlo antes de guardar reabre la carrera.

Se bloquea Espacio porque existe incluso cuando su agenda está vacía. Un `SELECT FOR UPDATE` sobre una búsqueda de reservas sin resultados no proporciona la fila estable que se necesita bloquear. La semántica efectiva debe probarse con Oracle y transacciones separadas.

Un `synchronized` de Java coordina hilos en una JVM, no varias instancias del backend. Un índice único de espacio e inicio solo impide inicios iguales. `@Version` en dos nuevas reservas tampoco coordina inserciones de filas distintas. Son mecanismos útiles para otros problemas, pero no sustituyen aquí el protocolo elegido.

La compensación es que dos horarios distintos de una misma sala esperan aunque no se crucen. Para una prueba pequeña es más fácil de entender y verificar que un sistema de bloqueos por franjas. Si luego el volumen lo exige, medir contención antes de cambiarlo.

## 5 Aceptación parcial y atomicidad no se contradicen

Aceptación parcial es una decisión de negocio: se consideran válidas tres de cuatro ocurrencias. Atomicidad es una propiedad técnica: esas tres filas se confirman juntas o ninguna se confirma si hay un error de base de datos.

El servicio puede clasificar cuatro intervalos, registrar una colisión como dato y guardar los otros tres dentro de una sola transacción. No necesita cuatro transacciones ni capturar cuatro excepciones. Esto simplifica la implementación y permite informar con precisión lo que pasó.

No hay una regla recurrente que se ejecute cada semana mediante un cron. Las cuatro reservas se materializan al crear. Por eso cada una puede cancelarse individualmente y todas participan de la consulta de colisiones desde el primer momento. Si se quisiera modificar «esta y las siguientes», aparecería la necesidad de un concepto de serie; no hace falta anticiparlo ahora.

El contador incluye la primera fecha. «Cuatro lunes» significa cuatro reservas, no una inicial más cuatro repeticiones. Una recurrencia no limitada podría ocupar recursos de CPU, memoria y bloqueo de base de datos; el límite de 12 controla ese coste. Debe explicarse como decisión propuesta, ajustable mediante una constante o propiedad validada.

## 6 Autenticación, autorización y CSRF

Autenticación responde quién llama. Autorización determina qué puede hacer sobre un recurso. Basic acredita a Ana, pero no le permite cancelar la reserva de Bruno. La búsqueda de cancelación incluye ID y propietario derivado de la identidad del servidor.

Responder 404 ante un recurso ajeno permite no revelar su existencia. Es una decisión de privacidad del contrato; un 403 podría ser válido con otro criterio, pero toda la API y sus pruebas deben ser coherentes. La seguridad real consiste en impedir la operación, no en escoger un código determinado.

Basic codifica usuario y contraseña con Base64; cualquiera que intercepte el valor puede decodificarlo. HTTPS protege el transporte. Mantener la credencial en memoria reduce persistencia accidental, pero no vuelve inocuo un XSS. No registrar Authorization ni introducir la contraseña en comandos que puedan quedar guardados en historial.

CSRF protege frente a solicitudes que otro sitio provoca en un navegador con credenciales que pueden enviarse automáticamente. Declarar el backend stateless no basta para descartar ese riesgo cuando se utiliza Basic. El token XSRF, verificado en operaciones de escritura, complementa la autenticación. Una aplicación de mismo origen simplifica su integración con Angular. Este riesgo de Basic está documentado por [Spring Security](https://docs.spring.io/spring-security/reference/features/exploits/csrf.html).

MSAL y OAuth2 Resource Server son los puntos futuros de entrada de identidad. MSAL obtiene tokens para el cliente; Resource Server verifica tokens recibidos por la API. La regla «solo mis reservas» continúa vigente con Azure. No se sustituye por confiar en un correo enviado desde Angular.

## 7 Fechas y zonas horarias sin ambigüedad

`2026-09-21T10:00:00-05:00` y `2026-09-21T15:00:00Z` describen el mismo instante. Compararlas como strings puede dar resultados erróneos. Se parsean a tipos temporales y se comparan como instantes.

La interfaz expresa horarios de Bogotá, independientemente de dónde esté configurado el navegador. El servidor genera una repetición semanal tomando esa zona, no la hora local del servidor. El mapeo a Oracle se prueba recuperando lo almacenado y comprobando instante y presentación.

No usar fechas fijas que ya pasaron para una demostración de «reservas activas». Preparar datos relativos a una fecha futura o fijar el reloj de las pruebas. Una prueba puede comprobar intervalos pasados si prueba solo colisión; la lista activa, en cambio, depende de la definición temporal acordada.

## 8 Cómo leer el código generado sin perderse

Cuando exista el repositorio, estudiar un flujo cada vez:

1. Abrir el test de creación válida y el de colisión parcial. Identificar entrada, preparación y resultado esperado.
2. Seguir la petición desde el servicio Angular al controlador y al servicio Java.
3. Localizar de dónde sale el propietario y dónde se comprueba CSRF.
4. Encontrar el comienzo de la transacción y la adquisición del bloqueo. Verificar que la consulta ocurre después.
5. Leer la consulta SQL o JPQL y comprobar que filtra espacio, estado y ambos extremos.
6. Seguir la respuesta parcial hasta la pantalla. Confirmar que distingue error técnico de rechazo de negocio.
7. Leer la prueba de cancelación ajena y comprobar el estado persistido tras el intento.
8. Leer la prueba concurrente y explicar por qué usa conexiones independientes.

Por cada archivo crítico escribir tres frases propias: para qué existe, qué regla garantiza y qué test detectaría un error suyo. Si no puede responderse, pedir a Codex una explicación sobre ese archivo específico, no sobre toda la solución a la vez.

## 9 Preguntas de sustentación con respuestas esperadas

| Pregunta | Qué debe contener una respuesta sólida |
|---|---|
| ¿Por qué elegiste esta arquitectura? | Un caso de uso pequeño, límites claros y pruebas; explicar por qué no hacen falta capas genéricas |
| ¿Qué regla exacta impide la colisión? | Comparaciones estrictas, mismo espacio, estado activo y ejemplo de contención |
| ¿Qué ocurre a las 11:00 si una termina y otra empieza? | Se permite por intervalo semiabierto, con test identificado |
| ¿Qué sucede si dos usuarios envían a la vez? | Bloqueo de la fila de Espacio, orden de operaciones y transacciones independientes |
| ¿Basta con @Transactional? | No; agrupa cambios, pero no impide por sí sola la carrera de consultar e insertar |
| ¿Por qué no sincronizar un método Java? | Solo protege una JVM; el sistema puede tener varias instancias |
| ¿Cómo sabes que la prueba de concurrencia sirve? | Solapa transacciones, empieza con espacio vacío y verifica filas finales en Oracle |
| ¿Qué pasa con una serie de cuatro y una colisión? | Tres creaciones, rechazo identificado, respuesta parcial y actualización de lista |
| ¿Y si Oracle falla al insertar la tercera? | Rollback del pedido, error técnico y ninguna promesa de creación parcial |
| ¿Cómo evitas que un usuario cambie userId? | El DTO no lo admite; identidad del principal, validación del contrato y consulta por propietario |
| ¿Dónde está la autorización real? | Backend y consulta de recurso, no botones o guard de Angular |
| ¿Por qué respondes 404 para una reserva ajena? | No revelar existencia y mantener una política coherente |
| ¿Qué ocurre al cancelar dos veces? | 204 para la propia ya cancelada; estado consistente y sin exposición a otros |
| ¿Basic cifra la contraseña? | No; Base64, TLS para transporte y credencial temporal en memoria |
| ¿Una API stateless necesita CSRF? | Puede necesitarlo con Basic en navegador; explicar token y prueba negativa |
| ¿Qué probaste realmente de Azure? | Solo lo comprobado: dependencia, adaptador y selección de modo; no inventar login real |
| ¿Por qué Oracle y no una base más fácil? | Es un requisito; además se necesita validar su bloqueo y tipos reales |
| ¿Por qué un WAR no prueba WebLogic? | Empaquetado y compatibilidad de runtime son cosas distintas; señalar incompatibilidad documentada |
| ¿Cómo verificaste una dependencia sugerida por IA? | Fuente, versión resuelta, peers o árbol, build y revisión de seguridad |
| ¿Qué propuesta de la IA rechazaste? | Un hecho real de la bitácora, razón y prueba; no una anécdota preparada que no ocurrió |
| ¿Qué no implementaste y por qué? | Exclusiones concretas ligadas al alcance y a las seis horas |
| ¿Qué límite reconoces? | Contención por sala, recurrencia simple, ausencia de validación WebLogic/Azure si sigue pendiente |

Las respuestas deben acompañarse de archivos y tests reales. Si el código final tomó otra decisión razonada, se debe defender esa decisión, no memorizar esta tabla.

## 10 Cambios que podrían pedir en vivo

Practicar cada reto con una rama de ensayo o un cambio reversible. Primero describir el comportamiento, escribir o ajustar un caso que lo demuestre, implementar el mínimo, ejecutar pruebas relacionadas y explicar límites. No introducir todos estos cambios en la entrega inicial.

| Reto | Tiempo de ensayo | Cambios mínimos | Pruebas que importan |
|---|---:|---|---|
| Ampliar límite de 12 a 20 ocurrencias | 5 min | Validación, constante/configuración, mensaje UI y documentación | 20 permitido, 21 rechazado; generación acotada |
| Agregar filtro por espacio en mis reservas | 10 min | Parámetro opcional y filtro adicional en consulta y UI | Mantener propietario incluso con filtro; otro usuario no aparece |
| Prohibir reservas pasadas | 10 min | Regla en servicio con Clock, mensaje de validación | Antes, exactamente ahora y después según política explícita |
| Duración máxima de dos horas | 10 min | Regla de duración y feedback de formulario | 120 minutos válidos y 121 inválidos, también recurrentes |
| Exigir diez minutos entre reservas | 15 min | Ajustar disponibilidad y contrato de separación | Nueve minutos rechaza; diez permite; ambos sentidos |
| Permitir repetición diaria | 15 min | Nuevo valor de frecuencia y expansión por días | Cambio de mes, número exacto y colisión en ocurrencia intermedia |
| Editar horario de una reserva propia | 20 min | DTO y endpoint de edición, propiedad, bloqueo, excluir ID propio | Ajena rechazada, mismo intervalo permitido, conflicto con otra rechazado |
| Cancelar toda una serie | 20 min | Persistir series identificables y definir alcance | Propiedad de toda la operación, cancelación repetida y ocurrencias ya canceladas |
| Dos usuarios compiten y uno espera demasiado | 15 min | Manejo del timeout real y respuesta temporal | Error no produce doble reserva ni se confunde con colisión |
| Paginación de mis reservas | 15 min | page/size acotados, orden estable y navegación | Sin reservas ajenas, tamaño máximo y orden de empates |

En edición, si cambia el espacio, adquirir los bloqueos de origen y destino en un orden estable de IDs para reducir riesgo de deadlock. No cancelar primero y crear después sin una transacción, porque un fallo podría dejar al usuario sin su reserva original.

Al pedir tiempo de preparación o limpieza, aclarar si es separación total entre reservas o expansión antes y después de cada una: expandir ambos extremos puede duplicar el margen. Al pedir cancelación de serie, aclarar «todas», «futuras» o «esta y las siguientes» antes de escoger una semántica.

## 11 Ensayo de una sustentación de 45 minutos

Propuesta personal de preparación, no agenda oficial del panel: cinco minutos de problema y alcance; ocho de demo; diez de decisiones y pruebas; diecisiete de reto de modificación; cinco de límites y preguntas. Adaptarse al orden que establezca el entrevistador.

Para la demo, tener una sala libre y dos identidades locales. Crear una reserva; mostrar colisión parcial; crear una adyacente; mostrar una serie con conflicto intermedio; cambiar de identidad para comprobar privacidad; cancelar con el propietario y volver a reservar el horario. No mostrar contraseñas en logs o pantalla compartida innecesariamente.

Preparar una explicación de dos minutos con palabras propias: «La aplicación materializa reservas y consulta las propias. El servidor autentica, valida propietario y controla colisiones. Uso intervalos semiabiertos para permitir adyacencias. Bloqueo la sala dentro de la transacción para evitar doble reserva concurrente. Cada recurrencia devuelve resultados por ocurrencia. Separé errores técnicos de rechazos de negocio. Las pruebas comprueban fechas, seguridad y persistencia. Estas son las limitaciones realmente pendientes».

## 12 Cómo usar Codex para aprender después de implementar

Pedir primero una explicación sin cambios: «Sigue una reserva desde Angular hasta Oracle. Cita los archivos y métodos reales y señala dónde se garantiza cada regla. Si un punto del plan no está implementado, dilo».

Después: «Hazme una pregunta por vez sobre este repositorio. Espera mi respuesta, evalúa su precisión con evidencia del código y haz una repregunta. No me des la respuesta antes de intentarlo».

Para ensayo de cambios: «Propón un reto de 15 minutos compatible con el dominio. No escribas el código por mí. Evalúa mi solución con los criterios de comportamiento, seguridad, pruebas y simplicidad». En una sesión diferente puede pedirse ayuda para estudiar lo que faltó.

La candidata debe poder señalar una decisión que tomó, una sugerencia que rechazó y una comprobación que cambió su diseño. Esa comprensión se construye durante el desarrollo, no al memorizar una explicación generada al final.
