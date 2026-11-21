package com.hardwarestore.erp.catalogue.category.repository;

import com.hardwarestore.erp.catalogue.category.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductCategoryRepository
        extends JpaRepository<ProductCategory, UUID> {

    boolean existsByNameIgnoreCaseAndParentCategory_Id(
            String name,
            UUID parentCategoryId
    );

    boolean existsByNameIgnoreCaseAndParentCategoryIsNull(
            String name
    );

    boolean existsByNameIgnoreCaseAndParentCategory_IdAndIdNot(
            String name,
            UUID parentCategoryId,
            UUID id
    );

    boolean existsByNameIgnoreCaseAndParentCategoryIsNullAndIdNot(
            String name,
            UUID id
    );

    boolean existsByParentCategory_IdAndActiveTrue(UUID parentCategoryId);

    List<ProductCategory> findAllByActiveTrueOrderByNameAsc();

    List<ProductCategory> findAllByParentCategory_IdOrderByNameAsc(
            UUID parentCategoryId
    );

    List<ProductCategory> findAllByParentCategoryIsNullOrderByNameAsc();

}
