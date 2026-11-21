
package com.hardwarestore.erp.catalogue.category.service;

import com.hardwarestore.erp.catalogue.category.dto.CategoryMapper;
import com.hardwarestore.erp.catalogue.category.dto.CategoryResponse;
import com.hardwarestore.erp.catalogue.category.dto.CreateCategoryRequest;
import com.hardwarestore.erp.catalogue.category.dto.UpdateCategoryRequest;
import com.hardwarestore.erp.catalogue.category.entity.ProductCategory;
import com.hardwarestore.erp.catalogue.category.repository.ProductCategoryRepository;
import com.hardwarestore.erp.common.exception.ResourceConflictException;
import com.hardwarestore.erp.common.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(
            ProductCategoryRepository categoryRepository,
            CategoryMapper categoryMapper
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        String name = request.name().trim();

        ProductCategory parentCategory = null;

        if (request.parentCategoryId() != null) {
            parentCategory = categoryRepository
                    .findById(request.parentCategoryId())
                    .filter(ProductCategory::isActive)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active parent category not found"
                    ));
        }

        boolean duplicate;

        if (parentCategory == null) {
            duplicate = categoryRepository
                    .existsByNameIgnoreCaseAndParentCategoryIsNull(name);
        } else {
            duplicate = categoryRepository
                    .existsByNameIgnoreCaseAndParentCategory_Id(
                            name,
                            parentCategory.getId()
                    );
        }

        if (duplicate) {
            throw new ResourceConflictException(
                    "A category with this name already exists under this parent"
            );
        }

        UUID createdBy = UUID.fromString(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );

        ProductCategory category = ProductCategory.create(
                name,
                request.description(),
                parentCategory,
                createdBy
        );

        ProductCategory savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(
            UUID id,
            UpdateCategoryRequest request
    ) {

        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found"
                ));

        String name = request.name().trim();

        ProductCategory newParent = null;

        if (request.parentCategoryId() != null) {

            if (request.parentCategoryId().equals(id)) {
                throw new ResourceConflictException(
                        "A category cannot be its own parent"
                );
            }

            newParent = categoryRepository
                    .findById(request.parentCategoryId())
                    .filter(ProductCategory::isActive)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active parent category not found"
                    ));

            if (isDescendant(newParent.getId(), id)) {
                throw new ResourceConflictException(
                        "A category cannot be moved under one of its descendants"
                );
            }
        }

        boolean duplicate;

        if (newParent == null) {
            duplicate = categoryRepository
                    .existsByNameIgnoreCaseAndParentCategoryIsNullAndIdNot(
                            name,
                            id
                    );
        } else {
            duplicate = categoryRepository
                    .existsByNameIgnoreCaseAndParentCategory_IdAndIdNot(
                            name,
                            newParent.getId(),
                            id
                    );
        }

        if (duplicate) {
            throw new ResourceConflictException(
                    "A category with this name already exists under this parent"
            );
        }

        category.updateDetails(
                name,
                request.description(),
                newParent
        );

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse changeCategoryStatus(UUID id, boolean active) {

        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found"
                ));

        if (category.isActive() == active) {
            return categoryMapper.toResponse(category);
        }

        if (active && category.getParentCategory() != null
                && !category.getParentCategory().isActive()) {

            throw new ResourceConflictException(
                    "Cannot activate a category whose parent is inactive"
            );
        }

        if (!active && categoryRepository
                .existsByParentCategory_IdAndActiveTrue(id)) {

            throw new ResourceConflictException(
                    "Cannot deactivate a category while it has active child categories"
            );
        }

        category.changeStatus(active);

        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {

        return categoryRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getActiveCategoryById(UUID id) {

        ProductCategory category = categoryRepository
                .findById(id)
                .filter(ProductCategory::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active category not found"
                ));

        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {

        return categoryRepository
                .findAllByParentCategoryIsNullOrderByNameAsc()
                .stream()
                .filter(ProductCategory::isActive)
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildCategories(UUID parentCategoryId) {

        ProductCategory parent = categoryRepository.findById(parentCategoryId)
                .filter(ProductCategory::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active parent category not found"
                ));

        return categoryRepository
                .findAllByParentCategory_IdOrderByNameAsc(parent.getId())
                .stream()
                .filter(ProductCategory::isActive)
                .map(categoryMapper::toResponse)
                .toList();
    }

    private boolean isDescendant(
            UUID potentialDescendantId,
            UUID categoryId
    ) {

        Set<UUID> visited = new HashSet<>();
        ArrayDeque<UUID> queue = new ArrayDeque<>();

        queue.add(categoryId);
        visited.add(categoryId);

        while (!queue.isEmpty()) {

            UUID currentId = queue.remove();

            List<ProductCategory> children =
                    categoryRepository.findAllByParentCategory_IdOrderByNameAsc(
                            currentId
                    );

            for (ProductCategory child : children) {

                UUID childId = child.getId();

                if (childId.equals(potentialDescendantId)) {
                    return true;
                }

                if (visited.add(childId)) {
                    queue.add(childId);
                }
            }
        }

        return false;
    }
}
