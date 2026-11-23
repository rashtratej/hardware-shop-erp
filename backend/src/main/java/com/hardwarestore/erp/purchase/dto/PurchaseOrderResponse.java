package com.hardwarestore.erp.purchase.dto;

import com.hardwarestore.erp.purchase.entity.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderResponse(
        UUID id,
        String poNumber,
        UUID supplierId,
        String supplierName,
        LocalDate orderDate,
        LocalDate expectedDeliveryDate,
        PurchaseOrderStatus status,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal grandTotal,
        String notes,
        UUID createdBy,
        Instant createdAt,
        List<PurchaseOrderItemResponse> items
) {
}
