package edu.utch.ecommerce.composite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la Composite API (API Compuesta / Aggregator).
 * Se ejecuta en el puerto 3000.
 */
@SpringBootApplication
public class CompositeGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompositeGatewayApplication.class, args);
    }
}
