package edu.utch.ecommerce.catalog.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.util.List;

/**
 * Modelo de dominio para la información básica de un producto en el catálogo.
 */
@JsonPropertyOrder({
    "product_id",
    "title",
    "description",
    "category",
    "brand",
    "sku",
    "price_usd",
    "rating",
    "specs",
    "service_instance"
})
public record Product(
    @JsonProperty("product_id") String productId,
    @JsonProperty("title") String title,
    @JsonProperty("description") String description,
    @JsonProperty("category") String category,
    @JsonProperty("brand") String brand,
    @JsonProperty("sku") String sku,
    @JsonProperty("price_usd") BigDecimal priceUsd,
    @JsonProperty("rating") double rating,
    @JsonProperty("specs") List<String> specs,
    @JsonProperty("service_instance") String serviceInstance
) {}

