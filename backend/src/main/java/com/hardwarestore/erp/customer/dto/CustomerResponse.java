package com.hardwarestore.erp.customer.dto;

import com.hardwarestore.erp.customer.entity.CustomerType;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        CustomerType customerType,
        String gstin,
        String contactPerson,
        String phone,
        String email,

        String billingAddressLine1,
        String billingAddressLine2,
        String billingCity,
        String billingState,
        String billingStateCode,
        String billingPostalCode,

        String shippingAddressLine1,
        String shippingAddressLine2,
        String shippingCity,
        String shippingState,
        String shippingStateCode,
        String shippingPostalCode,

        String notes,
        boolean active,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {
}
