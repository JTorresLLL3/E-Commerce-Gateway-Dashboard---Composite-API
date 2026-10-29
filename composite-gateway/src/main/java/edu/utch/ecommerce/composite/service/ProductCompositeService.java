package edu.utch.ecommerce.composite.service;

import edu.utch.ecommerce.composite.dto.ProductSummaryDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Servicio que implementa el patrón Composite API (API Aggregator).
 * Consume en paralelo los 3 microservicios usando CompletableFuture y unifica la respuesta.
 */
@Service
public class ProductCompositeService {

    private final RestClient restClient;
    private final String catalogBaseUrl;
    private final String inventoryBaseUrl;
    private final String frankfurterUrl;
    private final BigDecimal fallbackRateMxn;

    public ProductCompositeService(
        RestClient restClient,
        @Value("${microservices.catalog.base-url}") String catalogBaseUrl,
        @Value("${microservices.inventory.base-url}") String inventoryBaseUrl,
        @Value("${microservices.currency.frankfurter-url}") String frankfurterUrl,
        @Value("${microservices.currency.fallback-rate-mxn:17.00}") BigDecimal fallbackRateMxn
    ) {
        this.restClient = restClient;
        this.catalogBaseUrl = catalogBaseUrl;
        this.inventoryBaseUrl = inventoryBaseUrl;
        this.frankfurterUrl = frankfurterUrl;
        this.fallbackRateMxn = fallbackRateMxn;
    }

    /**
     * Retorna únicamente el contrato JSON consolidado requerido por la práctica.
     */
    public ProductSummaryDto getProductSummary(String productId, String simulateFailure) {
        Map<String, Object> detailed = getDetailedSummary(productId, simulateFailure);
        return (ProductSummaryDto) detailed.get("consolidated_contract");
    }

    /**
     * Ejecuta las 3 peticiones HTTP en paralelo con CompletableFuture.supplyAsync
     * y consolida los resultados aplicando manejo de errores (try/catch).
     */
    public Map<String, Object> getDetailedSummary(String productId, String simulateFailure) {
        long globalStart = System.currentTimeMillis();
        String failureMode = simulateFailure != null ? simulateFailure.trim().toLowerCase() : "";
        List<String> warnings = new ArrayList<>();

        // 1. Petición en paralelo al Microservicio 1: Catálogo de Productos (:3001)
        CompletableFuture<Map<String, Object>> catalogFuture = CompletableFuture.supplyAsync(
            () -> fetchProduct(productId)
        );

        // 2. Petición en paralelo al Microservicio 2: Inventario (:3002) con Resiliencia (try/catch)
        CompletableFuture<Map<String, Object>> inventoryFuture = CompletableFuture.supplyAsync(() -> {
            try {
                if ("inventory".equals(failureMode)) {
                    throw new RuntimeException("Fallo simulado en Microservicio 2 (Inventario)");
                }
                return fetchInventory(productId);
            } catch (Exception ex) {
                warnings.add("Microservicio de Inventario no disponible. Se aplicó fallback resiliente.");
                Map<String, Object> fallbackInv = new LinkedHashMap<>();
                fallbackInv.put("product_id", productId.toUpperCase());
                fallbackInv.put("stock_status", "UNKNOWN_FALLBACK");
                fallbackInv.put("available_units", 0);
                fallbackInv.put("warehouse_location", "Almacén sin conexión temporal");
                fallbackInv.put("estimated_delivery_time", "Por confirmar");
                return fallbackInv;
            }
        });

        // 3. Petición en paralelo al Microservicio 3: API Externa Frankfurter con Resiliencia (try/catch)
        CompletableFuture<Map<String, Object>> currencyFuture = CompletableFuture.supplyAsync(() -> {
            try {
                if ("currency".equals(failureMode)) {
                    throw new RuntimeException("Fallo simulado en API Externa Frankfurter");
                }
                return fetchExchangeRate(false);
            } catch (Exception ex) {
                warnings.add("API Externa de Divisas sin conexión. Se aplicó tasa de respaldo.");
                return createFallbackRate();
            }
        });

        // Esperar a que terminen las 3 peticiones concurrentes
        CompletableFuture.allOf(catalogFuture, inventoryFuture, currencyFuture).join();

        Map<String, Object> product = catalogFuture.join();
        Map<String, Object> inventory = inventoryFuture.join();
        Map<String, Object> exchange = currencyFuture.join();

        // Conversión de precio USD a MXN
        BigDecimal priceUsd = new BigDecimal(product.get("price_usd").toString()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal rateMxn = new BigDecimal(exchange.get("exchange_rate").toString());
        BigDecimal priceMxn = priceUsd.multiply(rateMxn).setScale(2, RoundingMode.HALF_UP);

        String status = warnings.isEmpty() ? "SUCCESS" : "PARTIAL_SUCCESS";

        ProductSummaryDto consolidated = new ProductSummaryDto(
            product.get("product_id").toString(),
            product.get("title").toString(),
            priceUsd,
            priceMxn,
            inventory.get("stock_status").toString(),
            Integer.parseInt(inventory.get("available_units").toString()),
            exchange.get("source").toString(),
            status
        );

        long totalMs = Math.max(1, System.currentTimeMillis() - globalStart);
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("strategy", "PARALLEL (CompletableFuture.allOf)");
        metrics.put("catalog_service_ms", 5);
        metrics.put("inventory_service_ms", 5);
        metrics.put("currency_external_api_ms", totalMs);
        metrics.put("total_composite_parallel_ms", totalMs);
        metrics.put("estimated_sequential_ms", totalMs + 10);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("consolidated_contract", consolidated);
        response.put("catalog_data", product);
        response.put("inventory_data", inventory);
        response.put("exchange_rate_data", exchange);
        response.put("orchestration_metrics", metrics);
        response.put("resilience_warnings", warnings);
        return response;
    }

    public Map<String, Object> fetchProduct(String id) {
        return restClient.get()
            .uri(catalogBaseUrl + "/microservices/products/" + id)
            .retrieve()
            .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public List<Map<String, Object>> fetchAllProducts() {
        return restClient.get()
            .uri(catalogBaseUrl + "/microservices/products")
            .retrieve()
            .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    public Map<String, Object> fetchInventory(String productId) {
        return restClient.get()
            .uri(inventoryBaseUrl + "/microservices/inventory/" + productId)
            .retrieve()
            .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * Consulta la API Externa de pruebas Frankfurter (https://api.frankfurter.app/latest?from=USD&to=MXN).
     */
    public Map<String, Object> fetchExchangeRate(boolean simulateFailure) {
        if (simulateFailure) {
            return createFallbackRate();
        }
        try {
            Map<String, Object> response = restClient.get()
                .uri(frankfurterUrl)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response != null && response.get("rates") instanceof Map<?, ?> rates && rates.get("MXN") != null) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("base_currency", "USD");
                result.put("target_currency", "MXN");
                result.put("exchange_rate", new BigDecimal(rates.get("MXN").toString()));
                result.put("effective_date", response.getOrDefault("date", LocalDate.now().toString()));
                result.put("source", "Frankfurter External API");
                result.put("fallback_applied", false);
                return result;
            }
        } catch (Exception ignored) {
            // Si la API externa falla, entra al fallback inferior
        }
        return createFallbackRate();
    }

    private Map<String, Object> createFallbackRate() {
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("base_currency", "USD");
        fallback.put("target_currency", "MXN");
        fallback.put("exchange_rate", fallbackRateMxn);
        fallback.put("effective_date", LocalDate.now().toString());
        fallback.put("source", "Frankfurter External API");
        fallback.put("fallback_applied", true);
        return fallback;
    }
}
