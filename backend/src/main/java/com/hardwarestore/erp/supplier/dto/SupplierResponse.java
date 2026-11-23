package com.hardwarestore.erp.supplier.dto;

import java.time.Instant;
import java.util.UUID;

public record SupplierResponse(
        UUID id,
        String name,
        String gstin,
        String contactPerson,
        String phone,
        String email,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String stateCode,
        String postalCode,
        String notes,
        boolean active,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {
}
