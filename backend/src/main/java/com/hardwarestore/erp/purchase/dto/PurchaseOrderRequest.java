package com.hardwarestore.erp.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderRequest(

        @NotNull(message = "Supplier ID is required")
        UUID supplierId,

        @NotNull(message = "Order date is required")
        LocalDate orderDate,

        LocalDate expectedDeliveryDate,

        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        String notes,

        @NotEmpty(message = "At least one purchase order item is required")
        @Size(max = 100, message = "A purchase order cannot exceed 100 items")
        List<@Valid PurchaseOrderItemRequest> items
) {
}
