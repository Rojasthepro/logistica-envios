# Guion de video — Logística de Envíos

Duración objetivo: 4:30. Narración pendiente de grabar en Voice Studio. Grabar la pantalla siguiendo cada bloque.

## 0:00–0:25 | Presentación
**Pantalla:** abrir la app en `http://localhost:8080`.
**Voz:** Hola. En este video presento el MVP del Sistema de Logística de Envíos, desarrollado con Java, Spring Boot, Thymeleaf y una base H2 en memoria. El alcance incluye el registro transaccional de recolecciones y la gestión de remitentes.

## 0:25–0:55 | Modelo y reglas
**Pantalla:** mostrar brevemente el diagrama `img/diagrama_clases.png` y la sección de reglas del documento de entrega.
**Voz:** El modelo relaciona remitentes, paquetes, recolecciones, mensajeros, destinatarios y zonas de cobertura. La operación aplica ocho reglas de negocio, entre ellas límites físicos, cobertura, disponibilidad del mensajero, cálculo de tarifa y trazabilidad.

## 0:55–1:55 | Caso feliz y tarifa
**Pantalla:** ir a `/recolecciones/nueva`; elegir Laura Gómez, marcar PKG-1, PKG-2 y PKG-3; señalar el peso facturable de PKG-3; elegir Andrés Torres y confirmar. Mantener visible el comprobante.
**Voz:** Para el caso exitoso selecciono a Laura Gómez y sus tres paquetes pendientes. El sistema actualiza la lista al elegir el remitente. En PKG-3, el peso real es diez kilos, pero el peso volumétrico es doce, así que la tarifa se calcula con doce kilos. Asigno al mensajero Andrés Torres, de Bogotá, y confirmo la recolección.

## 1:55–2:20 | Guía y auditoría
**Pantalla:** destacar número de guía, costo total, operador, sello de tiempo y estados del comprobante.
**Voz:** El comprobante muestra la guía GUIA-000002, el costo total liquidado, el operador y el sello de tiempo. La confirmación también cambia los paquetes a RECOGIDO y al mensajero a EN_RUTA dentro de la misma transacción.

## 2:20–3:15 | Validaciones de negocio
**Pantalla:** volver a nueva recolección. Mostrar Carlos Pérez con PKG-7 para RN-01; Ana María Ruiz con PKG-8 para RN-02; Pedro Salazar para RN-03; por último Laura con Felipe Mora para RN-07. No hace falta completar una operación válida.
**Voz:** El sistema bloquea paquetes que superan treinta kilos o ciento cincuenta centímetros en la suma de sus dimensiones. También impide despachar a zonas sin cobertura, procesar remitentes inactivos y asignar mensajeros de una zona distinta. Los errores identifican la regla infringida y preservan la integridad de la operación.

## 3:15–4:00 | CRUD de remitentes
**Pantalla:** abrir `/remitentes`, mostrar búsqueda y paginación; intentar eliminar a Jorge Castaño y mostrar el aviso; crear y borrar un remitente de demostración, o explicar la validación sin alterar datos si se prefiere conservar la base limpia.
**Voz:** El módulo de remitentes permite consultar, buscar, paginar, crear y editar registros. La eliminación protege la integridad histórica: por ejemplo, Jorge Castaño no se puede eliminar porque tiene una recolección asociada. Los formularios validan los datos antes de guardar.

## 4:00–4:30 | Pruebas y cierre
**Pantalla:** terminal con `./mvnw test` ejecutado y resultado visible.
**Voz:** Finalmente, las pruebas automatizadas cubren rechazos por reglas de negocio y el caso exitoso de extremo a extremo del servicio. Este MVP demuestra el flujo principal de recolección, sus validaciones y la trazabilidad de cada guía. Gracias.

## Preparación de grabación
1. Ejecutar `./mvnw spring-boot:run` y dejar la aplicación lista.
2. Grabar a 1080p, ocultando notificaciones y manteniendo el cursor pausado sobre cada dato que se explique.
3. Seguir los bloques anteriores; hacer pausas de dos segundos al cambiar de pantalla.
4. Ejecutar `./mvnw test` antes de grabar el último bloque y dejar el resultado visible.
5. Exportar la grabación sin música. En Voice Studio, generar la narración con la voz autorizada del estudiante, alinear cada bloque con sus marcas de tiempo y exportar el video final.

**Nota de demo:** la base H2 se reinicia con cada arranque. Reiniciar la app antes de la toma del caso feliz para que la guía sea GUIA-000002. Para conservar los datos de demo, omitir el alta/baja de remitente y limitarse a mostrar el bloqueo de eliminación.
