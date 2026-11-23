package com.hardwarestore.erp.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID productId,
        String sku,
        String movementType,
        BigDecimal quantityChange,
        String referenceType,
        UUID referenceId,
        String notes,
        UUID createdBy,
        Instant createdAt
) {}