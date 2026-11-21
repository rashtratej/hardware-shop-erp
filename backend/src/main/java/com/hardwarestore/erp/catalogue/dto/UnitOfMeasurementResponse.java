package com.hardwarestore.erp.catalogue.dto;

import java.util.UUID;

public record UnitOfMeasurementResponse(
        UUID id,
        String code,
        String displayName,
        short quantityScale
) {
}
