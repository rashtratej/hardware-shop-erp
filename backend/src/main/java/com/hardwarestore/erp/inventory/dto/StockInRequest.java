package com.hardwarestore.erp.inventory.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record StockInRequest(
        @NotNull UUID productId,

        @NotNull
        @DecimalMin("0.001")
        @Digits(integer = 11, fraction = 3)
        BigDecimal quantity,

        @Size(max = 40)
        String referenceType,

        UUID referenceId,

        @Size(max = 500)
        String notes
) {}