# E-Commerce Gateway & Dashboard — Composite API (API Aggregator)

**Universidad Tecnológica de Chihuahua (UTCH)**  
**Programa Educativo:** Ingeniería en Desarrollo y Gestión de Software  
**Asignatura:** Desarrollo Web Integral — Unidad 3  
**Práctica:** Implementación de Composite API y Consumo de Microservicios  

---

## 📌 Descripción del Proyecto

Solución Web empresarial desarrollada con **Java 21 LTS** y **Spring Boot 3.3.4** que implementa el patrón de arquitectura **Composite API (API Compuesta / Aggregator)** para optimizar el rendimiento de una aplicación de comercio electrónico y eliminar el antipatrón *Chatty I/O*.

En lugar de obligar al cliente web/móvil a realizar múltiples peticiones HTTP individuales hacia distintos servicios, el **Composite Gateway (`:3000`)** expone un único punto de entrada unificado que orquesta **asíncronamente y en paralelo** (`CompletableFuture.allOf` sobre *Virtual Threads* de Java 21) tres servicios independientes:

1. **Microservicio 1 — Catálogo de Productos (`catalog-service` · Puerto `3001`)**: Gestiona la información básica del producto (título, descripción, categoría, especificaciones y precio base en `USD`).
2. **Microservicio 2 — Inventario y Almacén (`inventory-service` · Puerto `3002`)**: Informa el estado de stock (`IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`), unidades disponibles, ubicación física en CEDIS y tiempo estimado de entrega.
3. **Microservicio 3 — API Externa de Divisas (`Frankfurter API`)**: Consulta en tiempo real la tasa de cambio `USD -> MXN` (`https://api.frankfurter.app/latest?from=USD&to=MXN`) con respaldo secundario (`ExchangeRate-API`) y *fallback* resiliente en caso de desconexión.

---

## 🏗️ Arquitectura de Microservicios y Endpoints

| # | Microservicio / API | Tipo | Protocolo / Formato | Puerto | Endpoint / URL Base | Descripción y Función |
|---|---|---|---|---|---|---|
| **1** | **Catálogo (`catalog-service`)** | Propia | REST / JSON | `3001` | `GET /microservices/products/{id}` | Provee la información general del producto y precio base en USD. |
| **2** | **Inventario (`inventory-service`)** | Propia | REST / JSON | `3002` | `GET /microservices/inventory/{id}` | Informa el stock disponible, ubicación en almacén y tiempo de entrega. |
| **3** | **Divisas (`Frankfurter API`)** | Terceros (Externa) | REST / JSON | `443` | `https://api.frankfurter.app/latest?from=USD&to=MXN` | Entrega el tipo de cambio USD/MXN en tiempo real. |
| **4** | **Composite API (`composite-gateway`)** | Propia (Agregadora) | REST / JSON | `3000` | `GET /api/v1/product-summary/{id}`<br>`GET /api/v1/products/{id}/summary` | Orquesta en paralelo las 3 peticiones, aplica tolerancia a fallos y unifica el payload JSON. |

---

## 📦 Contrato JSON Consolidado

Al consultar `GET http://localhost:3000/api/v1/product-summary/P-101`, la **Composite API** retorna el contrato JSON unificado:

```json
{
  "product_id": "P-101",
  "title": "Laptop Gamer XYZ",
  "price_usd": 1200.00,
  "price_mxn": 20400.00,
  "stock_status": "IN_STOCK",
  "available_units": 15,
  "currency_conversion_source": "Frankfurter External API",
  "status": "SUCCESS"
}
```

---

## 🚀 Requisitos e Instrucciones de Ejecución

### Prerrequisitos
- **Java JDK 21 LTS** (OpenJDK / Temurin 21)
- **Maven Wrapper incluido** (`.\mvnw.cmd` en Windows / `./mvnw` en Linux/macOS)

### Opción A: Inicio Rápido Automatizado (Windows PowerShell)
```powershell
.\start-services.ps1
```

### Opción B: Ejecución Manual por Servicio
1. **Compilar y empaquetar el proyecto:**
   ```powershell
   .\mvnw.cmd clean package
   ```
2. **Iniciar Microservicio 1 (Catálogo - Puerto 3001):**
   ```powershell
   java -jar catalog-service\target\catalog-service-1.0.0.jar
   ```
3. **Iniciar Microservicio 2 (Inventario - Puerto 3002):**
   ```powershell
   java -jar inventory-service\target\inventory-service-1.0.0.jar
   ```
4. **Iniciar Composite API Gateway & Dashboard (Puerto 3000):**
   ```powershell
   java -jar composite-gateway\target\composite-gateway-1.0.0.jar
   ```

### Accesos Web Locales
- **Dashboard Web E-Commerce:** `http://localhost:3000`
- **Consola REST Client de Pruebas:** `http://localhost:3000/rest-client.html`
- **Composite API Endpoint:** `http://localhost:3000/api/v1/product-summary/P-101`

---

## 🛡️ Tolerancia a Fallos y Resiliencia

El servicio agregador (`ProductCompositeService`) encapsula cada llamada asíncrona en bloques `try/catch` independientes dentro de `CompletableFuture.supplyAsync(...)`:
- **Fallo en Microservicio de Inventario (`?simulateFailure=inventory`)**: Retorna estado degradado `PARTIAL_SUCCESS` con `stock_status: "UNKNOWN_FALLBACK"` y `available_units: 0`, manteniendo visible la información y el precio del producto.
- **Fallo en API Externa de Divisas (`?simulateFailure=currency`)**: Aplica *Fallback Resiliente* con tasa de referencia en caché (`17.00 MXN/USD`) sin interrumpir la experiencia de compra.
- **Producto Inexistente (`GET /api/v1/product-summary/P-999`)**: `GlobalExceptionHandler` captura `ProductNotFoundException` y responde `HTTP 404 Not Found` con estructura JSON estandarizada.

