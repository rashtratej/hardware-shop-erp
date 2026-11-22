
package com.hardwarestore.erp.catalogue.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequest(

        @NotBlank
        @Size(max = 255)
        String sku,

        @NotBlank
        @Size(max = 180)
        String name,

        String description,

        @NotBlank
        @Size(max = 12)
        String hsnCode,

        UUID categoryId,

        @NotNull
        UUID uomId,

        @NotNull
        UUID gstRateId,

        @NotNull
        @DecimalMin("0.000")
        @Digits(integer = 11, fraction = 3)
        BigDecimal reorderLevel,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 12, fraction = 2)
        BigDecimal defaultSalePrice

) {
}
