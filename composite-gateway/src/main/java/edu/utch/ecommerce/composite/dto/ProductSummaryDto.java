package edu.utch.ecommerce.composite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;

/**
 * Contrato JSON consolidado de la Composite API.
 */
@JsonPropertyOrder({
    "product_id",
    "title",
    "price_usd",
    "price_mxn",
    "stock_status",
    "available_units",
    "currency_conversion_source",
    "status"
})
public record ProductSummaryDto(
    @JsonProperty("product_id") String productId,
    @JsonProperty("title") String title,
    @JsonProperty("price_usd") BigDecimal priceUsd,
    @JsonProperty("price_mxn") BigDecimal priceMxn,
    @JsonProperty("stock_status") String stockStatus,
    @JsonProperty("available_units") int availableUnits,
    @JsonProperty("currency_conversion_source") String currencyConversionSource,
    @JsonProperty("status") String status
) {}
