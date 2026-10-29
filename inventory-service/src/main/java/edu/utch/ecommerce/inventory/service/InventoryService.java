package edu.utch.ecommerce.inventory.service;

import edu.utch.ecommerce.inventory.model.InventoryItem;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de gestionar la disponibilidad de stock, ubicación física
 * en almacén y tiempos estimados de entrega por producto.
 */
@Service
public class InventoryService {

    private static final String INSTANCE_ID = "inventory-service:3002";
    private final Map<String, InventoryItem> inventory = new LinkedHashMap<>();

    public InventoryService() {
        register(new InventoryItem(
            "P-101",
            "IN_STOCK",
            15,
            "CEDIS Chihuahua Norte - Nave A",
            "Pasillo 04 - Estante B12",
            "24 a 48 horas (Envío Express)",
            "2026-10-28T16:30:00Z",
            INSTANCE_ID
        ));

        register(new InventoryItem(
            "P-102",
            "IN_STOCK",
            8,
            "CEDIS Monterrey Hub - Nave C",
            "Pasillo 09 - Estante A03",
            "2 a 3 días hábiles",
            "2026-10-28T16:45:00Z",
            INSTANCE_ID
        ));

        register(new InventoryItem(
            "P-103",
            "IN_STOCK",
            42,
            "CEDIS Chihuahua Norte - Nave B",
            "Pasillo 01 - Estante D07",
            "24 horas (Entrega Local)",
            "2026-10-28T17:00:00Z",
            INSTANCE_ID
        ));

        register(new InventoryItem(
            "P-104",
            "LOW_STOCK",
            3,
            "CEDIS Guadalajara Occidente - Nave A",
            "Pasillo 12 - Bóveda Alta Seguridad",
            "3 a 5 días hábiles",
            "2026-10-28T17:10:00Z",
            INSTANCE_ID
        ));

        register(new InventoryItem(
            "P-105",
            "IN_STOCK",
            27,
            "CEDIS Ciudad de México Central - Nave D",
            "Pasillo 06 - Estante C15",
            "2 a 4 días hábiles",
            "2026-10-28T17:15:00Z",
            INSTANCE_ID
        ));

        register(new InventoryItem(
            "P-106",
            "OUT_OF_STOCK",
            0,
            "CEDIS Chihuahua Norte - Nave A",
            "Pasillo 15 - Zona de Voluminosos",
            "Sin disponibilidad (Reabastecimiento en 7 días)",
            "2026-10-28T17:20:00Z",
            INSTANCE_ID
        ));
    }

    private void register(InventoryItem item) {
        inventory.put(item.productId().toUpperCase(), item);
    }

    public Optional<InventoryItem> findByProductId(String productId) {
        if (productId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(inventory.get(productId.trim().toUpperCase()));
    }

    public Collection<InventoryItem> findAll() {
        return inventory.values();
    }
}

