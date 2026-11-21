package com.hardwarestore.erp.catalogue.category.dto;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(

        UUID id,

        String name,

        String description,

        UUID parentCategoryId,

        String parentCategoryName,

        boolean active,

        Instant createdAt,

        Instant updatedAt

) {
}
