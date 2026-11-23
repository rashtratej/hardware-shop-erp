package com.hardwarestore.erp.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SupplierRequest(

        @NotBlank(message = "Supplier name is required")
        @Size(max = 180, message = "Supplier name cannot exceed 180 characters")
        String name,

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
        String addressLine1,

        @Size(max = 200)
        String addressLine2,

        @Size(max = 100)
        String city,

        @Size(max = 100)
        String state,

        @Pattern(
                regexp = "^$|^[0-9]{2}$",
                message = "State code must contain exactly 2 digits"
        )
        String stateCode,

        @Pattern(
                regexp = "^$|^[0-9]{6}$",
                message = "Postal code must contain exactly 6 digits"
        )
        String postalCode,

        @Size(max = 500)
        String notes
) {
}
