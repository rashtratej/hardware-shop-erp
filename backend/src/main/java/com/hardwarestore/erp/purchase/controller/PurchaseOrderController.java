package com.hardwarestore.erp.purchase.controller;

import com.hardwarestore.erp.common.api.ApiResponse;
import com.hardwarestore.erp.purchase.dto.PurchaseOrderReasonRequest;
import com.hardwarestore.erp.purchase.dto.PurchaseOrderRequest;
import com.hardwarestore.erp.purchase.dto.PurchaseOrderResponse;
import com.hardwarestore.erp.purchase.entity.PurchaseOrderStatus;
import com.hardwarestore.erp.purchase.service.PurchaseOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(
            PurchaseOrderService purchaseOrderService
    ) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> create(
            @Valid @RequestBody PurchaseOrderRequest request,
            Authentication authentication
    ) {
        PurchaseOrderResponse response = purchaseOrderService.create(
                request,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Purchase order created successfully",
                        response
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<PurchaseOrderResponse>>> search(
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Page<PurchaseOrderResponse> response =
                purchaseOrderService.search(
                        status,
                        supplierId,
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt")
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Purchase orders retrieved successfully",
                        response
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Purchase order retrieved successfully",
                        purchaseOrderService.getById(id)
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> updateDraft(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderRequest request,
            Authentication authentication
    ) {
        PurchaseOrderResponse response =
                purchaseOrderService.updateDraft(
                        id,
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Draft purchase order updated successfully",
                        response
                )
        );
    }


    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> submitForApproval(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Purchase order submitted for approval successfully",
                        purchaseOrderService.submitForApproval(id)
                )
        );
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> approve(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Purchase order approved successfully",
                        purchaseOrderService.approve(
                                id,
                                authentication.getName()
                        )
                )
        );
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> reject(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderReasonRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Purchase order rejected successfully",
                        purchaseOrderService.reject(
                                id,
                                authentication.getName(),
                                request.reason()
                        )
                )
        );
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> cancel(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderReasonRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Purchase order cancelled successfully",
                        purchaseOrderService.cancel(
                                id,
                                authentication.getName(),
                                request.reason()
                        )
                )
        );
    }
}