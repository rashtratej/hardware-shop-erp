package com.hardwarestore.erp.inventory.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record StockAdjustmentRequest(
        @NotNull UUID productId,

        @NotNull
        @Digits(integer = 11, fraction = 3)
        BigDecimal quantityChange,

        @Size(max = 40)
        String referenceType,

        UUID referenceId,

        @Size(max = 500)
        String notes
) {}