package edu.utch.ecommerce.inventory.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Modelo de dominio para el estado del inventario y almacén de un producto.
 */
@JsonPropertyOrder({
    "product_id",
    "stock_status",
    "available_units",
    "warehouse_location",
    "aisle_bin",
    "estimated_delivery_time",
    "last_updated",
    "service_instance"
})
public record InventoryItem(
    @JsonProperty("product_id") String productId,
    @JsonProperty("stock_status") String stockStatus,
    @JsonProperty("available_units") int availableUnits,
    @JsonProperty("warehouse_location") String warehouseLocation,
    @JsonProperty("aisle_bin") String aisleBin,
    @JsonProperty("estimated_delivery_time") String estimatedDeliveryTime,
    @JsonProperty("last_updated") String lastUpdated,
    @JsonProperty("service_instance") String serviceInstance
) {}

