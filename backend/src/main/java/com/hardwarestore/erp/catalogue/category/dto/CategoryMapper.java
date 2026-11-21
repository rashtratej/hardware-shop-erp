package com.hardwarestore.erp.catalogue.category.dto;

import com.hardwarestore.erp.catalogue.category.entity.ProductCategory;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(ProductCategory category) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParentCategory() != null
                        ? category.getParentCategory().getId()
                        : null,
                category.getParentCategory() != null
                        ? category.getParentCategory().getName()
                        : null,
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
