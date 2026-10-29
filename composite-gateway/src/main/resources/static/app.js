let selectedProductId = "P-101";
let selectedResilienceMode = "";

const moneyFormatterUsd = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 2
});

const moneyFormatterMxn = new Intl.NumberFormat("es-MX", {
    style: "currency",
    currency: "MXN",
    minimumFractionDigits: 2
});

async function loadProductSummary() {
    const query = selectedResilienceMode ? `?simulateFailure=${encodeURIComponent(selectedResilienceMode)}` : "";
    const summaryUrl = `/api/v1/product-summary/${selectedProductId}${query}`;
    const detailedUrl = `/api/v1/product-summary/${selectedProductId}/detailed${query}`;

    document.getElementById("currentEndpointUrl").textContent = summaryUrl;

    try {
        const [summaryResp, detailedResp] = await Promise.all([
            fetch(summaryUrl),
            fetch(detailedUrl)
        ]);

        const summaryJson = await summaryResp.json();
        const statusPill = document.getElementById("compositeHttpStatus");

        document.getElementById("consolidatedJsonViewer").textContent = JSON.stringify(summaryJson, null, 2);

        if (!summaryResp.ok) {
            statusPill.textContent = `HTTP ${summaryResp.status} NOT FOUND`;
            statusPill.classList.add("error");
            renderNotFoundState(summaryJson);
            return;
        }

        statusPill.textContent = `HTTP ${summaryResp.status} OK`;
        statusPill.classList.remove("error");

        const detailedJson = await detailedResp.json();
        renderDashboard(summaryJson, detailedJson);
    } catch (error) {
        document.getElementById("consolidatedJsonViewer").textContent = JSON.stringify({
            error: "NETWORK_ERROR",
            message: error.message
        }, null, 2);
    }
}

function renderNotFoundState(errorPayload) {
    const banner = document.getElementById("resilienceBanner");
    banner.className = "resilience-banner error-mode";
    banner.textContent = `Manejo de Excepción Activo (HTTP 404): ${errorPayload.message || "Producto no encontrado"}`;

    document.getElementById("prodTitle").textContent = `Producto ${selectedProductId} No Encontrado`;
    const badge = document.getElementById("stockBadge");
    badge.textContent = "NOT_FOUND (404)";
    badge.className = "stock-badge out-stock";

    document.getElementById("prodGlobalStatus").textContent = "ERROR (404)";
    document.getElementById("prodGlobalStatus").className = "status-text-warn";
    document.getElementById("ms1JsonBox").textContent = JSON.stringify(errorPayload, null, 2);
    document.getElementById("ms2JsonBox").textContent = "// Petición cancelada por producto inexistente";
}

function renderDashboard(summary, detailed) {
    const catalog = detailed.catalog_data;
    const inventory = detailed.inventory_data;
    const rate = detailed.exchange_rate_data;
    const metrics = detailed.orchestration_metrics;
    const warnings = detailed.resilience_warnings || [];

    const banner = document.getElementById("resilienceBanner");
    if (warnings.length > 0) {
        banner.className = "resilience-banner";
        banner.textContent = `Resiliencia / Tolerancia a Fallos Activa (try/catch): ${warnings.join(" | ")}`;
    } else {
        banner.className = "resilience-banner hidden";
    }

    document.getElementById("prodTitle").textContent = summary.title;
    document.getElementById("prodIdChip").textContent = `ID: ${summary.product_id}`;
    document.getElementById("prodCategory").textContent = catalog.category;
    document.getElementById("prodBrand").textContent = `Marca: ${catalog.brand}`;
    document.getElementById("prodSku").textContent = catalog.sku;
    document.getElementById("prodDescription").textContent = catalog.description;

    const badge = document.getElementById("stockBadge");
    badge.textContent = summary.stock_status;
    if (summary.stock_status === "IN_STOCK") {
        badge.className = "stock-badge in-stock";
    } else if (summary.stock_status === "LOW_STOCK") {
        badge.className = "stock-badge low-stock";
    } else {
        badge.className = "stock-badge out-stock";
    }

    document.getElementById("prodPriceMxn").innerHTML = `${moneyFormatterMxn.format(summary.price_mxn)} <small>MXN</small>`;
    document.getElementById("prodPriceUsd").innerHTML = `${moneyFormatterUsd.format(summary.price_usd)} <small>USD</small>`;
    document.getElementById("prodConversionSource").textContent = `Fuente: ${summary.currency_conversion_source}`;
    document.getElementById("prodExchangeRate").textContent = `Tasa: 1 USD = ${rate.exchange_rate} MXN (${rate.effective_date})`;

    document.getElementById("prodUnits").textContent = `${summary.available_units} unidades`;
    document.getElementById("prodWarehouse").textContent = inventory.warehouse_location;
    document.getElementById("prodDelivery").textContent = inventory.estimated_delivery_time;

    const statusEl = document.getElementById("prodGlobalStatus");
    statusEl.textContent = summary.status;
    statusEl.className = summary.status === "SUCCESS" ? "status-text-success" : "status-text-warn";

    const specsList = document.getElementById("prodSpecs");
    specsList.innerHTML = "";
    (catalog.specs || []).forEach(spec => {
        const li = document.createElement("li");
        li.textContent = spec;
        specsList.appendChild(li);
    });

    // Actualizar barras de tiempo de los 3 microservicios
    const ms1 = Math.max(1, metrics.catalog_service_ms || 1);
    const ms2 = Math.max(1, metrics.inventory_service_ms || 1);
    const ms3 = Math.max(1, metrics.currency_external_api_ms || 1);
    const maxTime = Math.max(ms1, ms2, ms3, 20);

    document.getElementById("ms1Time").textContent = `${ms1} ms`;
    document.getElementById("ms2Time").textContent = `${ms2} ms`;
    document.getElementById("ms3Time").textContent = `${ms3} ms`;

    document.getElementById("ms1Bar").style.width = `${Math.min(100, Math.max(12, (ms1 / maxTime) * 100))}%`;
    document.getElementById("ms2Bar").style.width = `${Math.min(100, Math.max(12, (ms2 / maxTime) * 100))}%`;
    document.getElementById("ms3Bar").style.width = `${Math.min(100, Math.max(12, (ms3 / maxTime) * 100))}%`;

    document.getElementById("totalParallelTime").textContent = `${metrics.total_composite_parallel_ms} ms`;
    document.getElementById("totalSequentialTime").textContent = `${metrics.estimated_sequential_ms} ms`;

    // Actualizar paneles inferiores con los JSON de cada microservicio
    document.getElementById("ms1JsonBox").textContent = JSON.stringify(catalog, null, 2);
    document.getElementById("ms2JsonBox").textContent = JSON.stringify(inventory, null, 2);
    document.getElementById("ms3JsonBox").textContent = JSON.stringify({
        amount: 1.0,
        base: rate.base_currency,
        date: rate.effective_date,
        rates: {
            MXN: rate.exchange_rate
        },
        source: rate.source
    }, null, 2);
}

document.querySelectorAll(".prod-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".prod-btn").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        selectedProductId = btn.getAttribute("data-id");
        loadProductSummary();
    });
});

document.querySelectorAll(".res-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".res-btn").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        selectedResilienceMode = btn.getAttribute("data-mode");
        loadProductSummary();
    });
});

document.getElementById("refreshAllBtn").addEventListener("click", () => {
    loadProductSummary();
});

window.addEventListener("DOMContentLoaded", () => {
    loadProductSummary();
});

