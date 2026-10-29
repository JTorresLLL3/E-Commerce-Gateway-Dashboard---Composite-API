package edu.utch.ecommerce.catalog.service;

import edu.utch.ecommerce.catalog.model.Product;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Servicio que gestiona el catálogo de productos con precios base en USD.
 */
@Service
public class CatalogService {

    private static final String INSTANCE_ID = "catalog-service:3001";
    private final Map<String, Product> catalog = new LinkedHashMap<>();

    public CatalogService() {
        register(new Product(
            "P-101",
            "Laptop Gamer XYZ",
            "Laptop de alto rendimiento con procesador de última generación, 32GB RAM DDR5, almacenamiento SSD NVMe de 1TB y tarjeta gráfica dedicada para gaming competitivo y desarrollo de software.",
            "Computadoras y Laptops",
            "XYZ Gaming",
            "SKU-LPT-XYZ-101",
            new BigDecimal("1200.00"),
            4.9,
            List.of("Intel Core i9 14900HX", "32 GB DDR5 5600MHz", "NVIDIA RTX 4070 8GB", "Pantalla 16\" QHD+ 240Hz"),
            INSTANCE_ID
        ));

        register(new Product(
            "P-102",
            "Monitor Curvo UltraWide 34\" WQHD",
            "Monitor ultrapanorámico curvo 1500R con panel IPS Nano Color, tasa de refresco de 165Hz, tiempo de respuesta de 1ms y soporte HDR400.",
            "Monitores y Pantallas",
            "VisionPro",
            "SKU-MON-UW-102",
            new BigDecimal("450.00"),
            4.8,
            List.of("Resolución 3440x1440 WQHD", "165Hz / 1ms GtG", "USB-C Power Delivery 90W", "AMD FreeSync Premium"),
            INSTANCE_ID
        ));

        register(new Product(
            "P-103",
            "Teclado Mecánico RGB Wireless Pro",
            "Teclado mecánico hot-swappable formato 75% con chasis de aluminio CNC, switches táctiles lubricados de fábrica y triple conectividad.",
            "Periféricos",
            "KeyCraft",
            "SKU-KBD-RGB-103",
            new BigDecimal("135.00"),
            4.7,
            List.of("Switches Táctiles Hot-Swap", "Bluetooth 5.1 / 2.4GHz / USB-C", "Batería 4000mAh", "Keycaps PBT Double-Shot"),
            INSTANCE_ID
        ));

        register(new Product(
            "P-104",
            "Tarjeta Gráfica RTX 4080 Super 16GB",
            "Unidad de procesamiento gráfico de arquitectura Ada Lovelace con 16GB GDDR6X, trazado de rayos en tiempo real y DLSS 3.5.",
            "Componentes PC",
            "AetherGPU",
            "SKU-GPU-4080S-104",
            new BigDecimal("999.00"),
            5.0,
            List.of("16 GB GDDR6X 256-bit", "Triple Ventilador Vapor Chamber", "DLSS 3.5 & Ray Tracing", "HDMI 2.1a + 3x DisplayPort 1.4a"),
            INSTANCE_ID
        ));

        register(new Product(
            "P-105",
            "Auriculares Studio ANC Wireless",
            "Auriculares circumaurales con cancelación activa de ruido híbrida, drivers planares de 45mm y audio de alta resolución sin pérdidas.",
            "Audio Profesional",
            "SonicWave",
            "SKU-AUD-ANC-105",
            new BigDecimal("280.00"),
            4.6,
            List.of("Cancelación Activa Híbrida 42dB", "Autonomía de 45 horas", "Codec LDAC / aptX Adaptive", "Micrófono con IA Beamforming"),
            INSTANCE_ID
        ));

        register(new Product(
            "P-106",
            "Silla Ergonómica Ejecutiva Mesh Pro",
            "Silla ergonómica de malla transpirable con soporte lumbar dinámico 4D, reposabrazos ajustables y mecanismo sincrónico.",
            "Mobiliario Oficina",
            "ErgoSeat",
            "SKU-CHR-ERG-106",
            new BigDecimal("320.00"),
            4.5,
            List.of("Soporte Lumbar Dinámico", "Pistón Clase 4 Certificación BIFMA", "Reposabrazos 4D", "Reclinación 135°"),
            INSTANCE_ID
        ));
    }

    private void register(Product product) {
        catalog.put(product.productId().toUpperCase(), product);
    }

    public Optional<Product> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(catalog.get(id.trim().toUpperCase()));
    }

    public Collection<Product> findAll() {
        return catalog.values();
    }
}

