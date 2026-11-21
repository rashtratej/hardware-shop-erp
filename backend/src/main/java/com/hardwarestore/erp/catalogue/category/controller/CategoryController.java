
package com.hardwarestore.erp.catalogue.category.controller;

import com.hardwarestore.erp.catalogue.category.dto.CategoryResponse;
import com.hardwarestore.erp.catalogue.category.dto.CreateCategoryRequest;
import com.hardwarestore.erp.catalogue.category.dto.UpdateCategoryRequest;
import com.hardwarestore.erp.catalogue.category.service.CategoryService;
import com.hardwarestore.erp.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hardwarestore.erp.catalogue.category.dto.ChangeCategoryStatusRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CategoryResponse category =
                categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Category created successfully",
                        category
                ));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {

        List<CategoryResponse> categories =
                categoryService.getAllActiveCategories();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categories retrieved successfully",
                        categories
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable UUID id
    ) {
        CategoryResponse category =
                categoryService.getActiveCategoryById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category retrieved successfully",
                        category
                )
        );
    }

    @GetMapping("/roots")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {

        List<CategoryResponse> categories =
                categoryService.getRootCategories();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Root categories retrieved successfully",
                        categories
                )
        );
    }

    @GetMapping("/{id}/children")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getChildCategories(
            @PathVariable UUID id
    ) {
        List<CategoryResponse> categories =
                categoryService.getChildCategories(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Child categories retrieved successfully",
                        categories
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        CategoryResponse category =
                categoryService.updateCategory(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category updated successfully",
                        category
                )
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> changeCategoryStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeCategoryStatusRequest request
    ) {
        CategoryResponse category =
                categoryService.changeCategoryStatus(id, request.active());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category status updated successfully",
                        category
                )
        );
    }
}