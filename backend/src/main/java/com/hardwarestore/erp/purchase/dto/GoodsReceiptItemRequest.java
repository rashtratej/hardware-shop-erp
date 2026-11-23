
package com.hardwarestore.erp.purchase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record GoodsReceiptItemRequest(

        @NotNull
        UUID purchaseOrderItemId,

        @NotNull
        @DecimalMin("0.001")
        @Digits(integer = 11, fraction = 3)
        BigDecimal receivedQuantity

) {}
