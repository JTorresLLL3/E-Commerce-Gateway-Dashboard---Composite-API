package edu.utch.ecommerce.composite.controller;

import edu.utch.ecommerce.composite.dto.ProductSummaryDto;
import edu.utch.ecommerce.composite.service.ProductCompositeService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador principal de la Composite API.
 * Expone el endpoint unificado GET /api/v1/product-summary/{id}.
 */
@RestController
@CrossOrigin(origins = "*")
public class ProductCompositeController {

    private final ProductCompositeService compositeService;

    public ProductCompositeController(ProductCompositeService compositeService) {
        this.compositeService = compositeService;
    }

    /**
     * Endpoint principal del Composite API que retorna el contrato JSON consolidado.
     */
    @GetMapping({
        "/api/v1/product-summary/{id}",
        "/api/v1/products/{id}/summary"
    })
    public ResponseEntity<?> getProductSummary(
        @PathVariable("id") String id,
        @RequestParam(value = "simulateFailure", required = false) String simulateFailure
    ) {
        try {
            ProductSummaryDto summary = compositeService.getProductSummary(id, simulateFailure);
            return ResponseEntity.ok(summary);
        } catch (Exception ex) {
            return buildNotFoundResponse(id);
        }
    }

    /**
     * Endpoint de apoyo para alimentar la vista detallada del Dashboard Web.
     */
    @GetMapping({
        "/api/v1/product-summary/{id}/detailed",
        "/api/v1/products/{id}/summary/detailed"
    })
    public ResponseEntity<?> getDetailedProductSummary(
        @PathVariable("id") String id,
        @RequestParam(value = "simulateFailure", required = false) String simulateFailure
    ) {
        try {
            return ResponseEntity.ok(compositeService.getDetailedSummary(id, simulateFailure));
        } catch (Exception ex) {
            return buildNotFoundResponse(id);
        }
    }

    @GetMapping("/microservices/products")
    public ResponseEntity<List<Map<String, Object>>> getAllProducts() {
        return ResponseEntity.ok(compositeService.fetchAllProducts());
    }

    @GetMapping("/microservices/products/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") String id) {
        try {
            return ResponseEntity.ok(compositeService.fetchProduct(id));
        } catch (Exception ex) {
            return buildNotFoundResponse(id);
        }
    }

    @GetMapping("/microservices/inventory/{productId}")
    public ResponseEntity<Map<String, Object>> getInventoryByProductId(@PathVariable("productId") String productId) {
        return ResponseEntity.ok(compositeService.fetchInventory(productId));
    }

    @GetMapping("/api/v1/exchange-rate")
    public ResponseEntity<Map<String, Object>> getExchangeRate(
        @RequestParam(value = "simulateFailure", required = false, defaultValue = "false") boolean simulateFailure
    ) {
        return ResponseEntity.ok(compositeService.fetchExchangeRate(simulateFailure));
    }

    private ResponseEntity<Map<String, Object>> buildNotFoundResponse(String productId) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("product_id", productId);
        error.put("error", "PRODUCT_NOT_FOUND");
        error.put("message", "No se encontró el producto con ID: " + productId);
        error.put("status", "ERROR");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
