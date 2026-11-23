package com.hardwarestore.erp.purchase.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemResponse(
        UUID id,
        UUID productId,
        String sku,
        String productName,
        String hsnCode,
        String uomCode,
        BigDecimal orderedQuantity,
        BigDecimal receivedQuantity,
        BigDecimal outstandingQuantity,
        BigDecimal unitCost,
        BigDecimal gstRate,
        BigDecimal taxableAmount,
        BigDecimal taxAmount,
        BigDecimal lineTotal
) {
}
