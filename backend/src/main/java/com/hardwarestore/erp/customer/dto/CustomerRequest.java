package com.hardwarestore.erp.customer.dto;

import com.hardwarestore.erp.customer.entity.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(

        @NotBlank(message = "Customer name is required")
        @Size(max = 180, message = "Customer name cannot exceed 180 characters")
        String name,

        @NotNull(message = "Customer type is required")
        CustomerType customerType,

        @Pattern(
                regexp = "^$|^[0-9]{2}[A-Za-z]{5}[0-9]{4}[A-Za-z][1-9A-Za-z]Z[0-9A-Za-z]$",
                message = "Invalid GSTIN format"
        )
        String gstin,

        @Size(max = 120)
        String contactPerson,

        @Pattern(
                regexp = "^$|^[+]?[0-9 ()-]{7,20}$",
                message = "Invalid phone number"
        )
        String phone,

        @Email(message = "Invalid email address")
        @Size(max = 255)
        String email,

        @Size(max = 200)
        String billingAddressLine1,

        @Size(max = 200)
        String billingAddressLine2,

        @Size(max = 100)
        String billingCity,

        @Size(max = 100)
        String billingState,

        @Pattern(regexp = "^$|^[0-9]{2}$",
                message = "Billing state code must contain exactly 2 digits")
        String billingStateCode,

        @Pattern(regexp = "^$|^[0-9]{6}$",
                message = "Billing postal code must contain exactly 6 digits")
        String billingPostalCode,

        @Size(max = 200)
        String shippingAddressLine1,

        @Size(max = 200)
        String shippingAddressLine2,

        @Size(max = 100)
        String shippingCity,

        @Size(max = 100)
        String shippingState,

        @Pattern(regexp = "^$|^[0-9]{2}$",
                message = "Shipping state code must contain exactly 2 digits")
        String shippingStateCode,

        @Pattern(regexp = "^$|^[0-9]{6}$",
                message = "Shipping postal code must contain exactly 6 digits")
        String shippingPostalCode,

        @Size(max = 500)
        String notes
) {
}
