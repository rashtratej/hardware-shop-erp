package com.hardwarestore.erp.catalogue.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeProductStatusRequest(

        @NotNull(message = "Active status is required")
        Boolean active

) {
}