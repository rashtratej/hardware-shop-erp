package com.hardwarestore.erp.catalogue.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GstRateResponse(
        UUID id,
        String code,
        String description,
        BigDecimal gstRate,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
