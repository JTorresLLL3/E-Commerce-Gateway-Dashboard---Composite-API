package edu.utch.ecommerce.inventory.controller;

import edu.utch.ecommerce.inventory.model.InventoryItem;
import edu.utch.ecommerce.inventory.service.InventoryService;
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
 * Controlador REST del Microservicio 2 (Inventario y Almacén).
 * Endpoint principal: GET /microservices/inventory/{productId}
 */
@RestController
@RequestMapping("/microservices/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<Collection<InventoryItem>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.findAll());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getInventoryByProductId(@PathVariable("productId") String productId) {
        return inventoryService.findByProductId(productId)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "INVENTORY_NOT_FOUND");
                error.put("message", "No existe registro de inventario para el producto: " + productId);
                error.put("product_id", productId);
                error.put("service", "inventory-service");
                error.put("timestamp", Instant.now().toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            });
    }
}

