# Logística de envíos — MVP

MVP académico (Software Modeling) para registrar recolecciones de paquetes.
Spring Boot 3.5 · Java 17 · Spring Web · Spring Data JPA · Thymeleaf · Validation · H2 en memoria.

## Cómo correr

Requisito: JDK 17 o superior. No hace falta instalar Maven, el proyecto trae el wrapper.

```bash
./mvnw spring-boot:run
```

Abrir **http://localhost:8080** (redirige a la pantalla de nueva recolección).

| Pantalla | URL |
|---|---|
| Transacción: nueva recolección | http://localhost:8080/recolecciones/nueva |
| CRUD de remitentes | http://localhost:8080/remitentes |

Para correr las pruebas:

```bash
./mvnw test
```

La base es en memoria: cada arranque vuelve a cargar `src/main/resources/data.sql`,
así que para repetir la demo desde cero basta con reiniciar la aplicación.

## Consola H2

1. Abrir http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:logistica`
3. Usuario: `sa` · Contraseña: *(vacía)*
4. Conectar. Tablas: `ZONA_COBERTURA`, `REMITENTE`, `DESTINATARIO`, `PAQUETE`, `MENSAJERO`, `RECOLECCION`.

## Datos para la demo

El usuario operador que queda registrado en cada recolección es `operador.demo`
(propiedad `logistica.usuario-operador` en `application.properties`, porque no hay login).

### Caso feliz

| Campo | Valor |
|---|---|
| Remitente | **Laura Gómez Rincón** (documento `1010101010`) |
| Paquetes | PKG-1, PKG-2 y PKG-3 |
| Mensajero | Andrés Torres · zona Bogotá |
| Dirección | `Calle 45 # 12-30, Bogotá` (viene precargada) |
| Resultado | Guía **GUIA-000002**, costo total **$ 48.110** |

La guía es la 000002 porque `data.sql` ya trae la GUIA-000001 (de Jorge Castaño).
PKG-3 sirve para explicar RN-06: pesa 10 kg pero su peso volumétrico es 50×40×30/5000 = 12 kg, y se cobra por 12.

### Casos que rompen cada regla

| Regla | Qué hacer en la pantalla | Mensaje esperado |
|---|---|---|
| RN-01 (peso) | Remitente **Carlos Pérez Mejía** (`1020202020`), marcar **PKG-7**, mensajero de Bogotá | `RN-01: el paquete PKG-7 pesa 35 kg y el máximo es 30 kg` |
| RN-01 (medidas) | Remitente **Distribuciones Andina SAS** (`900123456`), marcar **PKG-5**, mensajero Felipe Mora | `RN-01: el paquete PKG-5 mide 160 cm sumando alto + ancho + largo y el máximo es 150 cm` |
| RN-02 | Remitente **Ana María Ruiz** (`1040404040`), marcar **PKG-8** (va a Leticia), mensajero de Bogotá | `RN-02: el paquete PKG-8 va para Rosa Tikuna en la zona Leticia, que no tiene cobertura` |
| RN-03 (inactivo) | Remitente **Pedro Salazar** (`1111111111`, INACTIVO), marcar PKG-18 | `RN-03: el remitente Pedro Salazar está INACTIVO y solo se recoge a remitentes ACTIVO` |
| RN-03 (datos) | Remitente **Papelería El Punto** (`900555666`, sin dirección), marcar PKG-13, escribir una dirección terminada en `, Bogotá` | `RN-03: al remitente Papelería El Punto le falta: dirección` |
| RN-04 | Cualquier remitente válido, no marcar ningún paquete | `RN-04: debe seleccionar al menos un paquete` |
| RN-05 | Hacer el caso feliz y luego, con el botón *Atrás* del navegador, volver a confirmar con los mismos paquetes (o abrir dos pestañas y confirmar en ambas) | `RN-05: el paquete PKG-1 está en estado RECOGIDO y ya pertenece a la guía GUIA-000002; …` |
| RN-07 (zona) | Remitente Laura Gómez, marcar PKG-1, elegir **Felipe Mora** (zona Medellín) | `RN-07: el mensajero Felipe Mora cubre la zona Medellín y la recolección es en Bogotá` |
| RN-07 (dirección) | Borrar la ciudad de la dirección de recolección | `RN-07: la dirección de recolección debe terminar con la ciudad separada por coma…` |

Otros datos útiles:

- **PKG-10** (de Jorge Castaño) está en estado RECOGIDO desde la GUIA-000001: no aparece en la lista
  de pendientes, que es justamente lo que exige RN-05.
- **Sandra Ríos** está EN_RUTA, por eso no sale en el selector de mensajeros disponibles.
  Después del caso feliz, Andrés Torres también desaparece del selector (quedó EN_RUTA).
- **Luisa Fernanda Mora** (`1100110011`) no tiene paquetes: muestra el estado vacío
  "Este remitente no tiene paquetes pendientes de recolección".
- **PKG-15** (de Marcela Ortiz) va a Mitú, la segunda zona sin cobertura.

### CRUD de remitentes

- **Eliminar bloqueado:** Jorge Castaño Vélez tiene una recolección asociada →
  "No se puede eliminar a Jorge Castaño Vélez porque tiene 1 recolección asociada."
- **Eliminar permitido:** crear un remitente nuevo y eliminarlo, o eliminar a Luisa Fernanda Mora
  o a Comercial El Retiro (no tienen recolecciones ni paquetes).
- **Validaciones:** documento repetido (`1010101010`), teléfono con letras o con menos de 7 dígitos,
  nombre o documento vacíos.
- **Paginación:** hay 12 remitentes, así que el listado tiene 2 páginas de 10.

## Trazabilidad de reglas de negocio

Todas las reglas están en `src/main/java/com/logistica/envios/service/RecoleccionService.java`.
El método `confirmarRecoleccion` las ejecuta en orden dentro de una sola transacción (`@Transactional`);
si alguna falla se lanza `NegocioException` con el código de la regla y no se guarda nada.

| Regla | Descripción | Método que la valida |
|---|---|---|
| RN-01 | Ningún paquete supera 30 kg ni 150 cm (alto + ancho + largo) | `validarPesoYMedidas(List<Paquete>)` |
| RN-02 | La zona del destinatario de cada paquete está cubierta | `validarCoberturaDestinos(List<Paquete>)` |
| RN-03 | Remitente ACTIVO con documento, teléfono y dirección | `validarRemitenteHabilitado(Remitente)` |
| RN-04 | Al menos un paquete | `validarAlMenosUnPaquete(List<Paquete>)` |
| RN-05 | Cada paquete REGISTRADO y sin recolección | `validarPaquetesPendientes(List<Paquete>, Remitente)` |
| RN-06 | costoTotal = Σ (tarifaBase + pesoFacturable × valorPorKg); pesoFacturable = max(pesoKg, alto×ancho×largo/5000) | `calcularCostoTotal(List<Paquete>)`, apoyado en `calcularCostoPaquete(Paquete)` y `calcularPesoFacturable(Paquete)` |
| RN-07 | Mensajero DISPONIBLE y con zona igual a la ciudad de la dirección de recolección | `validarMensajeroAsignable(Mensajero, String)` |
| RN-08 | Guía consecutiva GUIA-000001, usuarioOperador y selloTiempo | `registrarGuiaYTrazabilidad(Recoleccion, String)`, apoyado en `generarNumeroGuia()` |

El costo estimado que se ve en pantalla antes de confirmar también sale de `calcularCostoPaquete`
(la API `/api/paquetes` lo devuelve por paquete y la página suma los marcados).

### Pruebas (`RecoleccionServiceTest`, formato Given-When-Then)

| Prueba | Regla |
|---|---|
| `paqueteDe35Kg_rechazaConRN01_yNoCreaLaRecoleccion` | RN-01 |
| `destinatarioEnZonaSinCobertura_rechazaConRN02` | RN-02 |
| `remitenteInactivo_rechazaConRN03` | RN-03 |
| `casoValido_creaRecoleccionConGuia_paquetesRecogidos_yMensajeroEnRuta` | RN-04..RN-08 (caso feliz) |

## Decisiones de diseño a tener en cuenta en el modelo

1. **Asociación Remitente — Paquete.** El modelo no relaciona un paquete con su remitente, pero la
   pantalla debe listar "los paquetes REGISTRADO del remitente" y estos todavía no tienen recolección.
   Por eso `Paquete` tiene además `remitente ManyToOne Remitente` (multiplicidad **Remitente 1 — 0..\* Paquete**).
   Conviene agregar esta asociación al diagrama de clases.
2. **Ciudad de la dirección de recolección (RN-07).** `Recoleccion` solo tiene `direccionRecoleccion`
   como texto, así que la ciudad se toma de lo que va después de la última coma
   (`Calle 45 # 12-30, Bogotá` → `Bogotá`). La comparación con `Mensajero.zona` ignora mayúsculas y tildes.
3. **Eliminar remitente.** Además de bloquearlo cuando tiene recolecciones (lo que pide el enunciado),
   también se bloquea si tiene paquetes registrados, porque si no esos paquetes quedarían sin remitente.
4. **Teléfono del remitente.** En el formulario es opcional, pero si se digita debe tener de 7 a 10 dígitos.
   RN-03 sí lo exige para poder hacer una recolección.
5. **Código del paquete.** `PKG-7` no es un atributo: se arma con `"PKG-" + idPaquete` (método `getCodigo()`).

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/remitentes` | Listado con buscador (`q`) y paginación (`pagina`) |
| GET | `/remitentes/nuevo` | Formulario de creación |
| POST | `/remitentes` | Crea |
| GET | `/remitentes/{id}/editar` | Formulario de edición |
| POST | `/remitentes/{id}` | Actualiza |
| POST | `/remitentes/{id}/eliminar` | Elimina |
| GET | `/recolecciones/nueva` | Pantalla de la transacción |
| POST | `/recolecciones` | Crea la recolección validando las 8 reglas |
| GET | `/recolecciones/{id}` | Comprobante |
| GET | `/api/remitentes?query=` | Buscador JSON |
| GET | `/api/paquetes?remitenteId=` | Paquetes pendientes del remitente, JSON |

## Estructura

```
src/main/java/com/logistica/envios/
├── model/        Entidades y enums del modelo de dominio
├── repository/   Repositorios Spring Data JPA
├── service/      RecoleccionService (RN-01..RN-08) y RemitenteService
├── controller/   Controladores MVC y API JSON
├── dto/          SolicitudRecoleccion, RemitenteResumen, PaqueteResumen
└── exception/    NegocioException
src/main/resources/
├── data.sql      Datos de demostración
├── templates/    Vistas Thymeleaf
└── static/       estilos.css y recoleccion.js
```

## Documentación y entrega

```
DOCUMENTO_ENTREGA_PROYECTO_GRUPAL.md   Documento fuente de la entrega
img/                  Diagramas renderizados, bocetos y capturas que usa el documento
diagramas/mermaid/    Fuentes .mmd (secuencia, actividad) + config.json (líneas en codo)
diagramas/uml/        Diagramas de clases y casos de uso (SVG en HTML)
diagramas/bocetos/    Bocetos de baja fidelidad de las dos pantallas (HTML)
diagramas/drawio/     Mismos diagramas en draw.io (importables en Lucidchart)
docs/                 Guion de la demo y evidencia de pruebas
entrega/              PDF y ZIP generados para entregar
scripts/              generar_entrega.py
```

Para regenerar los diagramas, bocetos, el PDF y el ZIP después de editar una fuente o el documento:

```bash
python3 scripts/generar_entrega.py
```

Necesita `chromium`, `npx` (Node), ImageMagick y `python-markdown-it-py`.
