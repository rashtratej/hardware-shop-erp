
package com.hardwarestore.erp.purchase.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GoodsReceiptItemResponse(
        UUID id,
        UUID purchaseOrderItemId,
        UUID productId,
        String sku,
        String productName,
        String uomCode,
        BigDecimal receivedQuantity
) {}
