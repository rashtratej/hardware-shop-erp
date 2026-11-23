package com.hardwarestore.erp.purchase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemRequest(

        @NotNull(message = "Product ID is required")
        UUID productId,

        @NotNull(message = "Ordered quantity is required")
        @DecimalMin(value = "0.000", inclusive = false,
                message = "Ordered quantity must be greater than zero")
        @Digits(integer = 11, fraction = 3,
                message = "Ordered quantity supports up to 3 decimal places")
        BigDecimal orderedQuantity,

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.00",
                message = "Unit cost cannot be negative")
        @Digits(integer = 12, fraction = 2,
                message = "Unit cost supports up to 2 decimal places")
        BigDecimal unitCost
) {
}
