package edu.utch.ecommerce.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio 2 (API Propia - Inventario y Almacén).
 * Expone los endpoints bajo /microservices/inventory/{productId} en el puerto 3002.
 */
@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}

