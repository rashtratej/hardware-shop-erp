
package com.hardwarestore.erp.purchase.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GoodsReceiptResponse(
        UUID id,
        String receiptNumber,
        UUID purchaseOrderId,
        String poNumber,
        LocalDate receiptDate,
        String supplierDeliveryNote,
        String notes,
        UUID receivedBy,
        Instant createdAt,
        List<GoodsReceiptItemResponse> items
) {}
