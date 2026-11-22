
package com.hardwarestore.erp.catalogue.controller;

import com.hardwarestore.erp.catalogue.dto.CreateProductRequest;
import com.hardwarestore.erp.catalogue.dto.ProductResponse;
import com.hardwarestore.erp.catalogue.service.ProductService;
import com.hardwarestore.erp.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hardwarestore.erp.catalogue.dto.UpdateProductRequest;

import java.util.List;
import java.util.UUID;
import com.hardwarestore.erp.catalogue.dto.ProductPageResponse;
import com.hardwarestore.erp.catalogue.dto.ChangeProductStatusRequest;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse product = productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Product created successfully",
                        product
                ));
    }



    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductPageResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID uomId,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        ProductPageResponse products = productService.searchProducts(
                page,
                size,
                search,
                categoryId,
                uomId,
                active,
                sortBy,
                direction
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Products retrieved successfully",
                        products
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable UUID id
    ) {
        ProductResponse product =
                productService.getActiveProductById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product retrieved successfully",
                        product
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        ProductResponse product =
                productService.updateProduct(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product updated successfully",
                        product
                )
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> changeProductStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeProductStatusRequest request
    ) {
        ProductResponse product =
                productService.changeProductStatus(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product status updated successfully",
                        product
                )
        );
    }

}