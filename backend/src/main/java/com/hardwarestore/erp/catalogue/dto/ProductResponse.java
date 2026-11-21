
package com.hardwarestore.erp.catalogue.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        String hsnCode,
        UUID uomId,
        String uomCode,
        String uomDisplayName,
        UUID gstRateId,
        String gstCode,
        BigDecimal gstRate,
        BigDecimal reorderLevel,
        BigDecimal defaultSalePrice,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}