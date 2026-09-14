# Alternativa concreta para decisión — T018

Propuesta del agente, 2026-09-14. **No aplicada al backend.** Se resolvió un POM
separado en .local/corrections/proposal con Maven, sin fuentes ni arranque de la app.
La configuración productiva continúa en Boot3.3.13/Security6.3.10/DataJPA3.3.13,
con mitigación de cabeceras y Tomcat10.1.59 autorizados dentro del stack.

| Componente | Propuesta exacta | Requisito original |
|---|---|---|
| Java | 21 | Se conserva |
| Spring Boot | 3.5.16 | 3.3.x o3.5.x permitido |
| Framework | 6.2.19 (BOM) | 6.1/6.2 permitido |
| Security | 6.5.11 (BOM) | **Excepción: original6.3/6.4** |
| Data JPA | 3.5.13 (BOM2025.0.13) | **Excepción: original3.3** |
| Hibernate | 6.6.53.Final (BOM) | 6.5/6.6 permitido |
| SpringDoc | 2.8.17 | 2.6/2.8 permitido |
| Tomcat core/el/websocket | 10.1.59 (propiedad compartida) | Misma familia Servlet6/Jakarta |

Maven Central confirmó artefactos públicos y la resolución transitiva anterior.
No se necesita comprar soporte para esta alternativa. El BOM3.5.16 por sí solo
trae Tomcat10.1.55; se mantiene la propiedad10.1.59. No se fijan transitivas antiguas
para aparentar cumplimiento. Fuentes: [BOM público](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/3.5.16/spring-boot-dependencies-3.5.16.pom),
[compatibilidad SpringDoc](https://springdoc.org/v2/), [Apache](https://tomcat.apache.org/security-10.html).

Motivo: obtener el conjunto público de parches Spring manteniendo versiones gestionadas.
En Security6.3, Central llega a6.3.10; varios parches de ramas anteriores requieren
Enterprise según avisos oficiales. No se ha supuesto acceso/licencia Enterprise.
La mitigación oficial de CVE-2026-22732 puede mantenerse incluso si cambia el stack,
pero no se presenta como parche de los restantes avisos.

Riesgos: cambios del framework/ORM/seguridad/documentación pueden alterar transacciones,
excepciones, consultas y contrato. La resolución de dependencias sola no demuestra
compatibilidad funcional ni ausencia de avisos aplicables. Se requiere catalogación
posterior y pruebas; no se promete cierre absoluto de seguridad.

Si se autoriza: cambiar parent/POM SpringDoc únicamente a este conjunto, revisar árbol
resuelto y avisos, ejecutar suite Java completa con Oracle, frontend strict, generar
WAR nuevo con hash, probar Basic/CSRF/cabeceras/propiedad/carrera/rollback, OpenAPI/Swagger,
recurrencia UI y Dev Container. Conservar todas las reservas/credenciales y arquitectura.
No resuelve WebLogic/T015; no incluye T023/T024 ni push.

Sin autorización: continuar mitigaciones/parches compatibles y T019–T022; T018 conserva
los puntos no suficientemente tratados como pendientes, sin aceptar riesgo por el usuario.

## Decisión recibida — DEC-019

El usuario decidió mantener el stack obligatorio por ahora, continuar mitigaciones
y dejar pendiente lo no resuelto. Esta alternativa no está autorizada ni aplicada.
