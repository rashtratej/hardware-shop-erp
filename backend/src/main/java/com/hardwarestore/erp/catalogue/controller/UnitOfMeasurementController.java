package com.hardwarestore.erp.catalogue.controller;

import com.hardwarestore.erp.catalogue.dto.UnitOfMeasurementResponse;
import com.hardwarestore.erp.catalogue.service.UnitOfMeasurementService;
import com.hardwarestore.erp.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/uoms")
public class UnitOfMeasurementController {

    private final UnitOfMeasurementService uomService;

    public UnitOfMeasurementController(
            UnitOfMeasurementService uomService
    ) {
        this.uomService = uomService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<UnitOfMeasurementResponse>>>
    getAllUnits() {

        List<UnitOfMeasurementResponse> units =
                uomService.getAllActiveUnits();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Units of measurement retrieved successfully",
                        units
                )
        );
    }
}
