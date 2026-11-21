package com.hardwarestore.erp.catalogue.controller;

import com.hardwarestore.erp.catalogue.dto.GstRateResponse;
import com.hardwarestore.erp.catalogue.service.GstRateService;
import com.hardwarestore.erp.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gst-rates")
public class GstRateController {

    private final GstRateService gstRateService;

    public GstRateController(GstRateService gstRateService) {
        this.gstRateService = gstRateService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<GstRateResponse>>>
    getAllActiveRates() {

        List<GstRateResponse> rates =
                gstRateService.getAllActiveRates();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "GST rates retrieved successfully",
                        rates
                )
        );
    }
}
