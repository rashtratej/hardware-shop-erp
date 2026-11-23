package com.hardwarestore.erp.inventory.controller;

import com.hardwarestore.erp.common.api.ApiResponse;
import com.hardwarestore.erp.inventory.dto.*;
import com.hardwarestore.erp.inventory.service.InventoryService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/stock-in")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> stockIn(
            @Valid @RequestBody StockInRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Stock added successfully",
                        inventoryService.stockIn(
                                request,
                                authentication.getName()
                        )
                )
        );
    }

    @PostMapping("/stock-out")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> stockOut(
            @Valid @RequestBody StockOutRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Stock removed successfully",
                        inventoryService.stockOut(
                                request,
                                authentication.getName()
                        )
                )
        );
    }

    @PostMapping("/adjustments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjust(
            @Valid @RequestBody StockAdjustmentRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Stock adjusted successfully",
                        inventoryService.adjust(
                                request,
                                authentication.getName()
                        )
                )
        );
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(
            @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Inventory retrieved successfully",
                        inventoryService.getInventory(productId)
                )
        );
    }

    @GetMapping("/{productId}/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<StockMovementResponse>>> getMovements(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Invalid pagination parameters"
            );
        }

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Stock movements retrieved successfully",
                        inventoryService.getMovements(productId, pageable)
                )
        );
    }
}