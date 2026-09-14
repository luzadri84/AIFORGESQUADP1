# Criterios de entrega e historial de recepción excluida

Adaptación autocontenida de las secciones 6 y 7 de la guía 07 suministrada; H001
solo incorpora criterios. Tareas/estado únicamente en
[tasks.md](../specs/001-booking-espacios/tasks.md). Este archivo conserva criterios;
la entrega ejecutada se describe en [VERIFICACION_FINAL](VERIFICACION_FINAL.md).

## T013 / H007: explicación final

Crear o reutilizar `docs/EXPLICACION_IMPLEMENTACION.md` durante el desarrollo y
revisarlo contra código/tests al cerrar T013. No presentar el plan teórico como
implementación. Debe cubrir estos doce contenidos:

1. Estado/alcance real: funciones, exclusiones y bloqueos; propuesta frente a ejecución.
2. Ejecución: versiones resueltas, comandos y URLs locales comprobadas; enlazar README.
3. Arquitectura concreta: responsabilidades, dependencias y archivos reales; motivos
   y alternativas según DEC, sin inventar rechazos.
4. Recorrido completo: creación Angular → transacción Oracle → respuesta; listado,
   cancelación y recurrencia, con rutas relativas y métodos reales.
5. Reglas: intervalos, adyacencias, zona, estados, recurrencias parciales y errores
   técnicos; ejemplos coherentes con tests.
6. Seguridad/concurrencia: identidad, propietario, CSRF, secretos, alcance del bloqueo
   y su prueba; distinguir extensiones preparadas de Azure realmente verificado.
7. Pruebas/evidencia: requisito → test/comando → resultado; distinguir qué prueban
   Oracle, mocks y sonda frontend; no inventar mediciones.
8. Proceso con IA: decisiones determinantes con DEC y commits reales; bitácora fuente.
9. Base definitiva y procedencia: describir DEC-004, repositorio actual e historia
   conservada. Starter/defecto/migración: No aplica por cambio de alcance; no simularlos.
10. Al menos 12 preguntas de sustentación basadas en código, respuestas razonadas,
    archivos a mostrar y errores conceptuales frecuentes.
11. Al menos 6 retos plausibles de modificación, analizados sin implementarlos.
12. Límites y mejoras: nueva decisión necesaria, fuera de alcance y sin certificación
    ficticia de producción.

Cada reto debe incluir cambio de comportamiento; regla/endpoint/DTO/UI/SQL afectados;
archivos reales a modificar; riesgos de autorización y concurrencia; casos de prueba;
y secuencia mínima para resolverlo en vivo. Esfuerzo solo orientativo, no medido.
Ejemplos a adaptar al código: duración máxima de dos horas, filtro por espacio sin
perder propietario, prohibir pasado con reloj comprobable, repetición diaria, margen
entre reservas, edición propia. Si alguno ya existe, usar paginación/cancelación de
serie u otro; no preimplementar todas las soluciones ni afirmar que las pedirá el evaluador.

No cerrar T013 hasta cumplir estos criterios, generar/probar el WAR conforme al
alcance acordado y mostrar todos los pendientes externos. Las 12 preguntas y 6 retos
no deben fabricarse en H001 alrededor de archivos que todavía no existen.

## T014 / H008: procedimiento histórico sin vigencia (DEC-004)

**No aplica por cambio de alcance.** Se conserva abajo la descripción de recepción,
comparación, migración e investigación original para trazabilidad. No ejecutar
estos pasos ni marcarlos implementados: el starter no estará disponible y fue excluido.

1. Registrar rama, HEAD, diff pendiente, pruebas y handoff del punto actual. Preservar
   cambios sin reset/stash/borrado automático; punto de recuperación sin secretos.
2. Obtener starter aparte, en carpeta hermana de revisión elegida cuando llegue;
   conservar el provisional y su historial. No copiar .git ni starter sobre el actual.
3. Inspeccionar requisitos, instrucciones, scripts, versiones, contenedores y código
   antes de ejecutar. No ejecutar destrucción de volúmenes; aislar recursos de prueba
   cuando puedan afectar datos existentes.
4. Investigar/reproducir el defecto antes de reemplazar piezas y guardar evidencia
   redactada. No inventar defecto ni atribuirle la contradicción de WebLogic sin prueba.
5. Comparar por componente: aportes del starter, implementación ya válida, adaptaciones
   necesarias y pruebas que se conservan o amplían.
6. Elegir repositorio de integración según evaluador. Si debe ser el starter, trabajar
   en una rama suya y trasladar cambios seleccionados con procedencia explícita;
   conservar historia provisional.
7. Registrar trazabilidad real de portado/cherry-pick: hashes originales y nuevos si
   cambian. No fingir identidad de hashes ni unir historias sin motivo revisado.
8. Corregir el defecto mínimamente; probar regresión antes/después y áreas afectadas.
   Reabrir/añadir tareas si aparecen incompatibilidades, sin borrar evidencia previa.
9. Actualizar README, AGENTS, spec/plan/tasks, bitácora, handoff y explicación para
   describir la integración real; no copiar toda la carpeta de apoyo al entregable.

## T015: aclaración externa separada

Recibir el starter no aprueba Tomcat, no resuelve WebLogic automáticamente y no
permite publicación. Registrar aclaración y evidencia exigida por el evaluador.
T014 no aplica por DEC-004; T015 conserva su bloqueo externo y criterios sin cambios.
No bloquear trabajo local independiente ya autorizado ni anticipar una capa genérica
de compatibilidad. La solución de estudio debe referenciar código/pruebas reales
y sus preguntas/retos son ejercicios de análisis, nunca nuevas funcionalidades.
