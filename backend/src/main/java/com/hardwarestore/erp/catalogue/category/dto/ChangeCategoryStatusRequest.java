package com.hardwarestore.erp.catalogue.category.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeCategoryStatusRequest(

        @NotNull(message = "Active status is required")
        Boolean active

) {
}
