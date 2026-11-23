
package com.hardwarestore.erp.purchase.controller;

import com.hardwarestore.erp.common.api.ApiResponse;
import com.hardwarestore.erp.purchase.dto.GoodsReceiptRequest;
import com.hardwarestore.erp.purchase.dto.GoodsReceiptResponse;
import com.hardwarestore.erp.purchase.service.GoodsReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    public GoodsReceiptController(
            GoodsReceiptService goodsReceiptService
    ) {
        this.goodsReceiptService = goodsReceiptService;
    }

    @PostMapping("/purchase-orders/{purchaseOrderId}/goods-receipts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> create(
            @PathVariable UUID purchaseOrderId,
            @Valid @RequestBody GoodsReceiptRequest request,
            Authentication authentication
    ) {
        GoodsReceiptResponse response = goodsReceiptService.create(
                purchaseOrderId,
                request,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Goods receipt created successfully",
                        response
                )
        );
    }

    @GetMapping("/purchase-orders/{purchaseOrderId}/goods-receipts")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<GoodsReceiptResponse>>> getByPurchaseOrder(
            @PathVariable UUID purchaseOrderId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Goods receipts retrieved successfully",
                        goodsReceiptService.getByPurchaseOrder(purchaseOrderId)
                )
        );
    }

    @GetMapping("/goods-receipts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Goods receipt retrieved successfully",
                        goodsReceiptService.getById(id)
                )
        );
    }
}