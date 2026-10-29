package edu.utch.ecommerce.catalog.controller;

import edu.utch.ecommerce.catalog.model.Product;
import edu.utch.ecommerce.catalog.service.CatalogService;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del Microservicio 1 (Catálogo de Productos).
 * Endpoint principal: GET /microservices/products/{id}
 */
@RestController
@RequestMapping("/microservices/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final CatalogService catalogService;

    public ProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<Collection<Product>> getAllProducts() {
        return ResponseEntity.ok(catalogService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") String id) {
        return catalogService.findById(id)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "PRODUCT_NOT_FOUND");
                error.put("message", "No se encontró el producto con ID: " + id);
                error.put("product_id", id);
                error.put("service", "catalog-service");
                error.put("timestamp", Instant.now().toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            });
    }
}

