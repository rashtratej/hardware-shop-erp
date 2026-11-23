package com.hardwarestore.erp.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
        UUID productId,
        String sku,
        String productName,
        String uomCode,
        BigDecimal quantityOnHand,
        BigDecimal quantityReserved,
        BigDecimal availableQuantity,
        BigDecimal reorderLevel,
        boolean lowStock,
        Instant updatedAt
) {}